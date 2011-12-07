package edu.mayo.mprc.swift.params2.mapping;

import java.util.ArrayList;
import java.util.Collection;

public final class ValidationList extends ArrayList<Validation> {
	private static final long serialVersionUID = 20101221L;


	public ValidationList(int initialCapacity) {
		super(initialCapacity);
	}

	public ValidationList() {
	}

	public ValidationList(Collection<? extends Validation> c) {
		super(c);
	}

	public Validation getLast() {
		return this.get(this.size() - 1);
	}

	public Object getValue() {
		for (Validation v : this) {
			if (v.getValue() != null) {
				return v.getValue();
			}
		}
		return null;
	}

	public ValidationSeverity getWorstSeverity() {
		return getWorstSeverityRec();
	}

	private ValidationSeverity getWorstSeverityRec() {
		ValidationSeverity currentSeverity = ValidationSeverity.NONE;
		for (Validation v : this) {
			if (currentSeverity == null || v.getSeverity().rank > currentSeverity.rank) {
				currentSeverity = v.getSeverity();
			}
		}
		return currentSeverity;
	}

	/**
	 * @return First throwable from the list of errors or null if none was specified.
	 */
	public Object getThrowable() {
		for (Validation v : this) {
			if (v.getThrowable() != null) {
				return v.getThrowable();
			}
		}
		return null;
	}
}
