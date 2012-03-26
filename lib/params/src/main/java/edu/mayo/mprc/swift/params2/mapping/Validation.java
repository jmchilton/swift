package edu.mayo.mprc.swift.params2.mapping;

import edu.mayo.mprc.swift.params2.ParamName;

public final class Validation {

	private String message;
	private ValidationSeverity severity;
	/**
	 * Can be null for dummy validations.
	 */
	private ParamName param;
	private Object value;
	private Throwable thbl;

	/* TODO This constructor has too many args; should probably break into a smaller set of required values
			and a series of setters.
		  */

	public Validation(final String message, final ValidationSeverity severity, final ParamName param, final Object value, final Throwable thbl) {
		this.message = message;
		this.severity = severity;
		this.param = param;
		this.value = value;
		this.thbl = thbl;
	}

	public ValidationSeverity getSeverity() {
		return severity;
	}

	public String getMessage() {
		return message;
	}

	public ParamName getParam() {
		return param;
	}

	public void setParam(final ParamName param) {
		this.param = param;
	}

	public void setValue(final Object value) {
		this.value = value;
	}

	public Throwable getThrowable() {
		return thbl;
	}

	public String toString() {
		return severity + " converting " + param + ": " + message;
	}

	public Object getValue() {
		return value;
	}

	public Validation shallowCopy() {
		return new Validation(message, severity, param, value, thbl);
	}
}