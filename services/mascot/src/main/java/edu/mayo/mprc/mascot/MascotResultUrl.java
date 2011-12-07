package edu.mayo.mprc.mascot;

import edu.mayo.mprc.daemon.progress.ProgressInfo;

public final class MascotResultUrl implements ProgressInfo {
	private static final long serialVersionUID = 20101101;

	private String mascotUrl;

	public MascotResultUrl() {
	}

	public MascotResultUrl(String mascotUrl) {
		this.mascotUrl = mascotUrl;
	}

	public String getMascotUrl() {
		return mascotUrl;
	}

	public void setMascotUrl(String mascotUrl) {
		this.mascotUrl = mascotUrl;
	}
}
