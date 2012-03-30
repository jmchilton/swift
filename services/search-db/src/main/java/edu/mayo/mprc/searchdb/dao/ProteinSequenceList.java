package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableHashedSetBase;
import edu.mayo.mprc.fastadb.ProteinSequence;

import java.util.Collection;

/**
 * A set of protein sequences.
 *
 * @author Roman Zenka
 */
public final class ProteinSequenceList extends PersistableHashedSetBase<ProteinSequence> {
	public ProteinSequenceList() {
	}

	public ProteinSequenceList(final int initialCapacity) {
		super(initialCapacity);
	}

	public ProteinSequenceList(final Collection<ProteinSequence> items) {
		super(items);
	}
}
