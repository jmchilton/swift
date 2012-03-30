package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableSetBase;

import java.util.Collection;

/**
 * Set of biological samples (each biosample can appear just once).
 *
 * @author Roman Zenka
 */
public final class BiologicalSampleList extends PersistableSetBase<BiologicalSample> {
	public BiologicalSampleList() {
	}

	public BiologicalSampleList(final int initialCapacity) {
		super(initialCapacity);
	}

	public BiologicalSampleList(final Collection<BiologicalSample> items) {
		super(items);
	}
}
