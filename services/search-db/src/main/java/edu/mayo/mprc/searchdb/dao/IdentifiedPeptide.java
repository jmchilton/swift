package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;

import java.util.List;

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
	/**
	 * A peptide sequence that was determined.
	 */
	private PeptideSequence sequence;

	/**
	 * A list of modifications + their positions.
	 */
	private List<ModificationPosition> modifications;

	/**
	 * Empty constructor for Hibernate.
	 */
	public IdentifiedPeptide() {
	}

	public PeptideSequence getSequence() {
		return sequence;
	}

	public List<ModificationPosition> getModifications() {
		return modifications;
	}
}
