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

	public MprcException(String message) {
		super(message);
	}

	public MprcException(String message, Throwable cause) {
		super(message, cause);
	}

	public MprcException(Throwable cause) {
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
	public static String getDetailedMessage(Throwable t) {
		Throwable current = t;
		Throwable previous = null;
		StringBuilder message = new StringBuilder();
		List<Throwable> list = new ArrayList<Throwable>();
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
			Throwable curr = list.get(i);
			assert curr != null : "The list must never contain null exception";
			if (i < list.size() - 1) {
				Throwable next = list.get(i + 1);
				// Compare current to the next throwable. Is the next just wrapping the current, preending the exception class name?
				String nonInformativeWrap = next.getClass().getName() + ": " + next.getMessage();
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

	private static String getThrowableMessage(Throwable throwable) {
		if (throwable == null) {
			return null;
		}
		return throwable.getMessage();
	}

	private static boolean exceptionMessageDiffers(Throwable current, Throwable previous) {

		if (previous == null) {
			return true; // We always differ from initial null.
		}
		String currentMessage = getThrowableMessage(current);

		// Going to null message always means no difference (just a loss of signal)
		return currentMessage != null && (!currentMessage.equalsIgnoreCase(getThrowableMessage(previous)));
	}

}
