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

	public BiologicalSampleList(int initialCapacity) {
		super(initialCapacity);
	}

	public BiologicalSampleList(Collection<BiologicalSample> items) {
		super(items);
	}
}
