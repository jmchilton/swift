package edu.mayo.mprc.swift;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.utilities.FileUtilities;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReportUtils {
	private static final Pattern tokenToHyperlink = Pattern.compile("<file>([^<]*)</file>", Pattern.CASE_INSENSITIVE);

	private ReportUtils() {
	}

	public static String newlineToBr(final String text) {
		return text.replaceAll("(\r\n|\n\r|\n)", "<br/>");
	}

	public static String replaceTokensWithHyperlinks(final String text, final File browseRoot, final String browseWebRoot, final FileTokenFactory tokenFactory) {
		final Matcher matcher = tokenToHyperlink.matcher(text);

		final StringBuffer result = new StringBuffer(text.length());
		while (matcher.find()) {
			final String token = matcher.group(1);
			final File file = tokenFactory.databaseTokenToFile(token);
			final String path = FileTokenFactory.canonicalFilePath(file);
			final String name = file.getName();
			final String relPath = path.substring(FileUtilities.canonicalDirectoryPath(browseRoot).length() - 1);
			matcher.appendReplacement(result, Matcher.quoteReplacement("<a class=\"path\" href=\"" + browseWebRoot + relPath + "\" title=\"" + file.getPath() + "\">" + name + "</a>"));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	/**
	 * Parse date as midnight (the very beginning of) a certain day.
	 * Day is specified by ISO 8601 as yyyy-MM-dd. E.g. 2011-08-26
	 * <p/>
	 * Optionally, time can be added using yyyy-MM-ddTHH:mm:ss format. E.g. 2011-08-26T13:21:45
	 *
	 * @param date  Date to parse
	 * @param field Name of the field - used when throwing exceptions
	 * @return Parsed date corresponding to midnight of the given day.
	 */
	public static DateTime parseDate(final String date, final String field) {
		if (date == null) {
			throw new MprcException("'" + field + "' date not specified");
		}
		try {
			return ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(date);
		} catch (Exception e) {
			throw new MprcException("Cannot parse " + field + " date. Expected yyyy-MM-dd format.", e);
		}
	}

}
