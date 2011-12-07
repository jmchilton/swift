package edu.mayo.mprc.messaging;

import java.io.Serializable;

/**
 * Received request. The request contains data as well as a means of sending one (or several) responses to them.
 * The last response has to be marked as closing.
 */
public interface Request {
	/**
	 * @return Data associated with the message.
	 */
	Serializable getMessageData();

	/**
	 * Sends an object response to the sender of the request.
	 * There can be more than one response to a request, the last one has to have the isLast parameter
	 * set to true.
	 *
	 * @param response Data to send back to the original request sender.
	 * @param isLast   True if this is the last response to return.
	 */
	void sendResponse(Serializable response, boolean isLast);

	/**
	 * Notifies that request has been processed.
	 */
	void processed();
}
