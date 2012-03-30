package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableSetBase;

import java.util.Collection;

/**
 * A set of peptide spectrum matches.
 *
 * @author Roman Zenka
 */
public final class PsmList extends PersistableSetBase<PeptideSpectrumMatch> {
	public PsmList() {
	}

	public PsmList(final int initialCapacity) {
		super(initialCapacity);
	}

	public PsmList(final Collection<PeptideSpectrumMatch> items) {
		super(items);
	}
}
