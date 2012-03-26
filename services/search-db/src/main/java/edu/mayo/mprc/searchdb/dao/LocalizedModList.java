package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableListBase;

import java.util.Collection;

/**
 * List of localized modifications.
 *
 * @author Roman Zenka
 */
public final class LocalizedModList extends PersistableListBase<LocalizedModification> {
	public LocalizedModList() {
	}

	public LocalizedModList(final int initialCapacity) {
		super(initialCapacity);
	}

	public LocalizedModList(final Collection<LocalizedModification> items) {
		super(items);
	}
}
