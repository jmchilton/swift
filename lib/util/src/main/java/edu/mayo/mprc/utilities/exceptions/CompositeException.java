package edu.mayo.mprc.utilities.exceptions;

import edu.mayo.mprc.MprcException;
import org.apache.log4j.Logger;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Eric Winter
 */
public final class CompositeException extends MprcException {
	private static final long serialVersionUID = 20071220L;
	private static final Logger LOGGER = Logger.getLogger(CompositeException.class);

	private List<Throwable> causes = new ArrayList<Throwable>(2);

	public CompositeException() {
		super();
	}

	public CompositeException(Exception cause) {
		super(cause);
	}

	public CompositeException(String message) {
		super(message);
	}

	public CompositeException(String message, Exception cause) {
		super(message, cause);
	}

	public CompositeException(Collection<Exception> causes) {
		this.causes.addAll(causes);
	}

	public void addCauses(Collection<Exception> causes) {
		this.causes.addAll(causes);
	}

	public void addCause(Throwable cause) {
		this.causes.add(cause);
	}

	public Collection<Throwable> getCauses() {
		return this.causes;
	}

	public String getMainMessage() {
		if (super.getMessage() != null) {
			return super.getMessage();
		} else {
			return "Composite exception";
		}
	}

	@Override
	public String toString() {
		return getMessage();
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append(getMainMessage()).append(":");
		int num = 1;
		for (Throwable t : this.causes) {
			sb.append("\n").append(num).append(") ").append(MprcException.getDetailedMessage(t));
			num++;
		}
		return sb.toString();
	}

	@Override
	public void printStackTrace() {
		for (Throwable t : this.causes) {
			t.printStackTrace();
		}
	}

	@Override
	public void printStackTrace(PrintWriter p) {
		int i = 1;
		final int total = this.causes.size();
		for (Throwable t : this.causes) {
			LOGGER.error(getMainMessage() + " (" + i + "/" + total + ")", t);
			i++;
		}
	}

	@Override
	public void printStackTrace(PrintStream s) {
		for (Throwable t : this.causes) {
			t.printStackTrace(s);
		}
	}

}
