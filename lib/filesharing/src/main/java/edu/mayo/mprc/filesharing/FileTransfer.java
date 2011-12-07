package edu.mayo.mprc.filesharing;

import edu.mayo.mprc.MprcException;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class that represent a file transfer being process.
 */
public final class FileTransfer {

	private static final Logger LOGGER = Logger.getLogger(FileTransfer.class);

	private List<File> files;
	private List<File> transferedFiles;
	private Exception errorException;
	private boolean done;

	private final Object monitor = new Object();

	public FileTransfer() {
		transferedFiles = new LinkedList<File>();
	}

	public FileTransfer(List<File> files) {
		this();
		this.files = files;
	}

	public FileTransfer(File file) {
		this();
		files = new LinkedList<File>();
		files.add(file);
	}

	/**
	 * Method blocks until the file transfer is completed.
	 *
	 * @return transferred file.
	 */
	public List<File> done() {
		synchronized (monitor) {
			while (true) {
				try {
					if (!done) {
						monitor.wait();
					} else {
						break;
					}
				} catch (InterruptedException e) {
					LOGGER.error("Interrupted while waiting for processing of files " + files + ". Resuming wait.", e);
				}
			}
		}

		if (getErrorException() != null) {
			throw new MprcException("File transfer failed", getErrorException());
		}
		return files;
	}

	public void setDone() {
		synchronized (monitor) {
			done = true;
			monitor.notifyAll();
		}
	}

	/**
	 * Returns tru if transfer is completed.
	 *
	 * @return
	 */
	public boolean isDone() {
		return done;
	}

	public Collection<File> getFiles() {
		return files;
	}

	public List<File> getTransferedFiles() {
		return transferedFiles;
	}

	public void setTransferedFiles(List<File> transferedFiles) {
		this.transferedFiles = transferedFiles;
	}

	/**
	 * Returns Throwable object if file transfer fails.
	 *
	 * @return
	 */
	public Exception getErrorException() {
		return errorException;
	}

	public void setErrorException(Exception errorException) {
		this.errorException = errorException;
	}
}
