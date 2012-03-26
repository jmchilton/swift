package edu.mayo.mprc.swift.ui.client.rpc;

import java.io.Serializable;

/**
 * Abbreviated information about a search engine - the only part that the UI cares about
 */
public final class ClientSearchEngine implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private String code;
	private String friendlyName;
	private boolean isOnByDefault;

	public ClientSearchEngine() {
	}

	/**
	 * @param code          Search engine code.
	 * @param friendlyName  The name we display for the user.
	 * @param isOnByDefault If this is true, the search engine's checkboxes will be checked by default.
	 */
	public ClientSearchEngine(final String code, final String friendlyName, final boolean isOnByDefault) {
		this.code = code;
		this.friendlyName = friendlyName;
		this.isOnByDefault = isOnByDefault;
	}

	public String getCode() {
		return code;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public boolean isOnByDefault() {
		return isOnByDefault;
	}
}
