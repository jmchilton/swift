package edu.mayo.mprc.mgf2mgf;

import edu.mayo.mprc.daemon.WorkPacketBase;

import java.io.File;

public final class MgfTitleCleanupWorkPacket extends WorkPacketBase {
	private static final long serialVersionUID = 20080214L;

	private File mgfToCleanup;
	private File cleanedMgf;

	public MgfTitleCleanupWorkPacket(final File mgfToCleanup, final File cleanedMgf, final String taskId, final boolean fromScratch) {
		super(taskId, fromScratch);
		this.mgfToCleanup = mgfToCleanup;
		this.cleanedMgf = cleanedMgf;
	}

	public File getMgfToCleanup() {
		return mgfToCleanup;
	}

	public File getCleanedMgf() {
		return cleanedMgf;
	}


	@Override
	public void synchronizeFileTokensOnReceiver() {
		if (getCleanedMgf().exists()) {
			uploadAndWait("cleanedMgf");
		}
	}
}
