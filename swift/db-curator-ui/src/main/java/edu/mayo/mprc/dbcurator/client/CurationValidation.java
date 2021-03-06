package edu.mayo.mprc.dbcurator.client;

/**
 * Helper class for both client and server side validation.
 */
public final class CurationValidation {
	public static final int SHORTNAME_MAX_LENGTH = 17;
	public static final int SHORTNAME_MIN_LENGTH = 5;

	private CurationValidation() {
	}

	public static String validateShortNameLegalCharacters(String toValidate) {
		final String spacesMessage = "Must not contain anything but a-z A-Z 0-9 : _ . - ( ) (no spaces)";
		final String lengthMessage = "Must be between " + SHORTNAME_MIN_LENGTH + " and " + SHORTNAME_MAX_LENGTH + " characters";

		if (!toValidate.matches("^[a-zA-Z0-9:_.\\-()]*$")) {
			return spacesMessage;
		} else if (toValidate.length() > SHORTNAME_MAX_LENGTH || toValidate.length() < SHORTNAME_MIN_LENGTH) {
			return lengthMessage;
		}
		return null;
	}
}
