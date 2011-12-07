package edu.mayo.mprc.qa;

import edu.mayo.mprc.daemon.files.FileHolder;
import edu.mayo.mprc.daemon.progress.ProgressInfo;

import java.io.File;

public final class RAWDumpResult extends FileHolder implements ProgressInfo {
	private static final long serialVersionUID = 20101221L;
	private File rawInfoFile;
	private File rawSpectraFile;
	private File chromatogramFile;

	public RAWDumpResult(File rawInfoFile, File rawSpectraFile, File chromatogramFile) {
		this.rawInfoFile = rawInfoFile;
		this.rawSpectraFile = rawSpectraFile;
		this.chromatogramFile = chromatogramFile;
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
}
