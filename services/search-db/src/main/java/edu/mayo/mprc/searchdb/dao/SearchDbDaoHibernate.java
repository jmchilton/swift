package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.RuntimeInitializer;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.database.PersistableListBase;
import edu.mayo.mprc.fastadb.FastaDbDao;
import edu.mayo.mprc.fastadb.ProteinSequence;
import edu.mayo.mprc.swift.db.SwiftDao;
import edu.mayo.mprc.swift.dbmapping.ReportData;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * DAO for the search results stored in the database.
 * We use stateless session to speed up the access, as this dao is used for bulk data loading.
 *
 * @author Roman Zenka
 */
public final class SearchDbDaoHibernate extends DaoBase implements RuntimeInitializer, SearchDbDao {
	private SwiftDao swiftDao;
	private FastaDbDao fastaDbDao;

	private final String MAP = "edu/mayo/mprc/searchdb/dao/";

	public SearchDbDaoHibernate() {
	}

	public SearchDbDaoHibernate(SwiftDao swiftDao, FastaDbDao fastaDbDao, DatabasePlaceholder databasePlaceholder) {
		super(databasePlaceholder);
		this.swiftDao = swiftDao;
		this.fastaDbDao = fastaDbDao;
	}

	public SwiftDao getSwiftDao() {
		return swiftDao;
	}

	public void setSwiftDao(SwiftDao swiftDao) {
		this.swiftDao = swiftDao;
	}

	public FastaDbDao getFastaDbDao() {
		return fastaDbDao;
	}

	public void setFastaDbDao(FastaDbDao fastaDbDao) {
		this.fastaDbDao = fastaDbDao;
	}

	@Override
	public String check(Map<String, String> params) {
		return null;
	}

	@Override
	public void initialize(Map<String, String> params) {
	}

	@Override
	public LocalizedModification addLocalizedModification(LocalizedModification mod) {
		if (mod.getId() == null) {
			return save(mod, localizedModificationEqualityCriteria(mod), false);
		}
		return mod;
	}

	private Criterion localizedModificationEqualityCriteria(LocalizedModification mod) {
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

			peptide.setSequence(fastaDbDao.addPeptideSequence(peptide.getSequence()));
			try {
				return save(peptide, identifiedPeptideEqualityCriteria(peptide), false);
			} catch (Exception e) {
				throw new MprcException("Could not add identified peptide", e);
			}
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

				.add(doubleEq("bestSearchEngineScores.mascotDeltaIonScore", match.getBestSearchEngineScores().getMascotDeltaIonScore(), SearchEngineScores.DELTA))
				.add(doubleEq("bestSearchEngineScores.mascotHomologyScore", match.getBestSearchEngineScores().getMascotHomologyScore(), SearchEngineScores.DELTA))
				.add(doubleEq("bestSearchEngineScores.mascotIdentityScore", match.getBestSearchEngineScores().getMascotIdentityScore(), SearchEngineScores.DELTA))
				.add(doubleEq("bestSearchEngineScores.mascotIonScore", match.getBestSearchEngineScores().getMascotIonScore(), SearchEngineScores.DELTA))
				.add(doubleEq("bestSearchEngineScores.sequestDcnScore", match.getBestSearchEngineScores().getSequestDcnScore(), SearchEngineScores.DELTA))
				.add(doubleEq("bestSearchEngineScores.sequestPeptidesMatched", match.getBestSearchEngineScores().getSequestPeptidesMatched(), SearchEngineScores.DELTA))
				.add(doubleEq("bestSearchEngineScores.sequestSpRank", match.getBestSearchEngineScores().getSequestSpRank(), SearchEngineScores.DELTA))
				.add(doubleEq("bestSearchEngineScores.sequestSpScore", match.getBestSearchEngineScores().getSequestSpScore(), SearchEngineScores.DELTA))
				.add(doubleEq("bestSearchEngineScores.sequestXcorrScore", match.getBestSearchEngineScores().getSequestXcorrScore(), SearchEngineScores.DELTA))
				.add(doubleEq("bestSearchEngineScores.tandemHyperScore", match.getBestSearchEngineScores().getTandemHyperScore(), SearchEngineScores.DELTA))
				.add(doubleEq("bestSearchEngineScores.tandemLadderScore", match.getBestSearchEngineScores().getTandemLadderScore(), SearchEngineScores.DELTA))

				.add(Restrictions.eq("spectrumIdentificationCounts.numberOfIdentifiedSpectra", match.getSpectrumIdentificationCounts().getNumberOfIdentifiedSpectra()))
				.add(Restrictions.eq("spectrumIdentificationCounts.numberOfIdentified1HSpectra", match.getSpectrumIdentificationCounts().getNumberOfIdentified1HSpectra()))
				.add(Restrictions.eq("spectrumIdentificationCounts.numberOfIdentified2HSpectra", match.getSpectrumIdentificationCounts().getNumberOfIdentified2HSpectra()))
				.add(Restrictions.eq("spectrumIdentificationCounts.numberOfIdentified3HSpectra", match.getSpectrumIdentificationCounts().getNumberOfIdentified3HSpectra()))
				.add(Restrictions.eq("spectrumIdentificationCounts.numberOfIdentified4HSpectra", match.getSpectrumIdentificationCounts().getNumberOfIdentified4HSpectra()))

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
						newList.add(fastaDbDao.addProteinSequence(item));
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
	public Analysis addAnalysis(Analysis analysis, ReportData reportData) {
		if (analysis.getId() == null) {
			final BiologicalSampleList originalList = analysis.getBiologicalSamples();
			if (originalList.getId() == null) {
				BiologicalSampleList newList = new BiologicalSampleList(originalList.size());
				for (BiologicalSample sample : originalList) {
					newList.add(addBiologicalSample(sample));
				}
				analysis.setBiologicalSamples(addList(newList));
				analysis.setReportData(reportData);
			}
			return save(analysis, analysisEqualityCriteria(analysis), false);
		}
		return analysis;
	}

	@Override
	public Analysis getAnalysis(long reportId) {
		return (Analysis) getSession().createCriteria(Analysis.class).add(Restrictions.eq("reportData.id", reportId)).uniqueResult();
	}

	@Override
	public boolean hasAnalysis(long reportId) {
		return (Long) getSession().createQuery("select count(*) from Analysis a where a.reportData.id =:reportId")
				.setParameter("reportId", reportId).uniqueResult() > 0;
	}

	@Override
	public List<String> getProteinAccessionNumbers(ProteinSequenceList proteinSequenceList) {
		return (List<String>) getSession().createQuery("select distinct e.accessionNumber from ProteinDatabaseEntry e where e.sequence in (:sequences) order by e.accessionNumber")
				.setParameterList("sequences", proteinSequenceList.getList())
				.list();
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
				MAP + "PeptideSpectrumMatch.hbm.xml",
				MAP + "ProteinGroup.hbm.xml",
				MAP + "ProteinGroupList.hbm.xml",
				MAP + "ProteinSequenceList.hbm.xml",
				MAP + "PsmList.hbm.xml",
				MAP + "SearchResult.hbm.xml",
				MAP + "SearchResultList.hbm.xml",
				MAP + "TandemMassSpectrometrySample.hbm.xml"
		);
	}
}
