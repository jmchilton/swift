package edu.mayo.mprc.messaging;

import java.io.Serializable;

/**
 * Every response to the request gets transferred to this listener. The last response is marked in a special way to
 * indicate no more data will be transferred.
 */
public interface ResponseListener {

	/**
	 * Called when the receiver of a message responds. The response
	 * either contains data or an exception in case of error.
	 * There can be more than one response. The last response has the isLast parameter set.
	 *
	 * @param response Response to the sent message.
	 * @param isLast   True if this is the last response to the particular message.
	 */
	void responseReceived(Serializable response, boolean isLast);
}
