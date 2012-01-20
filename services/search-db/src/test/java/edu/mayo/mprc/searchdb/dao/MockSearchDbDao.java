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
	public void addProteinSequence(ProteinSequence proteinSequence) {
		final String sequence = proteinSequence.getSequence();
		Integer id = proteinSequences.inverse().get(sequence);
		if (id == null) {
			final int newId = proteinSequences.size() + 1;
			proteinSequences.put(newId, sequence);
			proteinSequence.setId(newId);
		}
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
	public void addPeptideSequence(PeptideSequence peptideSequence) {
		final String sequence = peptideSequence.getSequence();
		Integer id = peptideSequences.inverse().get(sequence);
		if (id == null) {
			final int newId = peptideSequences.size() + 1;
			peptideSequences.put(newId, sequence);
			peptideSequence.setId(newId);
		}
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
	public void addLocalizedModification(LocalizedModification mod) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addIdentifiedPeptide(IdentifiedPeptide peptide) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addPeptideSpectrumMatch(PeptideSpectrumMatch match) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addProteinGroup(ProteinGroup group) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addTandemMassSpectrometrySample(TandemMassSpectrometrySample sample) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addSearchResult(SearchResult searchResult) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addBiologicalSample(BiologicalSample biologicalSample) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addAnalysis(Analysis analysis) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
