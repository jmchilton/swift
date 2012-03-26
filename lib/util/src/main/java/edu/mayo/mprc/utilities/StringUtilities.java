package edu.mayo.mprc.utilities;

import com.google.common.base.Strings;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class StringUtilities {
	private static final Pattern LINE_START = Pattern.compile("^", Pattern.MULTILINE);
	private static final char[] HEX_DIGIT = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
	private static final int MAX_HEX_DIGIT = 0xF;

	private StringUtilities() {
	}

	/**
	 * Prepends a tab to every single line in the input text.
	 *
	 * @param text Text to prepend with tabs.
	 * @return Text prepended with tabs.
	 */
	public static String appendTabBeforeLines(final String text) {
		return LINE_START.matcher(text).replaceAll("\t");
	}

	/**
	 * @param c     Character to fill string with.
	 * @param times How many times is the character repeated.
	 * @return String where a given character repeats given amount of times.
	 */
	public static String repeat(final char c, final int times) {
		if (times <= 0) {
			return "";
		}
		return Strings.repeat(String.valueOf(c), times);
	}

	/**
	 * Naive escaping of HTML string to make it render as-is.
	 *
	 * @param html HTML to be rendered as is.
	 * @return Escaped version with &<>" replaced with entities.
	 */
	public static String escapeHtml(final String html) {
		return html.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	/**
	 * Convert first character of the string to uppercase (English locale to stay consistent).
	 */
	public static String firstToUpper(final String s) {
		if (s.length() == 0) {
			return "";
		}
		return String.valueOf(s.charAt(0)).toUpperCase(Locale.ENGLISH) + s.substring(1);
	}

	/**
	 * Convert first character of the string to uppercase, rest of the string to lowercase (English locale to stay consistent).
	 */
	public static String firstToUpperRestToLower(final String s) {
		if (s.length() == 0) {
			return "";
		}
		return String.valueOf(s.charAt(0)).toUpperCase(Locale.ENGLISH) + s.substring(1).toLowerCase(Locale.ENGLISH);
	}

	/**
	 * Like {@link String#startsWith(String)} just using English locale for case-insensitive comparison.
	 */
	public static boolean startsWithIgnoreCase(final String s, final String prefix) {
		return s.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * Like {@link String#endsWith(String)} just using English locale for case-insensitive comparison.
	 */
	public static boolean endsWithIgnoreCase(final String s, final String suffix) {
		return s.toLowerCase(Locale.ENGLISH).endsWith(suffix.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * Like {@link String#contains(CharSequence)} just using English locale for case-insensitive comparison.
	 */
	public static boolean containsIgnoreCase(final String haystack, final String needle) {
		return haystack.toLowerCase(Locale.ENGLISH).contains(needle.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * Escapes unicode characters using the \\uxxxx syntax
	 * Taken from http://www.unix.com.ua/orelly/java-ent/servlet/ch12_06.htm.
	 *
	 * @param str String to escape
	 * @return string with all unicode characters encoded as \\uxxxx
	 */
	public static String toUnicodeEscapeString(final String str) {
		// Modeled after the code in java.util.Properties.save()
		final StringBuilder buf = new StringBuilder();
		final int len = str.length();
		char ch;
		for (int i = 0; i < len; i++) {
			ch = str.charAt(i);
			switch (ch) {
				default:
					if (ch >= ' ' && ch <= 127) {
						buf.append(ch);
					} else {
						charToUnicodeSequence(buf, ch);
					}
			}
		}
		return buf.toString();
	}

	public static void charToUnicodeSequence(final StringBuilder buf, final char ch) {
		buf.append("\\u");
		buf.append(toHex((ch >> 12) & MAX_HEX_DIGIT));
		buf.append(toHex((ch >> 8) & MAX_HEX_DIGIT));
		buf.append(toHex((ch >> 4) & MAX_HEX_DIGIT));
		buf.append(toHex(ch & MAX_HEX_DIGIT));
	}

	public static char toHex(final int nibble) {
		return HEX_DIGIT[(nibble & MAX_HEX_DIGIT)];
	}

	/**
	 * @param data      Data to encode.
	 * @param separator How to separate the bytes in the output
	 * @return The byte array encoded as a string of hex digits separated by given separator.
	 */
	public static String toHex(final byte[] data, final String separator) {
		if (data.length == 0) {
			return "";
		}

		final StringBuilder sb = new StringBuilder(data.length * (separator.length() + 2) - separator.length());

		for (int i = 0; i < data.length; i++) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(toHex((data[i] & 0xF0) >> 4));
			sb.append(toHex(data[i] & 0x0F));
		}

		return sb.toString();
	}

	/**
	 * @return True if the string contains just whitespace or is null.
	 */
	public static boolean stringEmpty(final String s) {
		return s == null || s.trim().length() == 0;
	}

	public static void split(final String line, final char delimiter, final List<String> parts) {
		parts.clear();
		int start = 0;
		while (start <= line.length()) {
			int end = line.indexOf(delimiter, start);
			if (end < 0) {
				end = line.length();
				parts.add(line.substring(start));
			} else {
				parts.add(line.substring(start, end));
			}
			start = end + 1;
		}
	}
}
