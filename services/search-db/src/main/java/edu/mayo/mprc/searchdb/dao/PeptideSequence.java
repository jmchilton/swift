package edu.mayo.mprc.searchdb.dao;

/**
 * A peptide sequence. Immutable, stored in the database only once with unique ID.
 */
public final class PeptideSequence extends Sequence {
	PeptideSequence() {
	}

	public PeptideSequence(String sequence) {
		super(sequence);
	}
}
