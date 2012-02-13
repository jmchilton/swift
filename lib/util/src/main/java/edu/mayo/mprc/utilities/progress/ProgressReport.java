package edu.mayo.mprc.utilities.progress;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * Reports how far did a certain worker get.
 * The report contains many values that represent percent done. The values are
 * stored as integers, represeting how many parts out of total are in particular state.
 */
public final class ProgressReport implements Serializable {
	private static final long serialVersionUID = 20071220L;

	private final int total;             // Total tasks. Finished workflow has failed + succeeded = total
	private final int sent;               // Sent but not yet in queue right now
	private final int queued;             // In queue right now
	private final int executing;          // Executing right now
	private final int succeeded;          // Ended successfully
	private final int failed;             // Failed - either by themselves or because they did not start

	private final int initFailed;         // Portion of subtasks that did not even start
	private final double fromExecutingDone;  // Out of all the currently executing tasks, which portion of work is done already

	/**
	 * Generic constructor. Each of the input values is in range [0-1] where 1=100%
	 *
	 * @param total             Amount of subtasks that constitute this task
	 * @param sent              Sent to be run
	 * @param queued            Sitting in a queue, waiting to start executing
	 * @param executing         Being executed right now
	 * @param fromExecutingDone Out of all the currently executing tasks, which portion of work is done already
	 * @param succeeded         Ended successfully
	 * @param failed            Failed - either by themselves or because their initialization failed
	 * @param initFailed        Out of failed how many did not even start (failed because dependency failed)
	 */
	public ProgressReport(int total, int sent, int queued, int executing, double fromExecutingDone, int succeeded, int failed, int initFailed) {
		this.total = total;
		this.sent = sent;
		this.queued = queued;
		this.executing = executing;
		this.fromExecutingDone = fromExecutingDone;
		this.succeeded = succeeded;
		this.failed = failed;
		this.initFailed = initFailed;
	}

	public int getTotal() {
		return total;
	}

	public int getSent() {
		return sent;
	}

	public int getQueued() {
		return queued;
	}

	public int getExecuting() {
		return executing;
	}

	public double getFromExecutingDone() {
		return fromExecutingDone;
	}

	public int getSucceeded() {
		return succeeded;
	}

	public int getFailed() {
		return failed;
	}

	public int getInitFailed() {
		return initFailed;
	}

	public String toString() {
		if (failed > 0) {
			return MessageFormat.format(
					"Progress: Out of {0} subtasks: {1} sent, {2} queued, {3} executing, {4} succeeded, {5} failed (out of which {6} failed because their dependency failed)",
					total, sent, queued, executing, succeeded, failed, initFailed);
		} else {
			return MessageFormat.format(
					"Progress: Out of {0} subtasks: {1} sent, {2} queued, {3} executing, {4} succeeded, none failed",
					total, sent, queued, executing, succeeded);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof ProgressReport) {
			ProgressReport report = (ProgressReport) obj;
			return total == report.getTotal() &&
					sent == report.getSent() &&
					queued == report.getQueued() &&
					executing == report.getExecuting() &&
					fromExecutingDone == report.getFromExecutingDone() &&
					succeeded == report.getSucceeded() &&
					failed == report.getFailed() &&
					initFailed == report.getInitFailed();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return total + sent + queued + executing + succeeded + failed + initFailed + Double.valueOf(fromExecutingDone).hashCode();
	}
}
