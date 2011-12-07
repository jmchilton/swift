package edu.mayo.mprc.daemon;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.files.FileTokenHolder;
import edu.mayo.mprc.daemon.progress.ProgressListener;
import edu.mayo.mprc.messaging.Request;
import edu.mayo.mprc.messaging.Service;

import java.io.Serializable;

/**
 * Wrapper around a Service that represents a connection to a daemon.
 * Takes care of request numbering which aids logging.
 * <p/>
 * After you send work request, you get automatically notified when the work starts being processed, finishes successfully or
 * when an exception occurs.
 * <p/>
 * Daemon connection takes care of proper file transfers. It is equipped with a file token factory that can
 * translate file references to URLs and then transfer files when needed.
 * <p/>
 * The direct daemon connection means there is only one daemon this connection points to. There could be
 * round robin connections or failover connections implemented as well.
 */
final class DirectDaemonConnection implements DaemonConnection {
	private Service service = null;
	private static int listenerNumber = 0;
	private FileTokenFactory fileTokenFactory;

	public DirectDaemonConnection(Service service, FileTokenFactory fileTokenFactory) {
		if (service == null) {
			throw new MprcException("The service must not be null");
		}
		this.service = service;
		this.fileTokenFactory = fileTokenFactory;
	}

	@Override
	public FileTokenFactory getFileTokenFactory() {
		return fileTokenFactory;
	}

	@Override
	public String getConnectionName() {
		return service.getName();
	}

	@Override
	public void sendWork(WorkPacket workPacket, ProgressListener listener) {
		workPacket.translateOnSender(fileTokenFactory);

		try {
			listenerNumber++;
			service.sendRequest(workPacket, new DaemonResponseListener(listener, "R#" + String.valueOf(listenerNumber), this));
		} catch (MprcException e) {
			// SWALLOWED: The exception is reported directly to the listener
			listener.requestTerminated(new DaemonException(e));
		}
	}

	@Override
	public DaemonRequest receiveDaemonRequest(long timeout) {
		final Request request = service.receiveRequest(timeout);
		if (request != null) {
			return new MyDaemonRequest(request, fileTokenFactory);
		}
		return null;
	}

	@Override
	public void close() {
		service.stopReceiving();
	}

	private static final class MyDaemonRequest implements DaemonRequest {
		private Request request;

		private MyDaemonRequest(Request request, FileTokenFactory fileTokenFactory) {
			this.request = request;

			if (request.getMessageData() instanceof FileTokenHolder) {
				FileTokenHolder fileTokenHolder = (FileTokenHolder) request.getMessageData();
				fileTokenHolder.translateOnReceiver(fileTokenFactory, fileTokenFactory);
			}
		}

		@Override
		public WorkPacket getWorkPacket() {
			return (WorkPacket) request.getMessageData();
		}

		@Override
		public void sendResponse(Serializable response, boolean isLast) {
			request.sendResponse(response, isLast);

			if (isLast) {
				processed();
			}
		}

		@Override
		public void processed() {
			request.processed();
		}
	}
}
