package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.daemon.files.FileHolder;

import java.io.File;

/**
 * A list of raw file metadata stored as many data files.
 *
 * @author Roman Zenka
 */
public class RawFileMetaData extends FileHolder {
	private File rawFile;
	private File info;
	private File tuneMethod;
	private File instrumentMethod;
	private File sampleInformation;
	private File errorLog;

	public RawFileMetaData(final File rawFile, final File info, final File tuneMethod, final File instrumentMethod, final File sampleInformation, final File errorLog) {
		this.rawFile = rawFile;
		this.info = info;
		this.tuneMethod = tuneMethod;
		this.instrumentMethod = instrumentMethod;
		this.sampleInformation = sampleInformation;
		this.errorLog = errorLog;
	}

	public File getRawFile() {
		return rawFile;
	}

	public File getInfo() {
		return info;
	}

	public File getTuneMethod() {
		return tuneMethod;
	}

	public File getInstrumentMethod() {
		return instrumentMethod;
	}

	public File getSampleInformation() {
		return sampleInformation;
	}

	public File getErrorLog() {
		return errorLog;
	}
}
