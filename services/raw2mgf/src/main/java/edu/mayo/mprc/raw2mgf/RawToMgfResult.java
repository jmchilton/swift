package edu.mayo.mprc.raw2mgf;

import edu.mayo.mprc.daemon.files.FileHolder;
import edu.mayo.mprc.daemon.progress.ProgressInfo;

import java.io.File;

public final class RawToMgfResult extends FileHolder implements ProgressInfo {
	private static final long serialVersionUID = 20101025l;
	private File mgf;

	public RawToMgfResult(File mgfFile) {
		this.mgf = mgfFile;
	}

	public File getMgf() {
		return mgf;
	}
}
