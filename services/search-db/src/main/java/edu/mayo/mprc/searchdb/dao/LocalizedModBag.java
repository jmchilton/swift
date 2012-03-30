package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBagBase;

import java.util.Collection;

/**
 * A bag of localized modifications. This can have the same entry more than once, because Scaffold
 * sometimes reports applying the same mod to the same residue more than once for inexplicable reasons (probably a bug).
 * We need to store these results to deal with later, otherwise masses do not match.
 *
 * @author Roman Zenka
 */
public final class LocalizedModBag extends PersistableBagBase<LocalizedModification> {
	public LocalizedModBag() {
	}

	public LocalizedModBag(final int initialCapacity) {
		super(initialCapacity);
	}

	public LocalizedModBag(final Collection<LocalizedModification> items) {
		super(items);
	}
}
