package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;
import edu.mayo.mprc.unimod.Mod;

/**
 * A modification at a particular position.
 *
 * @author Roman Zenka
 */
public class ModificationPosition extends PersistableBase {
	/**
	 * Observed modification. Keep in mind that what the software reports does not have to be
	 * what actually happened - it can be a different mod with the same mass.
	 */
	private Mod modification;

	/**
	 * Position where the modification was observed. The numbering starts from 0 corresponding to the
	 * first amino acid of the sequence (starting from the N-terminus).
	 */
	private int position;

	/**
	 * Empty constructor for Hibernate.
	 */
	public ModificationPosition() {
	}

	public ModificationPosition(Mod modification, int position) {
		this.modification = modification;
		this.position = position;
	}

	public Mod getModification() {
		return modification;
	}

	public int getPosition() {
		return position;
	}
}
