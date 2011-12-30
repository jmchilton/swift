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
	public ProteinSequence addProteinSequence(String sequence) {
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
	public PeptideSequence addPeptideSequence(String sequence) {
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
}
