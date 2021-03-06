package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.workflow.engine.TaskBase;
import edu.mayo.mprc.workflow.persistence.TaskState;

import java.util.Date;

/**
 * A task that does a single asynchronous call to a given daemon, then it waits for it to finish (or fail).
 * Since the daemon can send progress information that is actually valuable, the task has to be able to hook into that
 * by overriding a set of methods.
 */
public abstract class AsyncTaskBase extends TaskBase {

	private boolean wasSubmitted;
	protected DaemonConnection daemon;
	private Date taskEnqueued;
	private Date taskProcessingStarted;
	protected FileTokenFactory fileTokenFactory;
	private boolean fromScratch;

	protected AsyncTaskBase(DaemonConnection daemon, FileTokenFactory fileTokenFactory, boolean fromScratch) {
		assert daemon != null : "The daemon has to be set";
		this.daemon = daemon;
		wasSubmitted = false;
		taskEnqueued = null;
		taskProcessingStarted = null;
		this.fileTokenFactory = fileTokenFactory;
		this.fromScratch = fromScratch;
	}

	public Date getTaskEnqueued() {
		return taskEnqueued;
	}

	public void setTaskEnqueued(Date enqueued) {
		this.taskEnqueued = enqueued;
	}

	public Date getTaskProcessingStarted() {
		return taskProcessingStarted;
	}

	public void setTaskProcessingStarted(Date processingStarted) {
		this.taskProcessingStarted = processingStarted;
	}

	public FileTokenFactory getFileTokenFactory() {
		return fileTokenFactory;
	}

	public void setFileTokenFactory(FileTokenFactory fileTokenFactory) {
		this.fileTokenFactory = fileTokenFactory;
	}

	/**
	 * @return Work packet to be sent asynchronously. If it returns null, it means the work was done without a need
	 *         to send a work packet.
	 */
	public abstract WorkPacket createWorkPacket();

	public boolean isFromScratch() {
		return fromScratch;
	}

	/**
	 * @return Will be called until the task fails or succeeds through changing its status. If you do neither,
	 *         you must use the resumer otherwise you might not run again.
	 */
	public void run() {
		if (!wasSubmitted) {
			if (daemon == null) {
				throw new MprcException("The daemon for asynchronous task '" + this.getName() + "' was not set.");
			}
			wasSubmitted = true;
			WorkPacket workPacket = createWorkPacket();
			if (workPacket == null) {
				// We are already done.
				setState(TaskState.COMPLETED_SUCCESFULLY);
				return;
			}
			daemon.sendWork(workPacket, new TaskProgressListener(this) {
				public void requestProcessingFinished() {
					try {
						onSuccess();
						if (!waitingForFileToAppear.get()) {
							setState(TaskState.COMPLETED_SUCCESFULLY);
						}
					} catch (Exception t) {
						this.getTask().setError(t);
					}
				}

				public void userProgressInformation(ProgressInfo progressInfo) {
					try {
						onProgress(progressInfo);
					} catch (Exception t) {
						this.getTask().setError(t);
					}
					super.userProgressInformation(progressInfo);
				}
			});
		}
	}

	public abstract void onSuccess();

	public abstract void onProgress(ProgressInfo progressInfo);
}
