package edu.mayo.mprc.workflow.engine;

import edu.mayo.mprc.utilities.progress.ProgressReport;

import java.util.ArrayList;
import java.util.List;

public final class CombinedMonitor implements SearchMonitor {

	private List<SearchMonitor> monitors = null;

	public CombinedMonitor() {
		this.monitors = new ArrayList<SearchMonitor>();
	}

	public CombinedMonitor(final List<SearchMonitor> monitors) {
		this.monitors = monitors;
	}

	public void addMonitor(final SearchMonitor monitor) {
		this.monitors.add(monitor);
	}

	public void updateStatistics(final ProgressReport report) {
		for (final SearchMonitor monitor : monitors) {
			monitor.updateStatistics(report);
		}
	}

	public void taskChange(final TaskBase task) {
		for (final SearchMonitor monitor : monitors) {
			monitor.taskChange(task);
		}
	}

	public void error(final TaskBase task, final Throwable t) {
		for (final SearchMonitor monitor : monitors) {
			monitor.error(task, t);
		}
	}

	/**
	 * Task progress information arrived
	 */
	public void taskProgress(final TaskBase task, final Object progressInfo) {
		for (final SearchMonitor monitor : monitors) {
			monitor.taskProgress(task, progressInfo);
		}
	}

	public List<SearchMonitor> getMonitors() {
		return monitors;
	}

	public void error(final Throwable e) {
		for (final SearchMonitor monitor : monitors) {
			monitor.error(e);
		}
	}
}
