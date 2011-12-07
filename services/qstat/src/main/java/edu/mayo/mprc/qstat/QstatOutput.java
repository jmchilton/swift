package edu.mayo.mprc.qstat;

import edu.mayo.mprc.daemon.progress.ProgressInfo;

public final class QstatOutput implements ProgressInfo {
	private static final long serialVersionUID = 20080129L;

	private String qstatOutput;

	public QstatOutput(String qstatOutput) {
		this.qstatOutput = qstatOutput;
	}

	public String getQstatOutput() {
		return qstatOutput;
	}
}
