package edu.mayo.mprc.searchengine;

import edu.mayo.mprc.daemon.files.FileHolder;
import edu.mayo.mprc.utilities.progress.ProgressInfo;

import java.io.File;

public final class SearchEngineResult extends FileHolder implements ProgressInfo {
	private static final long serialVersionUID = 20101025l;
	private File resultFile;

	public SearchEngineResult(final File resultFile) {
		this.resultFile = resultFile;
	}

	public File getResultFile() {
		return resultFile;
	}
}
