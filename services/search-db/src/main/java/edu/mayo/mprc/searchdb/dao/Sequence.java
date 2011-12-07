package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.chem.AminoAcidSet;
import edu.mayo.mprc.database.PersistableBase;

/**
 * A sequence of amino acids.
 */
public abstract class Sequence extends PersistableBase {
	private String sequence;
	/**
	 * Monoisotopic mass of the sequence.
	 */
	private Double mass;

	public Sequence() {
	}

	public Sequence(String sequence) {
		setSequence(sequence);
		setMass(AminoAcidSet.DEFAULT.getMonoisotopicMass(sequence));
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
		if (!(o instanceof ProteinSequence)) {
			return false;
		}

		ProteinSequence that = (ProteinSequence) o;

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
