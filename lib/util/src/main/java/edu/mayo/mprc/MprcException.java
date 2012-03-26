package edu.mayo.mprc;

import java.util.ArrayList;
import java.util.List;

/**
 * Central exception to distinguish our exceptions from the outside ones.
 * Everybody should use this, unless they have extra needs.
 */
public class MprcException extends RuntimeException {
	private static final long serialVersionUID = 20071220L;

	public MprcException() {
		super("");
	}

	public MprcException(final String message) {
		super(message);
	}

	public MprcException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public MprcException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Returns a detailed message. {@link #getMessage} gets called for this exception and all its causes,
	 * results concatenated into one string. Duplicate messages in the output are discarded. The result can be used in the UI to
	 * provide some detail, yet not burden the user with a full stack trace.
	 *
	 * @param t Exception to extract detailed message from.
	 * @return Concantenated {@link #getMessage()} for this exception and all its causes, starting with this exception.
	 */
	public static String getDetailedMessage(final Throwable t) {
		Throwable current = t;
		Throwable previous = null;
		final StringBuilder message = new StringBuilder();
		final List<Throwable> list = new ArrayList<Throwable>();
		while (current != null && !current.equals(previous)) {
			if (exceptionMessageDiffers(current, previous)) {
				list.add(current);
			}
			previous = current;
			current = current.getCause();
		}

		// Let's go from the innermost to outermost and skip the trivial messages.
		// Trivial message happens when you wrap
		for (int i = 0; i < list.size(); i++) {
			final Throwable curr = list.get(i);
			assert curr != null : "The list must never contain null exception";
			if (i < list.size() - 1) {
				final Throwable next = list.get(i + 1);
				// Compare current to the next throwable. Is the next just wrapping the current, preending the exception class name?
				final String nonInformativeWrap = next.getClass().getName() + ": " + next.getMessage();
				if (curr.getMessage().equalsIgnoreCase(nonInformativeWrap)) {
					continue;
				}
			}
			if (i > 0) {
				message.append(" - ");
			}
			if (curr instanceof NullPointerException) {
				message.append("Null pointer exception");
			} else {
				message.append(curr.getMessage());
			}
		}
		return message.toString();
	}

	private static String getThrowableMessage(final Throwable throwable) {
		if (throwable == null) {
			return null;
		}
		return throwable.getMessage();
	}

	private static boolean exceptionMessageDiffers(final Throwable current, final Throwable previous) {

		if (previous == null) {
			return true; // We always differ from initial null.
		}
		final String currentMessage = getThrowableMessage(current);

		// Going to null message always means no difference (just a loss of signal)
		return currentMessage != null && (!currentMessage.equalsIgnoreCase(getThrowableMessage(previous)));
	}

}
