package edu.mayo.mprc.peaks.core;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class provides functionality to monitor submitted peaks online searches.
 */
public final class PeaksSearchMonitor {

	private static final Logger LOGGER = Logger.getLogger(PeaksSearchMonitor.class);

	private String searchId;
	private PeaksResult peaksOnlineResult;
	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
	private List<PeaksMonitorListener> peaksOnlineMonitorListeners;

	public PeaksSearchMonitor(final String searchId, final PeaksResult peaksOnlineResult) {
		this.searchId = searchId;
		this.peaksOnlineResult = peaksOnlineResult;

		peaksOnlineMonitorListeners = Collections.synchronizedList(new LinkedList<PeaksMonitorListener>());
	}

	public void addPeaksOnlineMonitorListener(final PeaksMonitorListener listener) {
		peaksOnlineMonitorListeners.add(listener);
	}

	public void removePeaksOnlineMonitorListener(final PeaksMonitorListener listner) {
		peaksOnlineMonitorListeners.remove(listner);
	}

	/**
	 * Start this monitor.
	 *
	 * @param intervalsBetweenRuns intervals between runs in milliseconds.
	 */
	public void start(final long intervalsBetweenRuns) {
		stop();
		scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
		scheduledThreadPoolExecutor.scheduleAtFixedRate(new ResultSearcher(), 0, intervalsBetweenRuns, TimeUnit.MILLISECONDS);
	}

	public void stop() {
		if (scheduledThreadPoolExecutor != null && !scheduledThreadPoolExecutor.isShutdown()) {
			scheduledThreadPoolExecutor.shutdownNow();
		}
	}

	/**
	 * Callable that queries peaks online result to search status.
	 */
	private class ResultSearcher implements Runnable {
		public void run() {
			String status = null;

			try {
				status = peaksOnlineResult.getSearchStatus(searchId);
			} catch (IOException e) {
				LOGGER.error("Search results could not be retrieved while querying for search: " + searchId, e);
			}

			(new Thread(new SearchStatusReported(status))).start();
		}
	}

	/**
	 * Class that fires events.
	 */
	private class SearchStatusReported implements Runnable {

		private String status;

		private SearchStatusReported(final String status) {
			this.status = status;
		}

		public void run() {
			synchronized (peaksOnlineMonitorListeners) {
				for (final PeaksMonitorListener listener : peaksOnlineMonitorListeners) {
					if (status != null) {
						if (status.equals(PeaksResult.SEARCH_COMPLETED_STATUS)) {
							listener.searchCompleted(new PeaksSearchStatusEvent(status, searchId));
						} else if (status.equals(PeaksResult.SEARCH_RUNNING_STATUS)) {
							listener.searchRunning(new PeaksSearchStatusEvent(status, searchId));
						} else if (status.equals(PeaksResult.SEARCH_WAITING_STATUS)) {
							listener.searchWaiting(new PeaksSearchStatusEvent(status, searchId));
						}
					} else {
						listener.searchNotFound(new PeaksSearchStatusEvent(status, searchId));
					}
				}
			}
		}
	}
}
