package edu.mayo.mprc.utilities;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class GZipUtilities {

	public static final int BUFF_SIZE = 2048;


	/**
	 * Null Constructor
	 */
	private GZipUtilities() {
	}


	public static boolean isGZipped(final File f) throws IOException {
		return isGZipped(new FileInputStream(f));
	}

	public static boolean isGZipped(final InputStream s) throws IOException {
		try {
			final byte[] toRead = new byte[2];
			final int readBytes = s.read(toRead);
			if (readBytes < 2) {
				return false;
			}
			final byte b1 = (byte) 0x1f;
			final byte b2 = (byte) 0x8b;
			return (toRead[0] == b1 && toRead[1] == b2);
		} finally {
			s.close();
		}
	}

	/**
	 * Compresses a given file into a new file.  This method will refuse to overwrite an existing file
	 *
	 * @param toCompress      the file you want to compress.
	 * @param destinationFile the the file you want to compress into.
	 * @throws java.io.IOException if there was a problem or if you were trying to overwrite an existing file.
	 */
	public static void compressFile(final File toCompress, final File destinationFile) throws IOException {
		streamToCompressedFile(new FileInputStream(toCompress), destinationFile);
	}


	/**
	 * Takes in InputStream and compresses it into a given file.  This method will not overwrite an existing file.
	 *
	 * @param istream         the stream to compress and write out
	 * @param destinationFile the file you want to put the compressed stream into.
	 * @throws IOException if there
	 */
	public static void streamToCompressedFile(final InputStream istream, final File destinationFile) throws IOException {
		if (destinationFile.exists()) {
			throw new IOException("Refusing to overwrite an existing file.");
		}

		OutputStream gzStream = null;
		InputStream biStream = null;
		try {
			gzStream = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(destinationFile)), BUFF_SIZE);
			biStream = new BufferedInputStream(istream, BUFF_SIZE);
			transferStream(biStream, gzStream);
		} finally {
			if (gzStream != null) {
				gzStream.close();
			}
			if (biStream != null) {
				biStream.close();
			}
		}
	}

	/**
	 * Decompreses one gzipped file into another file.
	 *
	 * @param toDecompress
	 * @param destinationFile
	 * @throws IOException if there was a problem.
	 */
	public static void decompressFile(final File toDecompress, final File destinationFile) throws IOException {

		if (destinationFile.exists()) {
			throw new IOException("Refusing to overwrite an existing file.");
		}

		InputStream istream = null;
		OutputStream ostream = null;
		try {

			ostream = new BufferedOutputStream(new FileOutputStream(destinationFile), BUFF_SIZE);
			istream = getCompressedFileAsStream(toDecompress);
			transferStream(istream, ostream);
		} finally {
			if (istream != null) {
				istream.close();
			}
			if (ostream != null) {
				ostream.close();
			}
		}
	}

	/**
	 * gets an InputStream from a file that is gzip compressed.
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static InputStream getCompressedFileAsStream(final File file) throws IOException {
		return new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
	}

	/**
	 * Transfers an InputStream to an OutputStream it is up to the job of the caller to close the streams.
	 *
	 * @param istream
	 * @param ostream
	 * @throws IOException
	 */
	protected static void transferStream(final InputStream istream, final OutputStream ostream) throws IOException {
		final byte[] inBuf = new byte[BUFF_SIZE];
		int readBytes = istream.read(inBuf);
		while (readBytes >= 0) {
			ostream.write(inBuf, 0, readBytes);
			readBytes = istream.read(inBuf);
		}
	}

}
