package edu.mayo.mprc.searchdb.dao;

public final class MockSearchDbDao implements SearchDbDao {
    public MockSearchDbDao() {
    }

    @Override
    public void begin() {
    }

    @Override
    public void commit() {
    }

    @Override
    public void rollback() {
    }


    @Override
    public LocalizedModification addLocalizedModification(LocalizedModification mod) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IdentifiedPeptide addIdentifiedPeptide(IdentifiedPeptide peptide) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
}
