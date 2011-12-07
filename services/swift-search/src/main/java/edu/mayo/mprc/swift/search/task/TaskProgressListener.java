package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.daemon.progress.ProgressListener;
import edu.mayo.mprc.workflow.engine.AssignedExecutedOnHost;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import java.util.Date;

abstract class TaskProgressListener implements ProgressListener {
	private static final Logger LOGGER = Logger.getLogger(TaskProgressListener.class);

	private AsyncTaskBase task;

	protected TaskProgressListener(AsyncTaskBase task) {
		this.task = task;
	}

	public void requestEnqueued(String hostString) {
		task.setTaskEnqueued(new Date());
		task.setExecutedOnHost(hostString);
		task.afterProgressInformationReceived(new AssignedExecutedOnHost(hostString));
	}

	public void requestProcessingStarted() {
		task.setTaskProcessingStarted(new Date());
		task.afterProgressInformationReceived(null);
	}

	public void requestTerminated(DaemonException e) {
		try {
			NDC.push(this.task.getFullId());
			LOGGER.error("Task failed: " + this.task.getName(), e);
			task.setError(e);
		} finally {
			NDC.pop();
		}
	}

	public void userProgressInformation(ProgressInfo progressInfo) {
		task.afterProgressInformationReceived(progressInfo);
	}

	public AsyncTaskBase getTask() {
		return task;
	}
}
