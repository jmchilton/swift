package edu.mayo.mprc.utilities;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

/**
 * Just like BufferedReader, but supports the {@link #isEof()} method to determine if there is more data to be read.
 * The implementation simply keeps two last lines of text to know if one of them is null.
 *
 * @author Roman Zenka
 */
public final class BufferedEofReader implements Closeable {
	private final BufferedReader reader;

	/**
	 * The line we read just to determine if there was an end of file.
	 * If null, and isEof is false - no such determination was done.
	 * If null and isEof is true - we know we are at the end.
	 */
	private String lookahead = null;
	/**
	 * Set to true when we detect end of file
	 */
	private boolean isEof = false;

	/**
	 * Wrap the eof-detecting reader around the buffered reader.
	 * @param reader Reader to wrap around.
	 */
	public BufferedEofReader(BufferedReader reader) {
		this.reader = reader;
	}

	public String readLine() throws IOException {
		final String result;
		if (lookahead != null) {
			result = lookahead;
			lookahead = null;
		} else {
			result = reader.readLine();
		}
		return result;
	}

	/**
	 * @return True if we reached the end of file. Determined by loading extra line of text and checking for null,
	 * unless we know for sure we are not at the end.
	 */
	public boolean isEof() throws IOException {
		if (isEof) {
			return true;
		}
		if (lookahead != null) {
			return false;
		}
		lookahead = reader.readLine();
		if (lookahead == null) {
			isEof = true;
		}
		return isEof;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
}
