package edu.mayo.mprc.fasta;

import edu.mayo.mprc.utilities.FileUtilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A DBOutputStream used to write FASTA headers and sequences into
 *
 * @author Eric J. Winter Date: Apr 10, 2007 Time: 11:37:54 AM
 */
public final class FASTAOutputStream implements DBOutputStream {
	/**
	 * the file that this output stream will write to
	 */
	private File file;
	/**
	 * the current writer object that we are writing to
	 */
	private FileWriter out;
	/**
	 * the number of sequences that have sofar been written
	 */
	private int sequenceCount = 0;

	/**
	 * the number of characters that should be included on a single line when outputing the sequence
	 */
	private static final int LINE_WIDTH = 80;

	/**
	 * create a new output stream from a file
	 *
	 * @param file the file that should be used as on output stream
	 * @throws IOException if there was a problem opening the file
	 */
	public FASTAOutputStream(final File file) throws IOException {
		this.file = file;
		this.out = new FileWriter(this.file);
	}


	/**
	 * Appends a given header and sequence to the output file.  This method may attempt to perform some ironing of the
	 * input if it is determined to need minor changes.  This makes sure that the number of characters per line of
	 * sequence is equal to the lineWidth(80?).
	 *
	 * @param header   the metadata header that describes the sequence
	 * @param sequence the actual sequence that should be written out
	 * @throws java.io.IOException if there was a problem performing the output
	 */
	public void appendSequence(final String header, final String sequence) throws IOException {
		final String cleanSequence = cleanupProteinSequence(sequence);

		//check to make sure the header contains a > since this is what denotes a header
		if (header.length() == 0 || header.charAt(0) != '>') {
			this.out.write('>');
		}
		//write out the header on a single line
		this.out.write(header);
		this.out.write("\n");

		//find out how many lines there eare and print out each but the last line in a loop
		final int steps = cleanSequence.length() / LINE_WIDTH;
		int i = 0;
		for (; i < steps; i++) {
			this.out.write(cleanSequence, i * LINE_WIDTH, LINE_WIDTH);
			this.out.write("\n");
		}

		//if we have more to print then print out the last line but make sure that we don't run off of the String
		if ((cleanSequence.length() - i * LINE_WIDTH) != 0) {
			this.out.write(cleanSequence, i * LINE_WIDTH, cleanSequence.length() - i * LINE_WIDTH);
			this.out.write("\n");
		}

		this.sequenceCount++;
	}

	/**
	 * Clean up a given protein sequence, by converting to uppercase and removing anything that is not A-Z.
	 *
	 * @param sequence Sequence to clean up.
	 * @return Cleaned-up sequence.
	 */
	public static String cleanupProteinSequence(String sequence) {
		StringBuilder result = new StringBuilder(sequence.length());
		for (int i = 0; i < sequence.length(); i++) {
			final char aa = sequence.charAt(i);
			if (aa >= 'A' && aa <= 'Z') {
				result.append(aa);
			} else if (aa >= 'a' && aa <= 'z') {
				result.append(Character.toUpperCase(aa));
			}
		}
		return result.toString();
	}

	/**
	 * Simply transfer every header and sequence remaining in the input stream to this output stream
	 *
	 * @param inputStream the stream to copy to this stream
	 * @throws IOException if there was a problem openning or working with either stream
	 */
	public void appendRemaining(final DBInputStream inputStream) throws IOException {
		if (inputStream != null) {
			while (inputStream.gotoNextSequence()) {
				this.appendSequence(inputStream.getHeader(), inputStream.getSequence());
			}
		}
	}

	public File getFile() {
		return this.file;
	}

	/**
	 * performs any cleaning up that may be necessary.
	 */
	public void close() {
		FileUtilities.closeQuietly(this.out);
	}

	/**
	 * gets the number of sequences that were written out
	 *
	 * @return the number of sequences written out (0 if no writting has been done)
	 */
	public int getSequenceCount() {
		return this.sequenceCount;
	}
}
