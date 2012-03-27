package edu.mayo.mprc.utilities.progress;

import java.text.MessageFormat;

/**
 * How many percent are complete for a given process.
 */
public class PercentDone implements ProgressInfo {
	private static final long serialVersionUID = 20110418L;
	private final float percentDone;

	/**
	 * @param percentDone How many percent done. 100% is stored as 100.0
	 */
	public PercentDone(final float percentDone) {
		this.percentDone = percentDone;
	}

	/**
	 * @return How many percent done. 100% is returned as 100.0
	 */
	public float getPercentDone() {
		return percentDone;
	}

	@Override
	public String toString() {
		return MessageFormat.format("Percent done: {0,number,#.##}", percentDone);
	}
}
