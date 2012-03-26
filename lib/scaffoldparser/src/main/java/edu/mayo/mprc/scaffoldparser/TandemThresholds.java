package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Thresholds for X!Tandem. The logExpectScores parameter is comma separated list of floats.
 */
@XStreamAlias("TandemThresholds")
public final class TandemThresholds implements AbstractProgramSpecificThreshold {

	@XStreamAlias("peptideProbability")
	@XStreamAsAttribute()
	private double peptideProbability;

	@XStreamAlias("logExpectScores")
	@XStreamAsAttribute()
	private String logExpectScores;

	public TandemThresholds() {
	}

	public TandemThresholds(final double peptideProbability, final String logExpectScores) {
		this.peptideProbability = peptideProbability;
		this.logExpectScores = logExpectScores;
	}

	public double getPeptideProbability() {
		return peptideProbability;
	}

	public void setPeptideProbability(final double peptideProbability) {
		this.peptideProbability = peptideProbability;
	}

	public String getLogExpectScores() {
		return logExpectScores;
	}

	public void setLogExpectScores(final String logExpectScores) {
		this.logExpectScores = logExpectScores;
	}
}
