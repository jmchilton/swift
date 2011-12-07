package edu.mayo.mprc.messaging.rmi;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Class holder of Messenger object information and bound to registry information.
 */
public final class MessengerInfo implements Serializable {
	private static final long serialVersionUID = 20090324L;
	private InetSocketAddress registryInfo;
	private String messengerRemoteName;

	public MessengerInfo(InetSocketAddress registryInfo, String messengerRemoteName) {
		this.registryInfo = registryInfo;
		this.messengerRemoteName = messengerRemoteName;
	}

	public InetSocketAddress getRegistryInfo() {
		return registryInfo;
	}

	public String getMessengerRemoteName() {
		return messengerRemoteName;
	}
}
