package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.RuntimeInitializer;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.database.PersistableListBase;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.fasta.FASTAInputStream;
import edu.mayo.mprc.swift.db.SwiftDao;
import edu.mayo.mprc.utilities.FileUtilities;
import org.hibernate.Query;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * DAO for the search results stored in the database.
 * We use stateless session to speed up the access, as this dao is used for bulk data loading.
 *
 * @author Roman Zenka
 */
public final class SearchDbDaoHibernate extends DaoBase implements RuntimeInitializer, SearchDbDao {
    private SwiftDao swiftDao;

    private final String MAP = "edu/mayo/mprc/searchdb/dao/";

    public SearchDbDaoHibernate() {
    }

    public SearchDbDaoHibernate(SwiftDao swiftDao, DatabasePlaceholder databasePlaceholder) {
        super(databasePlaceholder);
        this.swiftDao = swiftDao;
    }

    public SwiftDao getSwiftDao() {
        return swiftDao;
    }

    public void setSwiftDao(SwiftDao swiftDao) {
        this.swiftDao = swiftDao;
    }

    @Override
    public String check(Map<String, String> params) {
        return null;
    }

    @Override
    public void initialize(Map<String, String> params) {
    }

    @Override
    public ProteinSequence addProteinSequence(ProteinSequence proteinSequence) {
        if (proteinSequence.getId() == null) {
            return save(proteinSequence, nullSafeEq("sequence", proteinSequence.getSequence()), false);
        }
        return proteinSequence;
    }

    private ProteinSequence addProteinSequence(StatelessSession session, ProteinSequence proteinSequence) {
        if (proteinSequence.getId() == null) {
            return saveStateless(session, proteinSequence, nullSafeEq("sequence", proteinSequence.getSequence()), false);
        }
        return proteinSequence;
    }

    @Override
    public ProteinSequence getProteinSequence(int proteinId) {
        return (ProteinSequence) getSession().get(ProteinSequence.class, proteinId);
    }

    @Override
    public PeptideSequence addPeptideSequence(PeptideSequence peptideSequence) {
        if (peptideSequence.getId() == null) {
            return save(peptideSequence, nullSafeEq("sequence", peptideSequence.getSequence()), false);
        }
        return peptideSequence;
    }

    @Override
    public PeptideSequence getPeptideSequence(int peptideId) {
        return (PeptideSequence) getSession().get(PeptideSequence.class, peptideId);
    }

    @Override
    public long countDatabaseEntries(Curation database) {
        return (Long) getSession().createCriteria(ProteinDatabaseEntry.class)
                .add(associationEq("database", database))
                .setProjection(Projections.count("database"))
                .uniqueResult();
    }

