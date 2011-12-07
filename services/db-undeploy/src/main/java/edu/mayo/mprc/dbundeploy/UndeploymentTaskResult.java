package edu.mayo.mprc.dbundeploy;

import edu.mayo.mprc.MprcException;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Contains information of executed database undeployment task.
 */
public final class UndeploymentTaskResult implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private Throwable executionError;
	private boolean wasSuccessful;
	private String errorLogFilePath;
	private String outputLogFilePath;
	private LinkedList<String> messages;

	public UndeploymentTaskResult(boolean wasSuccessful, String outputLogFilePath, String errorLogFilePath) {
		this.wasSuccessful = wasSuccessful;
		this.errorLogFilePath = errorLogFilePath;
		this.outputLogFilePath = outputLogFilePath;

		messages = new LinkedList<String>();
	}

	public void addMessage(String message) {
		if (message != null) {
			messages.add(message);
		}
	}

	public void addAllMessage(Collection<String> messages) {
		if (messages != null) {
			this.messages.addAll(messages);
		}
	}

	public LinkedList<String> getMessages() {
		return messages;
	}

	public void setExecutionError(Throwable executionError) {
		this.executionError = executionError;
	}

	public Throwable getExecutionError() {
		return executionError;
	}

	public boolean wasSuccessful() {
		return wasSuccessful;
	}

	public void setErrorLogFilePath(String errorLogFilePath) {
		this.errorLogFilePath = errorLogFilePath;
	}

	public String getErrorLogFilePath() {
		return errorLogFilePath;
	}

	public void setOutputLogFilePath(String outputLogFilePath) {
		this.outputLogFilePath = outputLogFilePath;
	}

	public String getOutputLogFilePath() {
		return outputLogFilePath;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("Messages:");

		for (String str : messages) {
			builder.append("\n").append(str);
		}

		if (outputLogFilePath == null) {
			builder.append("\nOutput log file path:\n").append(outputLogFilePath);
		}

		if (errorLogFilePath == null) {
			builder.append("\nError log file path:\n").append(errorLogFilePath);
		}

		if (!wasSuccessful) {
			builder.append("\nExecution error:\n").append(MprcException.getDetailedMessage(executionError));
		}

		return builder.toString();
	}
}
