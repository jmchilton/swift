package edu.mayo.mprc.searchdb.dao;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import edu.mayo.mprc.dbcurator.model.Curation;

public final class MockSearchDbDao implements SearchDbDao {
    private BiMap<Integer, String> proteinSequences = HashBiMap.create();
    private BiMap<Integer, String> peptideSequences = HashBiMap.create();

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

    private ProteinSequence proteinSequence(Integer id, String sequence) {
        final ProteinSequence proteinSequence = new ProteinSequence(sequence);
        proteinSequence.setId(id);
        return proteinSequence;
    }

    @Override
    public ProteinSequence addProteinSequence(ProteinSequence proteinSequence) {
        final String sequence = proteinSequence.getSequence();
        Integer id = proteinSequences.inverse().get(sequence);
        if (id == null) {
            final int newId = proteinSequences.size() + 1;
            proteinSequences.put(newId, sequence);
            proteinSequence.setId(newId);
        }
        return proteinSequence;
    }

    @Override
    public ProteinSequence getProteinSequence(int proteinId) {
        String sequence = proteinSequences.get(proteinId);
        if (sequence != null) {
            return proteinSequence(proteinId, sequence);
        }
        return null;
    }

    @Override
    public PeptideSequence addPeptideSequence(PeptideSequence peptideSequence) {
        final String sequence = peptideSequence.getSequence();
        Integer id = peptideSequences.inverse().get(sequence);
        if (id == null) {
            final int newId = peptideSequences.size() + 1;
            peptideSequences.put(newId, sequence);
            peptideSequence.setId(newId);
        }
        return peptideSequence;
    }

    private PeptideSequence peptideSequence(Integer id, String sequence) {
        final PeptideSequence peptideSequence = new PeptideSequence(sequence);
        peptideSequence.setId(id);
        return peptideSequence;
    }

    @Override
    public PeptideSequence getPeptideSequence(int peptideId) {
        String sequence = peptideSequences.get(peptideId);
        if (sequence != null) {
            return peptideSequence(peptideId, sequence);
        }
        return null;
    }

    @Override
    public long countDatabaseEntries(Curation database) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addFastaDatabase(Curation database) {

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
