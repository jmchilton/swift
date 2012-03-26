package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableListBase;

import java.util.Collection;

/**
 * List of biological samples.
 *
 * @author Roman Zenka
 */
public final class BiologicalSampleList extends PersistableListBase<BiologicalSample> {
	public BiologicalSampleList() {
	}

	public BiologicalSampleList(final int initialCapacity) {
		super(initialCapacity);
	}

	public BiologicalSampleList(final Collection<BiologicalSample> items) {
		super(items);
	}
}
