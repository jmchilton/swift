package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;
import edu.mayo.mprc.unimod.ModSpecificity;
import edu.mayo.mprc.utilities.ComparisonChain;

/**
 * A modification at a particular position.
 * <p/>
 * * To return a list of localized modifications in a canonical order (to make database matching easier),
 * we define ordering on localized modifications. See {@link #compareTo} for more information.
 *
 * @author Roman Zenka
 */
public class LocalizedModification extends PersistableBase implements Comparable<LocalizedModification> {
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

	/**
	 * <p/>
	 * The mods are ordered by:
	 * <ol>
	 * <li>position, ascending</li>
	 * <li>modSpecificity.modification.title, ascending</li>
	 * </ol>
	 *
	 * @param o Other modification to compare to.
	 * @return compareTo result (-1=less than, 0=identical, 1=greater than the other mod)
	 */
	@Override
	public int compareTo(LocalizedModification o) {
		return ComparisonChain
				.start()
				.compare(position, o.position)
				.compare(modSpecificity.getModification().getTitle(), o.modSpecificity.getModification().getTitle())
				.result();
	}
}
