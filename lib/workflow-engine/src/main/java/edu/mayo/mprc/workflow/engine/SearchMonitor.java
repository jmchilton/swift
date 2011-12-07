package edu.mayo.mprc.workflow.engine;

import edu.mayo.mprc.daemon.progress.ProgressReport;

/**
 * Monitors the running search and logs/stores the results.
 */
public interface SearchMonitor {
	/**
	 * Update statistics for the entire run.
	 */
	void updateStatistics(ProgressReport report);

	/**
	 * Single task changed its state
	 */
	void taskChange(TaskBase task);

	/**
	 * Error occured within a task.
	 */
	void error(TaskBase task, Throwable t);

	/**
	 * Error occured within the search itself (for example in the workflow engine), not on the task level.
	 */
	void error(Throwable e);

	/**
	 * The task wants to notify the monitor about its progress.
	 *
	 * @param task         The task notifying the monitor.
	 * @param progressInfo Progress information. Can be anything.
	 */
	void taskProgress(TaskBase task, Object progressInfo);
}
