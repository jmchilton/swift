package edu.mayo.mprc.workflow.engine;

import edu.mayo.mprc.daemon.progress.ProgressReport;

import java.util.ArrayList;
import java.util.List;

public final class CombinedMonitor implements SearchMonitor {

	private List<SearchMonitor> monitors = null;

	public CombinedMonitor() {
		this.monitors = new ArrayList<SearchMonitor>();
	}

	public CombinedMonitor(List<SearchMonitor> monitors) {
		this.monitors = monitors;
	}

	public void addMonitor(SearchMonitor monitor) {
		this.monitors.add(monitor);
	}

	public void updateStatistics(ProgressReport report) {
		for (SearchMonitor monitor : monitors) {
			monitor.updateStatistics(report);
		}
	}

	public void taskChange(TaskBase task) {
		for (SearchMonitor monitor : monitors) {
			monitor.taskChange(task);
		}
	}

	public void error(TaskBase task, Throwable t) {
		for (SearchMonitor monitor : monitors) {
			monitor.error(task, t);
		}
	}

	/**
	 * Task progress information arrived
	 */
	public void taskProgress(TaskBase task, Object progressInfo) {
		for (SearchMonitor monitor : monitors) {
			monitor.taskProgress(task, progressInfo);
		}
	}

	public List<SearchMonitor> getMonitors() {
		return monitors;
	}

	public void error(Throwable e) {
		for (SearchMonitor monitor : monitors) {
			monitor.error(e);
		}
	}
}
