package edu.mayo.mprc.filesharing;

/**
 * Super class for
 */
public abstract class FileTransferThread extends Thread {

	protected TransferCompleteListener listener;

	protected FileTransferThread(final String name) {
		super(name);
	}

	public TransferCompleteListener getTransferCompleteListener() {
		return listener;
	}

	public void setTransferCompleteListener(final TransferCompleteListener listener) {
		this.listener = listener;
	}
}
