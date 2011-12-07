package edu.mayo.mprc.common.client;

/**
 * Exception thrown from a GWT service over the wire. Contains serialized stack trace, so the client can display it.
 * Use {@link edu.mayo.mprc.GWTServiceExceptionFactory} to create this exception.
 */
public final class GWTServiceException extends Exception {

	private String stackTrace;
	private static final long serialVersionUID = 20080303L;

	public GWTServiceException() {
		this("", "");
	}

	public GWTServiceException(String message, String stackTrace) {
		super(message);
		this.stackTrace = stackTrace;
	}

	/**
	 * @return Entire stack trace serialized to a large string using {@link Exception#printStackTrace}.
	 */
	public String getStackTraceAsString() {
		return stackTrace;
	}
}
