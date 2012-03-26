package edu.mayo.mprc.qstat;

import edu.mayo.mprc.utilities.progress.ProgressInfo;

public final class QstatOutput implements ProgressInfo {
	private static final long serialVersionUID = 20080129L;

	private String qstatOutput;

	public QstatOutput(final String qstatOutput) {
		this.qstatOutput = qstatOutput;
	}

	public String getQstatOutput() {
		return qstatOutput;
	}
}
