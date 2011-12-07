package edu.mayo.mprc;

import edu.mayo.mprc.common.client.GWTServiceException;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class GWTServiceExceptionFactory {

	private GWTServiceExceptionFactory() {
	}

	public static GWTServiceException createException(String message, Throwable cause) {
		final StringWriter result = new StringWriter();
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(result);
			cause.printStackTrace(printWriter);
			final String detailedMessage = MprcException.getDetailedMessage(cause);
			return new GWTServiceException(message == null ? detailedMessage : message + "<br/>" + detailedMessage, result.toString());
		} finally {
			if (printWriter != null) {
				printWriter.close();
			}
		}
	}
}
