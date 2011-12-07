package edu.mayo.mprc.swift.search;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.AssignedTaskData;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.daemon.progress.ProgressListener;
import edu.mayo.mprc.swift.dbmapping.SearchRun;
import org.apache.log4j.Logger;

/**
 * Used to send a socket request for a search run to {@link SwiftSearcher}
 */
public final class SwiftSearcherCaller {
	private static final Logger LOGGER = Logger.getLogger(SwiftSearcherCaller.class);

	private SwiftSearcherCaller() {
	}

	/**
	 * used to resubmit a transaction that has been run before
	 *
	 * @param td                         - Transaction to resubmit
	 * @param dispatcherDaemonConnection Daemon connection to the dispatcher.
	 * @param listener                   Progress listener for this submission.
	 */
	public static void resubmitSearchRun(SearchRun td, DaemonConnection dispatcherDaemonConnection, ProgressListener listener) {
		try {
			String sBatchName = td.getTitle();
			sendCallToDispatcher(dispatcherDaemonConnection, td.getSwiftSearch(), sBatchName, false, td.getId(), listener);
		} catch (Exception t) {
			throw new MprcException("resubmitSearchRun : failure sending call to dispatcher, " + t.getMessage(), t);
		}
	}

	public static SearchProgressListener startSearch(int swiftSearchId, String batchName, boolean fromScratch, int previousSearchId, DaemonConnection dispatcherDaemonConnection) {
		final SearchProgressListener listener = new SearchProgressListener();
		sendNewSearchToDispatcher(swiftSearchId, batchName, fromScratch, previousSearchId, dispatcherDaemonConnection, listener);
		return listener;
	}

	public static void sendNewSearchToDispatcher(int swiftSearchId, String batchName, boolean fromScratch, int previousSearchId, DaemonConnection dispatcherDaemonConnection, ProgressListener listener) {
		sendCallToDispatcher(dispatcherDaemonConnection, swiftSearchId, batchName, fromScratch, previousSearchId, listener);
	}

	private static void sendCallToDispatcher(DaemonConnection connection, Integer swiftSearchId, String sBatchName, boolean fromScratch, int previousSearchId, ProgressListener listener) {
		// Send work. We are not interested in progress at all, but we must specify progress listener
		connection.sendWork(new SwiftSearchWorkPacket(swiftSearchId, sBatchName, fromScratch, previousSearchId), listener);
	}

	public static class SearchProgressListener implements ProgressListener {
		private MprcException exception;
		private long searchId;
		private boolean running = true;
		private volatile boolean ready;
		private final Object monitor = new Object();

		SearchProgressListener() {
			exception = null;
		}

		/**
		 * Synchronization object. This object gets notifications every time the search progresses.
		 *
		 * @return Synchronization monitor for the search.
		 */
		public Object getMonitor() {
			return monitor;
		}

		public boolean isSuccessful() {
			return !isRunning() && getException() == null;
		}

		public boolean isRunning() {
			synchronized (monitor) {
				return running;
			}
		}

		public long getSearchRunId() {
			synchronized (monitor) {
				return searchId;
			}
		}

		public MprcException getException() {
			synchronized (monitor) {
				return exception;
			}
		}

		public void requestEnqueued(String hostString) {
			LOGGER.debug("Request enqueued " + hostString);
		}

		public void requestProcessingStarted() {
			LOGGER.debug("Request processing started");
		}

		public void waitForSearchReady(long timeout) throws InterruptedException {
			synchronized (monitor) {
				while (!ready) {
					monitor.wait(timeout);
				}
			}
		}

		public void requestProcessingFinished() {
			LOGGER.debug("Request processing finished successfully");
			synchronized (monitor) {
				ready = true;
				running = false;
				monitor.notifyAll();
			}
		}

		public void requestTerminated(DaemonException e) {
			LOGGER.debug("Request terminated with error", e);
			synchronized (monitor) {
				ready = true;
				running = false;
				this.exception = e;
				monitor.notifyAll();
			}
		}

		public void userProgressInformation(ProgressInfo progressInfo) {
			synchronized (monitor) {
				if (progressInfo instanceof AssignedSearchRunId) {
					searchId = ((AssignedSearchRunId) progressInfo).getSearchRunId();
				}

				//First ProgressInfo object must be the AssignedTaskData
				if (progressInfo instanceof AssignedTaskData) {
					return;
				}

				ready = true;
				monitor.notifyAll();
			}
		}
	}
}
