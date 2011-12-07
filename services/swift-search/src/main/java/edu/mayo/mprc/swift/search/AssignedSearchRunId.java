package edu.mayo.mprc.swift.search;

import edu.mayo.mprc.daemon.progress.ProgressInfo;

/**
 * The search run id assigned by running the search.
 */
public final class AssignedSearchRunId implements ProgressInfo {
	private static final long serialVersionUID = 20080129L;

	private long searchRunId;

	public AssignedSearchRunId(long searchRunId) {
		this.searchRunId = searchRunId;
	}

	public long getSearchRunId() {
		return searchRunId;
	}
}
