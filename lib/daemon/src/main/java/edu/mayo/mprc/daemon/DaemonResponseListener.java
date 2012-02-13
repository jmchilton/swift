package edu.mayo.mprc.daemon;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.files.FileTokenHolder;
import edu.mayo.mprc.messaging.ResponseListener;
import edu.mayo.mprc.utilities.progress.ProgressListener;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import java.io.Serializable;

/**
 * Listens to response returned by the daemon and translates it to calls on a given {@link edu.mayo.mprc.utilities.progress.ProgressListener}.
 * TODO: This should get better explanation.
 */
class DaemonResponseListener implements ResponseListener {
	private static final Logger LOGGER = Logger.getLogger(DaemonResponseListener.class);

	private final ProgressListener progressListener;

	private DaemonConnection daemonConnection;

	/**
	 * Reason why the request terminated.
	 */
	private String terminateCause = null;
	private String contextInfo = "(unknown)";

	public DaemonResponseListener(ProgressListener progressListener, String contextInfo, DaemonConnection daemonConnection) {
		this.progressListener = progressListener;
		this.contextInfo = contextInfo;
		this.daemonConnection = daemonConnection;
	}

	public void responseReceived(Serializable response, boolean isLast) {
		try {
			NDC.push(contextInfo);
			processResponse(response);
		} finally {
			NDC.pop();
		}
	}

	/**
	 * Parse the progress message and feed the listener.
	 *
	 * @param response Daemon response to be parsed and fed to the listener. We assume the listener is set.
	 */
	private void processResponse(Serializable response) {
		if (null == progressListener) {
			return;
		}
		if (response instanceof DaemonProgressMessage) {
			processProgressMessage((DaemonProgressMessage) response);
		} else if (response instanceof DaemonException) {
			processException((DaemonException) response);
		} else if (response instanceof Throwable) {
			processException(new DaemonException("Unexpected exception", (Throwable) response));
		} else {
			LOGGER.error("Unknown response received: " + response.toString());
		}
	}

	private void processProgressMessage(DaemonProgressMessage msg) {
		switch (msg.getProgress()) {
			case RequestCompleted:
				requestCompleted(msg);
				break;
			case RequestEnqueued:
				requestEnqueued(msg);
				break;
			case RequestProcessingStarted:
				requestProcessingStarted(msg);
				break;
			case UserSpecificProgressInfo:
				userSpecificProgressInfo(msg);
				break;
			default:
				throw new MprcException("Unsupported daemon progress message type: " + msg.getProgress().name());
		}
	}

	private void requestCompleted(DaemonProgressMessage msg) {
		logProgress(msg, "completed");
		if (terminateCause == null) {
			terminateCause = "REQUEST_COMPLETED " + (msg.getProgressData() == null ? "null" : msg.getProgressData().toString());
			progressListener.requestProcessingFinished();
		} else {
			throw new MprcException("Internal daemon system failure: Daemon response 'REQUEST_COMPLETED' received, after the request was already terminated by '" + terminateCause + "'");
		}
	}

	private void requestEnqueued(DaemonProgressMessage msg) {
		logProgress(msg, "enqueued");
		progressListener.requestEnqueued(msg.getHostString());
	}

	private void requestProcessingStarted(DaemonProgressMessage msg) {
		logProgress(msg, "processing started");
		progressListener.requestProcessingStarted();
	}

	private void userSpecificProgressInfo(DaemonProgressMessage msg) {
		logProgress(msg, "progress info");

		//If response if a FileTokenHolder, set FileTokenFactory.
		if (msg.getProgressData() instanceof FileTokenHolder) {
			FileTokenHolder fileTokenHolder = (FileTokenHolder) msg.getProgressData();
			fileTokenHolder.translateOnReceiver(daemonConnection.getFileTokenFactory(), daemonConnection.getFileTokenFactory());
		}

		progressListener.userProgressInformation(msg.getProgressData());
	}

	private void processException(DaemonException response) {
		LOGGER.debug("Request " + contextInfo + " progress info failed with daemon exception ", response);
		if (terminateCause == null) {
			terminateCause = "Exception " + MprcException.getDetailedMessage(response);
			progressListener.requestTerminated(response);
		} else {
			throw new MprcException("Internal daemon system failure: Daemon response containing exception '" + MprcException.getDetailedMessage((DaemonException) response) + "' received, but the request was already terminated by  '" + terminateCause + "'.");
		}
	}

	private void logProgress(DaemonProgressMessage msg, String status) {
		LOGGER.debug("Request " + contextInfo + " " + status + ": " + (msg.getProgressData() == null ? "null" : msg.getProgressData().toString()));
	}
}
