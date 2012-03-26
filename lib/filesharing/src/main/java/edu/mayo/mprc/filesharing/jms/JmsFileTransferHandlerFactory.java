package edu.mayo.mprc.filesharing.jms;

import java.net.URI;

public final class JmsFileTransferHandlerFactory {

	private URI brokerUri;
	private String userName;
	private String password;

	public JmsFileTransferHandlerFactory() {
	}

	public JmsFileTransferHandlerFactory(final URI brokerUri) {
		this.brokerUri = brokerUri;
	}

	public JmsFileTransferHandlerFactory(final URI brokerUri, final String userName, final String password) {
		this.brokerUri = brokerUri;
		this.userName = userName;
		this.password = password;
	}

	public URI getBrokerUri() {
		return brokerUri;
	}

	public void setBrokerUri(final URI brokerUri) {
		this.brokerUri = brokerUri;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public JmsFileTransferHandler createFileSharing(final String sourceId) {
		return new JmsFileTransferHandler(brokerUri, sourceId, userName, password);
	}
}
