package edu.mayo.mprc.searchdb.dao;

import java.util.List;

/**
 * Identification information for a particular peptide.
 * <p/>
 * The peptide is uniquely identified by its sequence and a list of modifications.
 * <p/>
 * It also remembers a list of proteins it could belong to. This is used as an initial speedup when searching
 * which peptide sequence belongs to which protein.
 *
 * @author Roman Zenka
 */
public class PeptideIdentification {
	/**
	 * A peptide sequence that was determined.
	 */
	private PeptideSequence sequence;

	/**
	 * Previous amino acid. Used e.g. to distinguish whether the algorithm could have
	 * thought this was an actual tryptic peptide (probabilities for those can vary).
	 * This was not actually observed by the instrument and it depends on which protein the algorithm
	 * assigned the peptide to.
	 */
	private char previousAminoAcid;

	/**
	 * Next amino acid. See {@link #previousAminoAcid} for more info.
	 */
	private char nextAminoAcid;

	/**
	 * A list of modifications + their positions.
	 */
	private List<ModificationPosition> modifications;

	/**
	 * Empty constructor for Hibernate.
	 */
	public PeptideIdentification() {
	}

	public PeptideSequence getSequence() {
		return sequence;
	}

	public char getPreviousAminoAcid() {
		return previousAminoAcid;
	}

	public char getNextAminoAcid() {
		return nextAminoAcid;
	}

	public List<ModificationPosition> getModifications() {
		return modifications;
	}
}
