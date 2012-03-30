package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableHashedSetBase;

import java.util.Collection;

/**
 * A set of peptide spectrum matches.
 *
 * @author Roman Zenka
 */
public final class PsmList extends PersistableHashedSetBase<PeptideSpectrumMatch> {
	public PsmList() {
	}

	public PsmList(final int initialCapacity) {
		super(initialCapacity);
	}

	public PsmList(final Collection<PeptideSpectrumMatch> items) {
		super(items);
	}
}
