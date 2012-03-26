package edu.mayo.mprc.swift.ui.client.rpc;

import java.io.Serializable;

/**
 * Wraps values from {@link edu.mayo.mprc.swift.core.database.DatabaseUndeployerProgress}.
 */
public final class ClientDatabaseUndeployerProgress implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private long databaseUndeployerTaskId;
	private String progressMessage;
	private boolean isLast;

	public ClientDatabaseUndeployerProgress(final long databaseUndeployerTaskId, final String progressMessage, final boolean last) {
		this.databaseUndeployerTaskId = databaseUndeployerTaskId;
		this.progressMessage = progressMessage;
		isLast = last;
	}

	public ClientDatabaseUndeployerProgress() {
	}

	public long getDatabaseUndeployerTaskId() {
		return databaseUndeployerTaskId;
	}

	public void setDatabaseUndeployerTaskId(final long databaseUndeployerTaskId) {
		this.databaseUndeployerTaskId = databaseUndeployerTaskId;
	}

	public String getProgressMessage() {
		return progressMessage;
	}

	public void setProgressMessage(final String progressMessage) {
		this.progressMessage = progressMessage;
	}

	public boolean isLast() {
		return isLast;
	}

	public void setLast(final boolean last) {
		isLast = last;
	}
}
