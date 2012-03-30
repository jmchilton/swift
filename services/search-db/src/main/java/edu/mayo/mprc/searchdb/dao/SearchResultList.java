package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableSetBase;

import java.util.Collection;

/**
 * A set of search results.
 *
 * @author Roman Zenka
 */
public final class SearchResultList extends PersistableSetBase<SearchResult> {
	public SearchResultList() {
	}

	public SearchResultList(final int initialCapacity) {
		super(initialCapacity);
	}

	public SearchResultList(final Collection<SearchResult> items) {
		super(items);
	}
}
