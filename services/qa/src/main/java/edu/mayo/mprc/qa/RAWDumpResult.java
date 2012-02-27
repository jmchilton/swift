package edu.mayo.mprc.qa;

import edu.mayo.mprc.daemon.files.FileHolder;
import edu.mayo.mprc.utilities.progress.ProgressInfo;

import java.io.File;

public final class RAWDumpResult extends FileHolder implements ProgressInfo {
	private static final long serialVersionUID = 20101221L;
	private File rawInfoFile;
	private File rawSpectraFile;
	private File chromatogramFile;
	private File tuneMethodFile;
	private File instrumentMethodFile;
	private File sampleInformationFile;
	private File errorLogFile;

	public RAWDumpResult(File rawInfoFile, File rawSpectraFile, File chromatogramFile, File tuneMethodFile, File instrumentMethodFile, File sampleInformationFile, File errorLogFile) {
		this.rawInfoFile = rawInfoFile;
		this.rawSpectraFile = rawSpectraFile;
		this.chromatogramFile = chromatogramFile;
		this.tuneMethodFile = tuneMethodFile;
		this.instrumentMethodFile = instrumentMethodFile;
		this.sampleInformationFile = sampleInformationFile;
		this.errorLogFile = errorLogFile;
	}

	public File getRawInfoFile() {
		return rawInfoFile;
	}

	public File getRawSpectraFile() {
		return rawSpectraFile;
	}

	public File getChromatogramFile() {
		return chromatogramFile;
	}

	public File getTuneMethodFile() {
		return tuneMethodFile;
	}

	public File getInstrumentMethodFile() {
		return instrumentMethodFile;
	}

	public File getSampleInformationFile() {
		return sampleInformationFile;
	}

	public File getErrorLogFile() {
		return errorLogFile;
	}
}
