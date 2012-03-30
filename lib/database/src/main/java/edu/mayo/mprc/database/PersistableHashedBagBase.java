package edu.mayo.mprc.database;

import java.util.Collection;

/**
 * Same as {@link PersistableSetBase} only with a hash code for optimizing equality checks.
 *
 * @author Roman Zenka
 */
public class PersistableHashedBagBase<T extends PersistableBase> extends PersistableBagBase<T> implements HashedCollection<T> {
	private long hash;

	public PersistableHashedBagBase() {
	}

	public PersistableHashedBagBase(int initialCapacity) {
		super(initialCapacity);
	}

	public PersistableHashedBagBase(Collection<T> items) {
		super(items);
	}

	public long getHash() {
		return hash;
	}

	public void setHash(long hash) {
		this.hash = hash;
	}
}