    /**
     * This method opens its own stateless session for its duration, so you do not need to call {@link #begin}
     * or {@link #commit} around this method. This makes the method quite special.
     * <p/>
     * If the curation was already previously loaded into the database, the method does nothing.
     *
     * @param database Database to load data for.
     */
    @Override
    public void addFastaDatabase(Curation database) {
        final StatelessSession session = getDatabasePlaceholder().getSessionFactory().openStatelessSession();
        Query entryCount = session.createQuery("select 1 from ProteinDatabaseEntry p where p.database=:database").setEntity("database", database);
        if (entryCount.uniqueResult() != null) {
            // We have loaded the database already
            return;
        }

        final File fasta = database.getFastaFile().getFile();
        final FASTAInputStream stream = new FASTAInputStream(fasta);
        try {
            stream.beforeFirst();
            session.getTransaction().begin();
            while (stream.gotoNextSequence()) {
                final String header = stream.getHeader();
                final String sequence = stream.getSequence();
                int space = header.indexOf(' ');
                final String accessionNumber;
                if (space >= 1) {
                    accessionNumber = header.substring(1, space);
                } else {
                    accessionNumber = header.substring(1);
                }
                final ProteinSequence proteinSequence = addProteinSequence(session, new ProteinSequence(sequence));
                final ProteinDatabaseEntry entry = new ProteinDatabaseEntry(database, accessionNumber, proteinSequence);
                saveStateless(session, entry, entryEqualityCriteria(entry), false);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new MprcException("Could not add FASTA file to database " + database.getTitle(), e);
        } finally {
            FileUtilities.closeQuietly(stream);
            session.close();
        }
    }

    private Criterion entryEqualityCriteria(ProteinDatabaseEntry entry) {
        return Restrictions.conjunction()
                .add(associationEq("database", entry.getDatabase()))
                .add(nullSafeEq("accessionNumber", entry.getAccessionNumber()))
                .add(associationEq("sequence", entry.getSequence()));
    }

    @Override
    public LocalizedModification addLocalizedModification(LocalizedModification mod) {
        if (mod.getId() == null) {
            return save(mod, localizedModificationEqualityCriteria(mod), false);
        }
        return mod;
    }

    private Junction localizedModificationEqualityCriteria(LocalizedModification mod) {
        return Restrictions.conjunction()
                .add(nullSafeEq("position", mod.getPosition()))
                .add(nullSafeEq("residue", mod.getResidue()))
                .add(associationEq("modSpecificity", mod.getModSpecificity()));
    }

    @Override
    public IdentifiedPeptide addIdentifiedPeptide(IdentifiedPeptide peptide) {
        if (peptide.getId() == null) {
            final LocalizedModList originalList = peptide.getModifications();
            if (originalList.getId() == null) {
                LocalizedModList newList = new LocalizedModList(originalList.size());
                for (LocalizedModification item : originalList) {
                    newList.add(addLocalizedModification(item));
                }
                peptide.setModifications(addList(newList));
            }

            peptide.setSequence(addPeptideSequence(peptide.getSequence()));
            return save(peptide, identifiedPeptideEqualityCriteria(peptide), false);
        }
        return peptide;
    }

    private Criterion identifiedPeptideEqualityCriteria(IdentifiedPeptide peptide) {
        return Restrictions.conjunction()
                .add(associationEq("sequence", peptide.getSequence()))
                .add(associationEq("modifications", peptide.getModifications()));
    }

    @Override
    public PeptideSpectrumMatch addPeptideSpectrumMatch(PeptideSpectrumMatch match) {
        if (match.getId() == null) {
            match.setPeptide(addIdentifiedPeptide(match.getPeptide()));
            return save(match, matchEqualityCriteria(match), false);
        }
        return match;
    }

    private Criterion matchEqualityCriteria(PeptideSpectrumMatch match) {
        return Restrictions.conjunction()
                .add(associationEq("peptide", match.getPeptide()))
                .add(nullSafeEq("previousAminoAcid", match.getPreviousAminoAcid()))
                .add(nullSafeEq("nextAminoAcid", match.getNextAminoAcid()))
                .add(nullSafeEq("bestPeptideIdentificationProbability", match.getBestPeptideIdentificationProbability()))
                .add(nullSafeEq("bestSearchEngineScores", match.getBestSearchEngineScores()))
                .add(nullSafeEq("spectrumIdentificationCounts", match.getSpectrumIdentificationCounts()))
                .add(nullSafeEq("numberOfEnzymaticTerminii", match.getNumberOfEnzymaticTerminii()));
    }

    @Override
    public ProteinGroup addProteinGroup(ProteinGroup group) {
        if (group.getId() == null) {
            {
                final ProteinSequenceList originalList = group.getProteinSequences();
                if (originalList.getId() == null) {
                    ProteinSequenceList newList = new ProteinSequenceList(originalList.size());
                    for (ProteinSequence item : originalList) {
                        newList.add(addProteinSequence(item));
                    }
                    group.setProteinSequences(addList(newList));
                }
            }

            {
                final PsmList originalList = group.getPeptideSpectrumMatches();
                if (originalList.getId() == null) {
                    PsmList newList = new PsmList(originalList.size());
                    for (PeptideSpectrumMatch item : originalList) {
                        newList.add(addPeptideSpectrumMatch(item));
                    }
                    group.setPeptideSpectrumMatches(addList(newList));
                }
            }

            return save(group, proteinGroupEqualityCriteria(group), false);
        }
        return group;
    }

    private Criterion proteinGroupEqualityCriteria(ProteinGroup group) {
        return Restrictions.conjunction()
                .add(associationEq("proteinSequences", group.getProteinSequences()))
                .add(associationEq("peptideSpectrumMatches", group.getPeptideSpectrumMatches()))
                .add(nullSafeEq("proteinIdentificationProbability", group.getProteinIdentificationProbability()))
                .add(nullSafeEq("numberOfUniquePeptides", group.getNumberOfUniquePeptides()))
                .add(nullSafeEq("numberOfUniqueSpectra", group.getNumberOfUniqueSpectra()))
                .add(nullSafeEq("numberOfTotalSpectra", group.getNumberOfTotalSpectra()))
                .add(nullSafeEq("percentageOfTotalSpectra", group.getPercentageOfTotalSpectra()))
                .add(nullSafeEq("percentageSequenceCoverage", group.getPercentageSequenceCoverage()));
    }

    @Override
    public TandemMassSpectrometrySample addTandemMassSpectrometrySample(TandemMassSpectrometrySample sample) {
        if (sample == null) {
            return null;
        }
        if (sample.getId() == null) {
            return save(sample, sampleEqualityCriteria(sample), false);
        }
        return sample;
    }

    /**
     * Two {@link TandemMassSpectrometrySample} objects are considered identical if they point to the same file.
     * This way it is possible to update an older extraction of metadata for a file.
     */
    private Criterion sampleEqualityCriteria(TandemMassSpectrometrySample sample) {
        return Restrictions.conjunction()
                .add(nullSafeEq("file", sample.getFile()));
    }

    @Override
    public SearchResult addSearchResult(SearchResult searchResult) {
        if (searchResult.getId() == null) {
            searchResult.setMassSpecSample(addTandemMassSpectrometrySample(searchResult.getMassSpecSample()));
            final ProteinGroupList originalList = searchResult.getProteinGroups();
            if (originalList.getId() == null) {
                ProteinGroupList newList = new ProteinGroupList(originalList.size());
                for (ProteinGroup item : originalList) {
                    newList.add(addProteinGroup(item));
                }
                searchResult.setProteinGroups(addList(newList));
            }
            return save(searchResult, searchResultEqualityCriteria(searchResult), false);
        }
        return searchResult;
    }

    private Criterion searchResultEqualityCriteria(SearchResult searchResult) {
        return Restrictions.conjunction()
                .add(associationEq("massSpecSample", searchResult.getMassSpecSample()))
                .add(associationEq("proteinGroups", searchResult.getProteinGroups()));
    }

    @Override
    public BiologicalSample addBiologicalSample(BiologicalSample biologicalSample) {
        if (biologicalSample.getId() == null) {
            final SearchResultList originalList = biologicalSample.getSearchResults();
            if (originalList.getId() == null) {
                SearchResultList newList = new SearchResultList(originalList.size());
                for (SearchResult item : originalList) {
                    newList.add(addSearchResult(item));
                }
                biologicalSample.setSearchResults(addList(newList));
            }
            return save(biologicalSample, biologicalSampleEqualityCriteria(biologicalSample), false);
        }
        return biologicalSample;
    }

    private Criterion biologicalSampleEqualityCriteria(BiologicalSample biologicalSample) {
        return Restrictions.conjunction()
                .add(nullSafeEq("sampleName", biologicalSample.getCategory()))
                .add(nullSafeEq("category", biologicalSample.getCategory()))
                .add(associationEq("searchResults", biologicalSample.getSearchResults()));
    }

    @Override
    public Analysis addAnalysis(Analysis analysis) {
        if (analysis.getId() == null) {
            final BiologicalSampleList originalList = analysis.getBiologicalSamples();
            if (originalList.getId() == null) {
                BiologicalSampleList newList = new BiologicalSampleList(originalList.size());
                for (BiologicalSample sample : originalList) {
                    newList.add(addBiologicalSample(sample));
                }
                analysis.setBiologicalSamples(addList(newList));
            }
            return save(analysis, analysisEqualityCriteria(analysis), false);
        }
        return analysis;
    }

    private Criterion analysisEqualityCriteria(Analysis analysis) {
        return Restrictions.conjunction()
                .add(nullSafeEq("scaffoldVersion", analysis.getScaffoldVersion()))
                .add(nullSafeEq("analysisDate", analysis.getAnalysisDate()))
                .add(associationEq("biologicalSamples", analysis.getBiologicalSamples()));
    }

    /**
     * Save any kind of list into the database.
     *
     * @param list List to save.
     * @param <T>  Type of the list, must extend {@link PersistableListBase}
     * @return Saved list (or the same one in case it was saved already).
     */
    private <T extends PersistableListBase<?>> T addList(T list) {
        if (list.getId() == null) {
            return updateSet(list, list.getList(), "list");
        }
        return list;
    }

    @Override
    public Collection<String> getHibernateMappings() {
        return Arrays.asList(
                MAP + "Analysis.hbm.xml",
                MAP + "BiologicalSample.hbm.xml",
                MAP + "BiologicalSampleList.hbm.xml",
                MAP + "IdentifiedPeptide.hbm.xml",
                MAP + "LocalizedModification.hbm.xml",
                MAP + "LocalizedModList.hbm.xml",
                MAP + "PeptideSequence.hbm.xml",
                MAP + "PeptideSpectrumMatch.hbm.xml",
                MAP + "ProteinDatabaseEntry.hbm.xml",
                MAP + "ProteinGroup.hbm.xml",
                MAP + "ProteinGroupList.hbm.xml",
                MAP + "ProteinSequence.hbm.xml",
                MAP + "ProteinSequenceList.hbm.xml",
                MAP + "PsmList.hbm.xml",
                MAP + "SearchResult.hbm.xml",
                MAP + "SearchResultList.hbm.xml",
                MAP + "TandemMassSpectrometrySample.hbm.xml"
        );
    }
}
