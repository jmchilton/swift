package edu.mayo.mprc.fasta;

import java.io.File;
import java.io.IOException;

/**
 * A class that abstracts the FileIO that must go on when dealing with sequence files.  The current implementation will
 * be something like FASTAOutputStream.
 *
 * @author Eric J. Winter Date: Apr 6, 2007 Time: 10:25:43 AM
 */
public interface DBOutputStream {

	/**
	 * Appends a given header and sequence to the output file.  This method may attempt to perform some ironing of the
	 * input if it is determined to need minor changes.
	 *
	 * @param header   the metadata header that describes the sequence
	 * @param sequence the actual sequence that should be written out
	 * @throws IOException if there was a problem performing the output
	 */
	void appendSequence(String header, String sequence) throws IOException;

	/**
	 * Takes a DBInputStream and appends all of its contents to this OutputStream
	 *
	 * @param inputStream the input stream you want to import
	 */
	void appendRemaining(DBInputStream inputStream) throws IOException;

	/**
	 * gets the underlying file
	 *
	 * @return the underlying file
	 */
	File getFile();

	/**
	 * performs any cleaning up that may be necessary.
	 */
	void close();

	/**
	 * gets the number of sequence that have been written through this object
	 *
	 * @return the number of sequence written through this object
	 */
	int getSequenceCount();
}
