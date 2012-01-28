package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableListBase;

import java.util.Collection;

/**
 * A list of search results.
 *
 * @author Roman Zenka
 */
public final class SearchResultList extends PersistableListBase<SearchResult> {
	public SearchResultList() {
	}

	public SearchResultList(Collection<SearchResult> items) {
		super(items);
	}
}
