package edu.mayo.mprc.searchdb.dao;

/**
 * A bundle of various search engine scores coming from Scaffold.
 * The scores are initially set to NaN unless specified otherwise, which means "score missing"
 *
 * @author Roman Zenka
 */
public final class SearchEngineScores {
	/**
	 * Sequest XCorr score
	 */
	private double sequestXcorrScore = Double.NaN;

	/**
	 * Sequest DCn score
	 */
	private double sequestDcnScore = Double.NaN;

	/**
	 * Preliminary Sequest score.
	 */
	private double sequestSpScore = Double.NaN;

	/**
	 * Sequest Sp rank.
	 */
	private double sequestSpRank = Double.NaN;

	/**
	 * How many peptides did Sequest match for this spectrum.
	 */
	private double sequestPeptidesMatched = Double.NaN;

	/**
	 * Mascot Ion score
	 */
	private double mascotIonScore = Double.NaN;

	/**
	 * Mascot Identity score
	 */
	private double mascotIdentityScore = Double.NaN;

	/**
	 * Mascot Homology score
	 */
	private double mascotHomologyScore = Double.NaN;

	/**
	 * Mascot Delta Ion Score
	 */
	private double mascotDeltaIonScore = Double.NaN;

	/**
	 * X! Tandem hypergeometric score
	 */
	private double tandemHyperScore = Double.NaN;

	/**
	 * X! Tandem ladder score
	 */
	private double tandemLadderScore = Double.NaN;

	/**
	 * Minimum scores (all zero).
	 */
	public SearchEngineScores() {
	}

	public SearchEngineScores(double sequestXcorrScore, double sequestDcnScore, double sequestSpScore, double sequestSpRank, double sequestPeptidesMatched, double mascotIonScore, double mascotIdentityScore, double mascotHomologyScore, double mascotDeltaIonScore, double tandemHyperScore, double tandemLadderScore) {
		this.sequestXcorrScore = sequestXcorrScore;
		this.sequestDcnScore = sequestDcnScore;
		this.sequestSpScore = sequestSpScore;
		this.sequestSpRank = sequestSpRank;
		this.sequestPeptidesMatched = sequestPeptidesMatched;
		this.mascotIonScore = mascotIonScore;
		this.mascotIdentityScore = mascotIdentityScore;
		this.mascotHomologyScore = mascotHomologyScore;
		this.mascotDeltaIonScore = mascotDeltaIonScore;
		this.tandemHyperScore = tandemHyperScore;
		this.tandemLadderScore = tandemLadderScore;
	}

	private double maxNaN(double d1, double d2) {
		if (Double.isNaN(d1)) {
			return d2;
		}
		if (Double.isNaN(d2)) {
			return d1;
		}
		return Math.max(d1, d2);
	}

	private double minNaN(double d1, double d2) {
		if (Double.isNaN(d1)) {
			return d2;
		}
		if (Double.isNaN(d2)) {
			return d1;
		}
		return Math.min(d1, d2);
	}

	/**
	 * Set these scores to maximum of current scores and supplied new scores.
	 * The scores can be set to NaN in which case it the score is missing.
	 *
	 * @param newScores New scores to incorporate.
	 */
	public void setMax(SearchEngineScores newScores) {
		sequestXcorrScore = maxNaN(getSequestXcorrScore(), newScores.getSequestXcorrScore());
		sequestDcnScore = maxNaN(getSequestDcnScore(), newScores.getSequestDcnScore());
		sequestSpScore = maxNaN(getSequestSpScore(), newScores.getSequestSpScore());
		sequestSpRank = /*min*/minNaN(getSequestSpRank(), newScores.getSequestSpRank());
		sequestPeptidesMatched = maxNaN(getSequestPeptidesMatched(), newScores.getSequestPeptidesMatched());
		mascotIonScore = maxNaN(getMascotIonScore(), newScores.getMascotIonScore());
		mascotIdentityScore = maxNaN(getMascotIdentityScore(), newScores.getMascotIdentityScore());
		mascotHomologyScore = maxNaN(getMascotHomologyScore(), newScores.getMascotHomologyScore());
		mascotDeltaIonScore = maxNaN(getMascotDeltaIonScore(), newScores.getMascotDeltaIonScore());
		tandemHyperScore = maxNaN(getTandemHyperScore(), newScores.getTandemHyperScore());
		tandemLadderScore = maxNaN(getTandemLadderScore(), newScores.getTandemLadderScore());
	}

	public double getSequestXcorrScore() {
		return sequestXcorrScore;
	}

	public double getSequestDcnScore() {
		return sequestDcnScore;
	}

	public double getSequestSpScore() {
		return sequestSpScore;
	}

	public double getSequestSpRank() {
		return sequestSpRank;
	}

	public double getSequestPeptidesMatched() {
		return sequestPeptidesMatched;
	}

	public double getMascotIonScore() {
		return mascotIonScore;
	}

	public double getMascotIdentityScore() {
		return mascotIdentityScore;
	}

	public double getMascotHomologyScore() {
		return mascotHomologyScore;
	}

	public double getMascotDeltaIonScore() {
		return mascotDeltaIonScore;
	}

	public double getTandemHyperScore() {
		return tandemHyperScore;
	}

	public double getTandemLadderScore() {
		return tandemLadderScore;
	}
}
