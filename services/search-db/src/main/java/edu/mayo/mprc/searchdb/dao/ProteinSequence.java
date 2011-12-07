package edu.mayo.mprc.searchdb.dao;

/**
 * A protein sequence. Immutable, stored in the database only once with unique ID.
 */
public final class ProteinSequence extends Sequence {
	ProteinSequence() {
	}

	public ProteinSequence(String sequence) {
		super(sequence);
	}
}
