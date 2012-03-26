package edu.mayo.mprc.fastadb;

/**
 * A peptide sequence. Immutable, stored in the database only once with unique ID.
 */
public final class PeptideSequence extends Sequence {
	PeptideSequence() {
	}

	public PeptideSequence(final String sequence) {
		super(sequence);
	}
}
