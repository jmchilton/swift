package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableListBase;

import java.util.Collection;

/**
 * A list of peptide spectrum matches.
 *
 * @author Roman Zenka
 */
public final class PsmList extends PersistableListBase<PeptideSpectrumMatch> {
	public PsmList() {
	}

	public PsmList(int initialCapacity) {
		super(initialCapacity);
	}

	public PsmList(Collection<PeptideSpectrumMatch> items) {
		super(items);
	}
}
