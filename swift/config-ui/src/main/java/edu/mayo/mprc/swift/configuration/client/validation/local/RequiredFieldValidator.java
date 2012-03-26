package edu.mayo.mprc.swift.configuration.client.validation.local;

public final class RequiredFieldValidator implements Validator {

	public String validate(final String value) {
		if (value == null || value.trim().length() == 0) {
			return "Required field, please enter a value.";
		}
		return null;
	}
}
