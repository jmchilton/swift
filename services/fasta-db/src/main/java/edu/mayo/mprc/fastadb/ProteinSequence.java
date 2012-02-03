package edu.mayo.mprc.fastadb;

/**
 * A protein sequence. Immutable, stored in the database only once with unique ID.
 *
 * @author Roman Zenka
 */
public final class ProteinSequence extends Sequence {
    ProteinSequence() {
    }

    public ProteinSequence(String sequence) {
        super(sequence);
    }
}
