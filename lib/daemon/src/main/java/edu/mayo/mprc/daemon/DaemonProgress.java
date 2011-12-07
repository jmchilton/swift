package edu.mayo.mprc.daemon;

/**
 * Request progress steps.
 */
public enum DaemonProgress {
	/**
	 * The request was received and enqueued by the daemon.
	 */
	RequestEnqueued,

	/**
	 * The daemon started to work on the request.
	 */
	RequestProcessingStarted,

	/**
	 * Daemon completed work on the request.
	 */
	RequestCompleted,

	/**
	 * Daemon worker sends user-specific progress information.
	 */
	UserSpecificProgressInfo,
}
