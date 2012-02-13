package edu.mayo.mprc.searchdb;

import com.google.common.base.Strings;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.StringUtilities;

import java.io.IOException;
import java.io.Writer;

/**
 * Helps reporting the loaded data.
 *
 * @author Roman Zenka
 */
public class Report {
	private Writer w;

	private boolean rowStarted = false;

	public Report(Writer w) {
		this.w = w;
	}

	private static String esc(String s) {
		return StringUtilities.escapeHtml(s);
	}


	public Report write(String s) {
		try {
			w.write(s);
		} catch (IOException e) {
			throw new MprcException("Could not write out data", e);
		}
		return this;
	}

	/**
	 * Start a table with a title on the top.
	 *
	 * @param title Title on the top of the table.
	 */
	public Report startTable(String title) {
		rowStarted = false;
		if (title != null) {
			header(title);
		}
		write("<table>");
		return this;
	}

	public Report nextRow() {
		write("</tr>\n");
		rowStarted = false;
		return this;
	}

	public Report cell(String text) {
		return cell(text, 1);
	}

	public Report cell(String text, int colspan) {
		checkRow();
		if (colspan == 1) {
			write("<td>");
		} else {
			write("<td colspan=\"" + colspan + "\">");
		}
		if (Strings.isNullOrEmpty(text)) {
			write("&nbsp;");
		} else {
			write(esc(text));
		}
		write("</td>\n");
		return this;
	}

	private void checkRow() {
		if (!rowStarted) {
			write("<tr>");
			rowStarted = true;
		}
	}

	public Report hCell(String text) {
		checkRow();
		write("<th>" + esc(text) + "</th>\n");
		return this;
	}


	/**
	 * Add a key-value pair to a table on a separate row.
	 *
	 * @param key   Key, displayed in bold.
	 * @param value Value.
	 */
	public Report addKeyValueTable(String key, Object value) {
		hCell(key);
		cell(value == null ? "<null>" : value.toString());
		nextRow();
		return this;
	}

	/**
	 * Close the table.
	 */
	public Report endTable() {
		write("</table>\n");
		return this;
	}

	public Report header(String text) {
		write("<h2>" + esc(text) + "</h2>\n");
		return this;
	}
}
