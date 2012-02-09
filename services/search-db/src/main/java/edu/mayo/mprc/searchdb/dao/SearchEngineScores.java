package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.utilities.MprcDoubles;

/**
 * A bundle of various search engine scores coming from Scaffold.
 * The scores are initially set to NaN unless specified otherwise, which means "score missing"
 *
 * @author Roman Zenka
 */
public final class SearchEngineScores {
    /**
     * Delta for double comparison.
     */
    public static final double DELTA = 1E-8;

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
        this.setSequestXcorrScore(sequestXcorrScore);
        this.setSequestDcnScore(sequestDcnScore);
        this.setSequestSpScore(sequestSpScore);
        this.setSequestSpRank(sequestSpRank);
        this.setSequestPeptidesMatched(sequestPeptidesMatched);
        this.setMascotIonScore(mascotIonScore);
        this.setMascotIdentityScore(mascotIdentityScore);
        this.setMascotHomologyScore(mascotHomologyScore);
        this.setMascotDeltaIonScore(mascotDeltaIonScore);
        this.setTandemHyperScore(tandemHyperScore);
        this.setTandemLadderScore(tandemLadderScore);
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
        setSequestXcorrScore(maxNaN(getSequestXcorrScore(), newScores.getSequestXcorrScore()));
        setSequestDcnScore(maxNaN(getSequestDcnScore(), newScores.getSequestDcnScore()));
        setSequestSpScore(maxNaN(getSequestSpScore(), newScores.getSequestSpScore()));
        setSequestSpRank(minNaN(getSequestSpRank(), newScores.getSequestSpRank()));
        setSequestPeptidesMatched(maxNaN(getSequestPeptidesMatched(), newScores.getSequestPeptidesMatched()));
        setMascotIonScore(maxNaN(getMascotIonScore(), newScores.getMascotIonScore()));
        setMascotIdentityScore(maxNaN(getMascotIdentityScore(), newScores.getMascotIdentityScore()));
        setMascotHomologyScore(maxNaN(getMascotHomologyScore(), newScores.getMascotHomologyScore()));
        setMascotDeltaIonScore(maxNaN(getMascotDeltaIonScore(), newScores.getMascotDeltaIonScore()));
        setTandemHyperScore(maxNaN(getTandemHyperScore(), newScores.getTandemHyperScore()));
        setTandemLadderScore(maxNaN(getTandemLadderScore(), newScores.getTandemLadderScore()));
    }

    public double getSequestXcorrScore() {
        return sequestXcorrScore;
    }

    public void setSequestXcorrScore(double sequestXcorrScore) {
        this.sequestXcorrScore = sequestXcorrScore;
    }

    public double getSequestDcnScore() {
        return sequestDcnScore;
    }

    public void setSequestDcnScore(double sequestDcnScore) {
        this.sequestDcnScore = sequestDcnScore;
    }

    public double getSequestSpScore() {
        return sequestSpScore;
    }

    public void setSequestSpScore(double sequestSpScore) {
        this.sequestSpScore = sequestSpScore;
    }

    public double getSequestSpRank() {
        return sequestSpRank;
    }

    public void setSequestSpRank(double sequestSpRank) {
        this.sequestSpRank = sequestSpRank;
    }

    public double getSequestPeptidesMatched() {
        return sequestPeptidesMatched;
    }

    public void setSequestPeptidesMatched(double sequestPeptidesMatched) {
        this.sequestPeptidesMatched = sequestPeptidesMatched;
    }

    public double getMascotIonScore() {
        return mascotIonScore;
    }

    public void setMascotIonScore(double mascotIonScore) {
        this.mascotIonScore = mascotIonScore;
    }

    public double getMascotIdentityScore() {
        return mascotIdentityScore;
    }

    public void setMascotIdentityScore(double mascotIdentityScore) {
        this.mascotIdentityScore = mascotIdentityScore;
    }

    public double getMascotHomologyScore() {
        return mascotHomologyScore;
    }

    public void setMascotHomologyScore(double mascotHomologyScore) {
        this.mascotHomologyScore = mascotHomologyScore;
    }

    public double getMascotDeltaIonScore() {
        return mascotDeltaIonScore;
    }

    public void setMascotDeltaIonScore(double mascotDeltaIonScore) {
        this.mascotDeltaIonScore = mascotDeltaIonScore;
    }

    public double getTandemHyperScore() {
        return tandemHyperScore;
    }

    public void setTandemHyperScore(double tandemHyperScore) {
        this.tandemHyperScore = tandemHyperScore;
    }

    public double getTandemLadderScore() {
        return tandemLadderScore;
    }

    public void setTandemLadderScore(double tandemLadderScore) {
        this.tandemLadderScore = tandemLadderScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SearchEngineScores that = (SearchEngineScores) o;

        if (!MprcDoubles.within(that.getMascotDeltaIonScore(), getMascotDeltaIonScore(), DELTA) ||
                !MprcDoubles.within(that.getMascotHomologyScore(), getMascotHomologyScore(), DELTA) ||
                !MprcDoubles.within(that.getMascotIdentityScore(), getMascotIdentityScore(), DELTA) ||
                !MprcDoubles.within(that.getMascotIonScore(), getMascotIonScore(), DELTA) ||
                !MprcDoubles.within(that.getSequestDcnScore(), getSequestDcnScore(), DELTA) ||
                !MprcDoubles.within(that.getSequestPeptidesMatched(), getSequestPeptidesMatched(), DELTA) ||
                !MprcDoubles.within(that.getSequestSpRank(), getSequestSpRank(), DELTA) ||
                !MprcDoubles.within(that.getSequestSpScore(), getSequestSpScore(), DELTA) ||
                !MprcDoubles.within(that.getSequestXcorrScore(), getSequestXcorrScore(), DELTA) ||
                !MprcDoubles.within(that.getTandemHyperScore(), getTandemHyperScore(), DELTA) ||
                !MprcDoubles.within(that.getTandemLadderScore(), getTandemLadderScore(), DELTA)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = getSequestXcorrScore() != +0.0d ? Double.doubleToLongBits(getSequestXcorrScore()) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = getSequestDcnScore() != +0.0d ? Double.doubleToLongBits(getSequestDcnScore()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = getSequestSpScore() != +0.0d ? Double.doubleToLongBits(getSequestSpScore()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = getSequestSpRank() != +0.0d ? Double.doubleToLongBits(getSequestSpRank()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = getSequestPeptidesMatched() != +0.0d ? Double.doubleToLongBits(getSequestPeptidesMatched()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = getMascotIonScore() != +0.0d ? Double.doubleToLongBits(getMascotIonScore()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = getMascotIdentityScore() != +0.0d ? Double.doubleToLongBits(getMascotIdentityScore()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = getMascotHomologyScore() != +0.0d ? Double.doubleToLongBits(getMascotHomologyScore()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = getMascotDeltaIonScore() != +0.0d ? Double.doubleToLongBits(getMascotDeltaIonScore()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = getTandemHyperScore() != +0.0d ? Double.doubleToLongBits(getTandemHyperScore()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = getTandemLadderScore() != +0.0d ? Double.doubleToLongBits(getTandemLadderScore()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
