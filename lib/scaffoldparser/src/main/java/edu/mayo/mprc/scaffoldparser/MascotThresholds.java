package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("MascotThresholds")
public final class MascotThresholds implements AbstractProgramSpecificThreshold {
	@XStreamAlias("peptideProbability")
	@XStreamAsAttribute()
	private double peptideProbability;

	@XStreamAlias("ionMinusIdentityScore")
	@XStreamAsAttribute()
	private double ionMinusIdentityScore;

	@XStreamAlias("ionScores")
	@XStreamAsAttribute()
	private String ionScores;

	public MascotThresholds(final float peptideProbability, final float ionMinusIdentityScore, final String ionScores) {
		this.peptideProbability = peptideProbability;
		this.ionMinusIdentityScore = ionMinusIdentityScore;
		this.ionScores = ionScores;
	}

	public MascotThresholds() {

	}

	public double getPeptideProbability() {
		return peptideProbability;
	}

	public void setPeptideProbability(final double peptideProbability) {
		this.peptideProbability = peptideProbability;
	}

	public double getIonMinusIdentityScore() {
		return ionMinusIdentityScore;
	}

	public void setIonMinusIdentityScore(final double ionMinusIdentityScore) {
		this.ionMinusIdentityScore = ionMinusIdentityScore;
	}

	public String getIonScores() {
		return ionScores;
	}

	public void setIonScores(final String ionScores) {
		this.ionScores = ionScores;
	}
}
