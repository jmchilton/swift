package edu.mayo.mprc.messaging;

/**
 * Listener that is called when a new request arrives.
 */
public interface RequestListener {
	/**
	 * Called when a new request arrives.
	 *
	 * @param request Received request. Use this object to send at least one response confirming that data arrived,
	 *                unless you know that the {@link ResponseListener} producing this message specified no callback.
	 */
	void requestReceived(Request request);
}
