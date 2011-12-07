package edu.mayo.mprc.dbundeploy;

import java.io.Serializable;

/**
 * Every database undeployment requests is given a task id by the DatabaseUndeployerCaller class.
 * Every progress message from the task processing any given requests is sent back to web client using this class
 * where the databaseUndeployerTaskId is the given task id by the DatabaseUndeployerCaller class and
 * the progress message is the database undeployer task progress message.
 */
public final class DatabaseUndeployerProgress implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private long databaseUndeployerTaskId;
	private String progressMessage;
	private boolean isLast;

	public DatabaseUndeployerProgress(long databaseUndeployerTaskId, String progressMessage, boolean last) {
		this.databaseUndeployerTaskId = databaseUndeployerTaskId;
		this.progressMessage = progressMessage;
		isLast = last;
	}

	public long getDatabaseUndeployerTaskId() {
		return databaseUndeployerTaskId;
	}

	public String getProgressMessage() {
		return progressMessage;
	}

	public boolean isLast() {
		return isLast;
	}
}
