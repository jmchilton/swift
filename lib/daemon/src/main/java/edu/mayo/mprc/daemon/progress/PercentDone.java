package edu.mayo.mprc.daemon.progress;

/**
 * How many percent are complete for a given process.
 */
public class PercentDone implements ProgressInfo {
	private static final long serialVersionUID = 20110418L;
	private final float percentDone;

	public PercentDone(float percentDone) {
		this.percentDone = percentDone;
	}

	public float getPercentDone() {
		return percentDone;
	}
}
