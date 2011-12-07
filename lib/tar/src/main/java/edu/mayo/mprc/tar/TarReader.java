package edu.mayo.mprc.tar;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.tools.tar.TarInputStream;

import java.io.File;
import java.io.IOException;

public final class TarReader {

	private final File tarFile;

	/**
	 * Used to open a reader on an existing tar file
	 *
	 * @param tarFile - the tar file
	 */
	public TarReader(File tarFile) {
		this.tarFile = tarFile;
		if (!this.tarFile.exists()) {
			throw new MprcException("tar file=" + this.tarFile.getAbsolutePath() + " does not exist");
		}
	}

	/**
	 * read the number of headers in a tar file
	 *
	 * @param tarFile - the name of the tar file
	 * @return
	 */
	public static int readNumberHeaders(File tarFile) {
		TarReader t = new TarReader(tarFile);
		return t.readNumberHeaders();
	}

	/**
	 * get the number of headers in the tar file
	 *
	 * @return the number of headers in the tar file
	 */
	public int readNumberHeaders() {
		int headers = 0;
		TarInputStream inputStream = null;
		try {
			inputStream = new TarInputStream(FileUtilities.getInputStream(tarFile));
			while (inputStream.getNextEntry() != null) {
				headers++;
			}
		} catch (IOException ioe) {
			throw new MprcException(ioe);
		} finally {
			FileUtilities.closeQuietly(inputStream);
		}
		return headers;
	}

}
