package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.chem.AminoAcidSet;
import edu.mayo.mprc.database.PersistableBase;

/**
 * A sequence of amino acids.
 *
 * @author Roman Zenka
 */
public abstract class Sequence extends PersistableBase {
	private String sequence;
	/**
	 * Monoisotopic mass of the sequence. Can be null if the mass was not determined.
	 */
	private Double mass;

	/**
	 * Empty constructor for Hibernate.
	 */
	public Sequence() {
	}

	public Sequence(String sequence) {
		setSequence(sequence);
		if (sequence == null) {
			setMass(null);
		} else {
			setMass(AminoAcidSet.DEFAULT.getMonoisotopicMass(sequence));
		}
	}

	public String getSequence() {
		return sequence;
	}

	void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public Double getMass() {
		return mass;
	}

	void setMass(Double mass) {
		this.mass = mass;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Sequence)) {
			return false;
		}

		Sequence that = (Sequence) o;

		if (!getSequence().equals(that.getSequence())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getSequence().hashCode();
	}
}
