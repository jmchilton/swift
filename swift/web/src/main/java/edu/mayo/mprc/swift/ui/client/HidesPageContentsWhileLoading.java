package edu.mayo.mprc.swift.ui.client;

/**
 * This interface, exposed by SwiftApp page, allows the long initalizations run while the page contents are hidden.
 */
public interface HidesPageContentsWhileLoading {
	/**
	 * Call this before any initialization RPC call.
	 */
	void hidePageContentsWhileLoading();

	/**
	 * Call this after success of initialization RPC call.
	 */
	void showPageContentsAfterLoad();

	/**
	 * Call after failure in initialization RPC call, so the page gets fully displayed (for debugging purposes).
	 */
	void showPageContents();
}
