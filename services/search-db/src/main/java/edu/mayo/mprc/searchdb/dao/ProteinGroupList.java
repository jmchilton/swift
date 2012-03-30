package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableSetBase;

import java.util.Collection;

/**
 * A set of protein groups.
 *
 * @author Roman Zenka
 */
public final class ProteinGroupList extends PersistableSetBase<ProteinGroup> {
	public ProteinGroupList() {
	}

	public ProteinGroupList(final Collection<ProteinGroup> items) {
		super(items);
	}

	public ProteinGroupList(final int initialCapacity) {
		super(initialCapacity);
	}
}
