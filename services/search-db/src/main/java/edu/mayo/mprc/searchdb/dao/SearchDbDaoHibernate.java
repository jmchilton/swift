package edu.mayo.mprc.searchdb.dao;

import com.google.common.collect.Lists;
import edu.mayo.mprc.config.RuntimeInitializer;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.swift.db.SwiftDao;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
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
        return save(proteinSequence, Restrictions.eq("sequence", proteinSequence.getSequence()), false);
    }

    @Override
    public ProteinSequence getProteinSequence(int proteinId) {
        return (ProteinSequence) getSession().get(ProteinSequence.class, proteinId);
    }

    @Override
    public PeptideSequence addPeptideSequence(PeptideSequence peptideSequence) {
        return save(peptideSequence, Restrictions.eq("sequence", peptideSequence.getSequence()), false);
    }

    @Override
    public PeptideSequence getPeptideSequence(int peptideId) {
        return (PeptideSequence) getSession().get(PeptideSequence.class, peptideId);
    }

    @Override
    public LocalizedModification addLocalizedModification(LocalizedModification mod) {
        return save(mod, localizedModificationEqualityCriteria(mod), false);
    }

    private Junction localizedModificationEqualityCriteria(LocalizedModification mod) {
        return Restrictions.conjunction()
                .add(Restrictions.eq("position", mod.getPosition()))
                .add(Restrictions.eq("residue", mod.getResidue()))
                .add(associationEq("modSpecificity", mod.getModSpecificity()));
    }

    @Override
    public IdentifiedPeptide addIdentifiedPeptide(IdentifiedPeptide peptide) {
        ArrayList<LocalizedModification> savedMods = Lists.newArrayListWithCapacity(peptide.getModifications().size());
        for (LocalizedModification localizedModification : peptide.getModifications()) {
            savedMods.add(addLocalizedModification(localizedModification));
        }
        peptide.setModifications(savedMods);
        peptide.setSequence(addPeptideSequence(peptide.getSequence()));
        return save(peptide, identifiedPeptideEqualityCriteria(peptide), false);
    }

    private Criterion identifiedPeptideEqualityCriteria(IdentifiedPeptide peptide) {
        return Restrictions.conjunction()
                .add(associationEq("sequence", peptide.getSequence()));
        // CONTINUE HERE;
    }

    @Override
    public PeptideSpectrumMatch addPeptideSpectrumMatch(PeptideSpectrumMatch match) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProteinGroup addProteinGroup(ProteinGroup group) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TandemMassSpectrometrySample addTandemMassSpectrometrySample(TandemMassSpectrometrySample sample) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SearchResult addSearchResult(SearchResult searchResult) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public BiologicalSample addBiologicalSample(BiologicalSample biologicalSample) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Analysis addAnalysis(Analysis analysis) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<String> getHibernateMappings() {
        return Arrays.asList(
                MAP + "Analysis.hbm.xml",
                MAP + "BiologicalSample.hbm.xml",
                MAP + "IdentifiedPeptide.hbm.xml",
                MAP + "LocalizedModification.hbm.xml",
                MAP + "PeptideSequence.hbm.xml",
                MAP + "PeptideSpectrumMatch.hbm.xml",
                MAP + "ProteinGroup.hbm.xml",
                MAP + "ProteinSequence.hbm.xml",
                MAP + "SearchResult.hbm.xml",
                MAP + "TandemMassSpectrometrySample.hbm.xml"
        );
    }
}
