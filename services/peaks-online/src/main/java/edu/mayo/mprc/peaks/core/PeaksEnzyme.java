package edu.mayo.mprc.peaks.core;

import java.io.Serializable;

public final class PeaksEnzyme implements Serializable {
	private static final long serialVersionUID = 20090324L;
	private String enzymeId;
	private String enzymeName;

	public PeaksEnzyme(final String enzymeId, final String enzymeName) {
		this.enzymeId = enzymeId;
		this.enzymeName = enzymeName;
	}

	public String getEnzymeId() {
		return enzymeId;
	}

	public void setEnzymeId(final String enzymeId) {
		this.enzymeId = enzymeId;
	}

	public String getEnzymeName() {
		return enzymeName;
	}

	public void setEnzymeName(final String enzymeName) {
		this.enzymeName = enzymeName;
	}

	public String toString() {

		final StringBuilder builder = new StringBuilder();

		builder.append("Enzyme Id: ").append(enzymeId).append("\n");
		builder.append("Enzyme Name: ").append(enzymeName);

		return builder.toString();
	}
}
