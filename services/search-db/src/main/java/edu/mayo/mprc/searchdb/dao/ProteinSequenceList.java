package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableListBase;
import edu.mayo.mprc.fastadb.ProteinSequence;

import java.util.Collection;

/**
 * A list of protein sequences.
 *
 * @author Roman Zenka
 */
public final class ProteinSequenceList extends PersistableListBase<ProteinSequence> {
	public ProteinSequenceList() {
	}

	public ProteinSequenceList(final int initialCapacity) {
		super(initialCapacity);
	}

	public ProteinSequenceList(final Collection<ProteinSequence> items) {
		super(items);
	}
}
