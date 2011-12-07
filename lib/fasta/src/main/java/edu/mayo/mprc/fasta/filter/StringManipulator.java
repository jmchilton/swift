package edu.mayo.mprc.fasta.filter;

/**
 * An interface that provides a single routine that takes a String and performs some sort of manipulation on it and
 * returns the manipulated string.
 */
public interface StringManipulator {
	/**
	 * Perform the manipulation on the String and return the manipulated String
	 *
	 * @param toManipulate the String you want to create a manipulation of, will obviously be unchanged
	 * @return the manipulated String
	 */
	String manipulateString(String toManipulate);


	/**
	 * A short (single word?) description of what this manipulator does.  This is needed to insert into the meta data of the
	 * manipulated String to let people know what we are dong
	 *
	 * @return a single String identifying what this does
	 */
	String getDescription();
}
