package edu.mayo.mprc.messaging.rmi;

import java.io.Serializable;

public interface MessageListener extends Serializable {
	void messageReceived(Object message);
}
