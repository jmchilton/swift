package edu.mayo.mprc.searchdb.dao;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

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
			id = proteinSequences.size() + 1;
			proteinSequences.put(id, sequence);
		}
		return proteinSequence(id, sequence);
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
			id = peptideSequences.size() + 1;
			peptideSequences.put(id, sequence);
		}
		return peptideSequence(id, sequence);
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
