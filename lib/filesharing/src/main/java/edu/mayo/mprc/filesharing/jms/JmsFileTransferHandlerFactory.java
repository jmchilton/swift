package edu.mayo.mprc.filesharing.jms;

import java.net.URI;

public final class JmsFileTransferHandlerFactory {

	private URI brokerUri;
	private String userName;
	private String password;

	public JmsFileTransferHandlerFactory() {
	}

	public JmsFileTransferHandlerFactory(URI brokerUri) {
		this.brokerUri = brokerUri;
	}

	public JmsFileTransferHandlerFactory(URI brokerUri, String userName, String password) {
		this.brokerUri = brokerUri;
		this.userName = userName;
		this.password = password;
	}

	public URI getBrokerUri() {
		return brokerUri;
	}

	public void setBrokerUri(URI brokerUri) {
		this.brokerUri = brokerUri;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public JmsFileTransferHandler createFileSharing(String sourceId) {
		return new JmsFileTransferHandler(brokerUri, sourceId, userName, password);
	}
}
