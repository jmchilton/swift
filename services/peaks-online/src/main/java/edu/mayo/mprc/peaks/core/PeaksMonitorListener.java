package edu.mayo.mprc.peaks.core;

import java.util.EventListener;

/**
 * Provides method signature to search result monitor listeners.
 */
public interface PeaksMonitorListener extends EventListener {

	void searchCompleted(PeaksSearchStatusEvent event);

	void searchRunning(PeaksSearchStatusEvent event);

	void searchWaiting(PeaksSearchStatusEvent event);

	void searchNotFound(PeaksSearchStatusEvent event);
}
