package edu.mayo.mprc.utilities.progress;

/**
 * Interface allowing the user to report progress of a {@link edu.mayo.mprc.daemon.Worker}.
 * <p/>
 * Use this interface separately when you only want to give your method the
 * progress reporting capabilities, nothing else.
 */
public interface ProgressReporter {
	/**
	 * Reports that the worker has started processing. This is implemented so e.g. a worker cache can
	 * postpone this report until it hears from its child worker.
	 */
	void reportStart();

	/**
	 * Reports progress of the worker. The progress can be anything serializable, for instance an Integer containing
	 * amount of percent. The grid engine daemon implementation for instance sends back the assigned grid engine number as a specific
	 * progress report.
	 *
	 * @param progressInfo Information about the progress, e.g. an Integer containing the amount of percent done.
	 */
	void reportProgress(ProgressInfo progressInfo);

	/**
	 * Reports success. There must be no more reports after this one.
	 * The method is guaranteed to never throw an exception.
	 */
	void reportSuccess();

	/**
	 * Reports failure that leads to termination of the worker. There must be no reports after this one.
	 * The method is guaranteed to never throw an exception.
	 *
	 * @param t Exception the worker ended with.
	 */
	void reportFailure(Throwable t);
}
