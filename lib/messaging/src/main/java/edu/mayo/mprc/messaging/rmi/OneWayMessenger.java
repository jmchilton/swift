package edu.mayo.mprc.messaging.rmi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OneWayMessenger extends Remote, Serializable {
	/**
	 * This method is to called on the remote instance of this object.
	 * Todo: Change method name to better name.
	 *
	 * @param message
	 * @throws java.rmi.RemoteException
	 */
	void sendMessage(Object message) throws RemoteException;
}
