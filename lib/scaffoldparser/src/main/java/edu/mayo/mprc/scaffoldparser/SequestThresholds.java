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

	public SequestThresholds(double peptideProbability, double deltaCn, String xCorrs) {
		this.peptideProbability = peptideProbability;
		this.deltaCn = deltaCn;
		this.xCorrs = xCorrs;
	}

	public SequestThresholds() {

	}

	public double getPeptideProbability() {
		return peptideProbability;
	}

	public void setPeptideProbability(double peptideProbability) {
		this.peptideProbability = peptideProbability;
	}

	public double getDeltaCn() {
		return deltaCn;
	}

	public void setDeltaCn(double deltaCn) {
		this.deltaCn = deltaCn;
	}

	public String getXCorrs() {
		return xCorrs;
	}

	public void setXCorrs(String xCorrs) {
		this.xCorrs = xCorrs;
	}
}
