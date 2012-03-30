package edu.mayo.mprc.database;

import java.util.Collection;

/**
 * Same as {@link PersistableSetBase} only with a hash code for optimizing equality checks.
 *
 * @author Roman Zenka
 */
public class PersistableHashedSetBase<T extends PersistableBase> extends PersistableSetBase<T> implements HashedCollection<T> {
	private long hash;

	public PersistableHashedSetBase() {
	}

	public PersistableHashedSetBase(int initialCapacity) {
		super(initialCapacity);
	}

	public PersistableHashedSetBase(Collection<T> items) {
		super(items);
	}

	public long getHash() {
		return hash;
	}

	public void setHash(long hash) {
		this.hash = hash;
	}
}
