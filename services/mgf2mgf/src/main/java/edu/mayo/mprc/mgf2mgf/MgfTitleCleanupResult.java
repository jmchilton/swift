package edu.mayo.mprc.mgf2mgf;

import edu.mayo.mprc.utilities.progress.ProgressInfo;

/**
 * Result of cleaning up the mgf.
 */
public final class MgfTitleCleanupResult implements ProgressInfo {
	private static final long serialVersionUID = 20090324L;
	private boolean cleanupPerformed;

	public MgfTitleCleanupResult(boolean cleanupPerformed) {
		this.cleanupPerformed = cleanupPerformed;
	}

	public boolean isCleanupPerformed() {
		return cleanupPerformed;
	}
}
