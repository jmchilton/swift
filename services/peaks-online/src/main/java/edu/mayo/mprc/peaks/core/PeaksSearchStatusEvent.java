package edu.mayo.mprc.peaks.core;

/**
 * Event class describing a search status.
 */
public final class PeaksSearchStatusEvent {

	private String status;
	private String searchId;

	public PeaksSearchStatusEvent(final String status, final String searchId) {
		this.status = status;
		this.searchId = searchId;
	}

	public String getStatus() {
		return status;
	}

	public String getSearchId() {
		return searchId;
	}
}
