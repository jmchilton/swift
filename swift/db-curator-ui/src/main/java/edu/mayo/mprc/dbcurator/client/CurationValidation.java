package edu.mayo.mprc.dbcurator.client;

/**
 * Helper class for both client and server side validation.
 */
public final class CurationValidation {
	public static final int SHORTNAME_MAX_LENGTH = 17;
	public static final int SHORTNAME_MIN_LENGTH = 5;

	private CurationValidation() {
	}

	/**
	 * Copy of {@link edu.mayo.mprc.dbcurator.model.Curation#validateShortNameLegalCharacters(String)} to be used
	 * on the client.
	 *
	 * @param toValidate Name to validate
	 * @return Error message or null if name is ok
	 */
	public static String validateShortNameLegalCharacters(final String toValidate) {

		if (!toValidate.matches("^[a-zA-Z0-9:_.\\-()]*$")) {
			return "Must not contain anything but a-z A-Z 0-9 : _ . - ( ) (no spaces)";
		} else if (toValidate.length() > SHORTNAME_MAX_LENGTH || toValidate.length() < SHORTNAME_MIN_LENGTH) {
			return "Must be between " + SHORTNAME_MIN_LENGTH + " and " + SHORTNAME_MAX_LENGTH + " characters";
		}
		return null;
	}
}
