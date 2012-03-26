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

	public UndeploymentTaskResult(final boolean wasSuccessful, final String outputLogFilePath, final String errorLogFilePath) {
		this.wasSuccessful = wasSuccessful;
		this.errorLogFilePath = errorLogFilePath;
		this.outputLogFilePath = outputLogFilePath;

		messages = new LinkedList<String>();
	}

	public void addMessage(final String message) {
		if (message != null) {
			messages.add(message);
		}
	}

	public void addAllMessage(final Collection<String> messages) {
		if (messages != null) {
			this.messages.addAll(messages);
		}
	}

	public LinkedList<String> getMessages() {
		return messages;
	}

	public void setExecutionError(final Throwable executionError) {
		this.executionError = executionError;
	}

	public Throwable getExecutionError() {
		return executionError;
	}

	public boolean wasSuccessful() {
		return wasSuccessful;
	}

	public void setErrorLogFilePath(final String errorLogFilePath) {
		this.errorLogFilePath = errorLogFilePath;
	}

	public String getErrorLogFilePath() {
		return errorLogFilePath;
	}

	public void setOutputLogFilePath(final String outputLogFilePath) {
		this.outputLogFilePath = outputLogFilePath;
	}

	public String getOutputLogFilePath() {
		return outputLogFilePath;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("Messages:");

		for (final String str : messages) {
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
