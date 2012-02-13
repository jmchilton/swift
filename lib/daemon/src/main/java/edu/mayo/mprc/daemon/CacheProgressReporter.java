package edu.mayo.mprc.daemon;

import edu.mayo.mprc.utilities.progress.ProgressInfo;
import edu.mayo.mprc.utilities.progress.ProgressReporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds progress reporters for a particular work in progress.
 * Every new notification is distributed to all current reporters.
 * The newcomers get immediately notified of the past events (abridged form).
 */
final class CacheProgressReporter implements ProgressReporter {
	private boolean startReported;
	private ProgressInfo lastProgressInfo;
	private boolean successReported;
	private Throwable failureReported;
	private List<ProgressReporter> reporters = new ArrayList<ProgressReporter>(1);

	public CacheProgressReporter() {
	}

	public synchronized void addProgressReporter(ProgressReporter reporter) {
		reporters.add(reporter);
		if (startReported) {
			reporter.reportStart();
		}
		if (lastProgressInfo != null) {
			reporter.reportProgress(lastProgressInfo);
		}
		if (successReported) {
			reporter.reportSuccess();
		}
		if (failureReported != null) {
			reporter.reportFailure(failureReported);
		}
	}

	@Override
	public synchronized void reportStart() {
		startReported = true;
		for (ProgressReporter reporter : reporters) {
			reporter.reportStart();
		}
	}

	@Override
	public synchronized void reportProgress(ProgressInfo progressInfo) {
		lastProgressInfo = progressInfo;
		for (ProgressReporter reporter : reporters) {
			reporter.reportProgress(progressInfo);
		}
	}

	@Override
	public synchronized void reportSuccess() {
		successReported = true;
		for (ProgressReporter reporter : reporters) {
			reporter.reportSuccess();
		}
	}

	@Override
	public synchronized void reportFailure(Throwable t) {
		failureReported = t;
		for (ProgressReporter reporter : reporters) {
			reporter.reportFailure(failureReported);
		}
	}
}
