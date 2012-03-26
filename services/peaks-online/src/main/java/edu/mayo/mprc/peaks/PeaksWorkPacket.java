package edu.mayo.mprc.peaks;

import edu.mayo.mprc.daemon.WorkPacketBase;

import java.io.File;

/**
 * Defines a Peaks Online search.
 */
public final class PeaksWorkPacket extends WorkPacketBase {
	private static final long serialVersionUID = 20090324L;

	private File paramsFile;
	private File mgfFile;

	public PeaksWorkPacket(final String taskId, final boolean fromScratch) {
		super(taskId, fromScratch);
	}

	public PeaksWorkPacket(final String taskId, final boolean fromScratch, final File paramsFile, final File mgfFile) {
		super(taskId, fromScratch);
		this.paramsFile = paramsFile;
		this.mgfFile = mgfFile;
	}

	public File getParamsFile() {
		return paramsFile;
	}

	public File getMgfFile() {
		return mgfFile;
	}
}
