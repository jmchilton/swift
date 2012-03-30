package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableHashedSetBase;

import java.util.Collection;

/**
 * A set of search results.
 *
 * @author Roman Zenka
 */
public final class SearchResultList extends PersistableHashedSetBase<SearchResult> {
	public SearchResultList() {
	}

	public SearchResultList(final int initialCapacity) {
		super(initialCapacity);
	}

	public SearchResultList(final Collection<SearchResult> items) {
		super(items);
	}
}
