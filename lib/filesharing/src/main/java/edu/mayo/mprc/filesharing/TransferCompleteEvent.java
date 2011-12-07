package edu.mayo.mprc.filesharing;

import java.io.File;

/**
 * Event represent file transfer completion. If file transfer operation
 * is successful, exception is null. Otherwise, exception object should
 * reference error exception.
 */
public final class TransferCompleteEvent {

	private Exception exception;
	private File file;

	public TransferCompleteEvent(File file, Exception exception) {
		this.exception = exception;
		this.file = file;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
