package edu.mayo.mprc.io;

/**
 * Can provide a tab separated line of information per input file key.
 */
public interface KeyedTsvReader {
	/**
	 * @return A tab-separated header line (sans the Scan number column) for all columns in the data in proper format
	 */
	public String getHeaderLine();

	/**
	 * @return A sequence of tabs that matches the length of the header-1. Used to output missing information.
	 */
	String getEmptyLine();

	/**
	 * @param key Key to obtain data for.
	 * @return Entire line for a given key, ready to be embedded in a larger tab-separated scan. The line does not
	 *         contain the key. The columns are as specified by {@link #getHeaderLine()}.
	 */
	String getLineForKey(String key);
}
