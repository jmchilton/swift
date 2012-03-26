package edu.mayo.mprc.common.client;

import java.util.List;

public final class StringUtilities {

	public static final int AVERAGE_STRING_LENGTH = 20;

	/**
	 * Null Constructor
	 */
	private StringUtilities() {
	}

	/**
	 * joins list of strings using a delimiter.
	 *
	 * @param values    - the string to concatenate
	 * @param delimiter - delimiter to use between strings
	 * @return List of values delimeted by delimiter.
	 */
	public static String join(final String[] values, final String delimiter) {
		if (values == null) {
			return "";
		}
		final StringBuilder result = new StringBuilder(values.length * AVERAGE_STRING_LENGTH);
		for (int i = 0; i < values.length; i++) {
			result.append(values[i]);
			if (i < values.length - 1) {
				result.append(delimiter);
			}
		}
		return result.toString();
	}

	/**
	 * joins list of strings using a delimiter.
	 *
	 * @param values    - the string to concatenate
	 * @param delimiter - delimiter to use between strings
	 * @return List of values delimited by delimiter.
	 */
	public static String join(final List<String> values, final String delimiter) {
		if (values == null) {
			return "";
		}
		final StringBuilder result = new StringBuilder(values.size() * AVERAGE_STRING_LENGTH);
		for (int i = 0; i < values.size(); i++) {
			result.append(values.get(i));
			if (i < values.size() - 1) {
				result.append(delimiter);
			}
		}
		return result.toString();
	}

	/**
	 * GWT 1.5.3 does not support Locale in String toLowerCase. This method is used
	 * so all calls to toLowerCase can be replaced with properly localized version.
	 */
	public static String toLowerCase(final String s) {
		return s.toLowerCase();
	}

	/**
	 * GWT 1.5.3 does not support Locale in String toUpperCase. This method is used
	 * so all calls to toUpperCase can be replaced with properly localized version.
	 */
	public static String toUpperCase(final String s) {
		return s.toLowerCase();
	}
}
