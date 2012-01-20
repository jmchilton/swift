package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;
import edu.mayo.mprc.searchdb.ScaffoldModificationFormat;

import java.util.Set;
import java.util.TreeSet;

/**
 * The peptide is uniquely identified by its sequence and a list of modifications.
 * <p/>
 * The following fields from Scaffold spectrum report are parsed to fill in this data structure:
 * <ul>
 * <li>Peptide sequence</li>
 * <li>Fixed modifications identified by spectrum</li>
 * <li>Variable modifications identified by spectrum</li>
 * </ul>
 *
 * @author Roman Zenka
 */
public class IdentifiedPeptide extends PersistableBase {
	private static final int EXPECTED_MOD_SIZE = 20;
	/**
	 * A peptide sequence that was determined.
	 */
	private PeptideSequence sequence;

	/**
	 * A list of modifications + their positions. Canonicalized by {@link ScaffoldModificationFormat} parser.
	 */
	private Set<LocalizedModification> modifications;

	/**
	 * Empty constructor for Hibernate.
	 */
	public IdentifiedPeptide() {
	}

	/**
	 * @param sequence              Peptide sequence
	 * @param fixedModifications    {@link ScaffoldModificationFormat} fixed mods.
	 * @param variableModifications {@link ScaffoldModificationFormat} variable mods.
	 * @param format                {@link ScaffoldModificationFormat} that can parse the Scaffold's mods.
	 */
	public IdentifiedPeptide(
			PeptideSequence sequence,
			String fixedModifications,
			String variableModifications,
			ScaffoldModificationFormat format) {
		this.sequence = sequence;
		this.modifications = format.parseModifications(sequence.getSequence(), fixedModifications, variableModifications);
	}

	/**
	 * Use this when localized modification reuse is desired.
	 *
	 * @param sequence      Peptide sequence
	 * @param modifications List of {@link LocalizedModification}
	 */
	public IdentifiedPeptide(PeptideSequence sequence, Set<LocalizedModification> modifications) {
		this.sequence = sequence;
		this.modifications = modifications;
	}

    public PeptideSequence getSequence() {
        return sequence;
    }

    public void setSequence(PeptideSequence sequence) {
        this.sequence = sequence;
    }

    public Set<LocalizedModification> getModifications() {
        return modifications;
    }

    public void setModifications(Set<LocalizedModification> modifications) {
        this.modifications = modifications;
    }

    /**
	 * @return List of mods as comma separated string, e.g. {@code c18: Carbamidomethyl(C)}
	 */
	public String getModificationsAsString() {
		StringBuilder result = new StringBuilder(EXPECTED_MOD_SIZE * getModifications().size());
		for (LocalizedModification modification : getModifications()) {
			result.append(", ");
			result.append(Character.toLowerCase(modification.getResidue())).append(modification.getPosition() + 1).append(": ").append(modification.getModSpecificity().toMascotString());
		}
		return result.length() > 1 ? result.substring(2) : "";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IdentifiedPeptide that = (IdentifiedPeptide) o;

		if (!modifications.equals(that.modifications)) return false;
		if (!sequence.equals(that.sequence)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = sequence.hashCode();
		result = 31 * result + modifications.hashCode();
		return result;
	}
}
