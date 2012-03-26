package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("SequestThresholds")
public final class SequestThresholds implements AbstractProgramSpecificThreshold {
	@XStreamAlias("peptideProbability")
	@XStreamAsAttribute
	private double peptideProbability;

	@XStreamAlias("deltaCn")
	@XStreamAsAttribute
	private double deltaCn;

	@XStreamAlias("xCorrs")
	@XStreamAsAttribute
	private String xCorrs;

	public SequestThresholds(final double peptideProbability, final double deltaCn, final String xCorrs) {
		this.peptideProbability = peptideProbability;
		this.deltaCn = deltaCn;
		this.xCorrs = xCorrs;
	}

	public SequestThresholds() {

	}

	public double getPeptideProbability() {
		return peptideProbability;
	}

	public void setPeptideProbability(final double peptideProbability) {
		this.peptideProbability = peptideProbability;
	}

	public double getDeltaCn() {
		return deltaCn;
	}

	public void setDeltaCn(final double deltaCn) {
		this.deltaCn = deltaCn;
	}

	public String getXCorrs() {
		return xCorrs;
	}

	public void setXCorrs(final String xCorrs) {
		this.xCorrs = xCorrs;
	}
}
