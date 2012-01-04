package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;
import edu.mayo.mprc.unimod.ModSpecificity;

/**
 * A modification at a particular position.
 *
 * @author Roman Zenka
 */
public class LocalizedModification extends PersistableBase {
	/**
	 * Observed modification specificity. Keep in mind that what the software reports does not have to be
	 * what actually happened - it can be a different mod with the same mass.
	 */
	private ModSpecificity modSpecificity;

	/**
	 * Position where the modification was observed. The numbering starts from 0 corresponding to the
	 * first amino acid of the sequence (starting from the N-terminus).
	 */
	private int position;

	/**
	 * The residue the modification is residing on.
	 */
	private char residue;

	/**
	 * Empty constructor for Hibernate.
	 */
	public LocalizedModification() {
	}

	public LocalizedModification(ModSpecificity modSpecificity, int position, char residue) {
		this.modSpecificity = modSpecificity;
		this.position = position;
		this.residue = residue;
	}

	public ModSpecificity getModSpecificity() {
		return modSpecificity;
	}

	public int getPosition() {
		return position;
	}

	public char getResidue() {
		return residue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LocalizedModification that = (LocalizedModification) o;

		if (position != that.position) return false;
		if (residue != that.residue) return false;
		if (!modSpecificity.equals(that.modSpecificity)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = modSpecificity.hashCode();
		result = 31 * result + position;
		result = 31 * result + (int) residue;
		return result;
	}
}
