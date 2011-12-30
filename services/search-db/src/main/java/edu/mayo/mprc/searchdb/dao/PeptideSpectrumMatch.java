package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;

/**
 * Information about how many times was a specific peptide matched to a spectrum.
 * <p/>
 * Following columns from Scaffold's spectrum report are matched:
 * <ul>
 * <li>Previous amino acid</li>
 * <li>Next amino acid</li>
 * <li>Number of enzymatic termini</li>
 * </ul>
 * These statistics are calculated from the spectra:
 * <ul>
 * <li>Best Peptide identification probability</li>
 * <li>Best SEQUEST XCorr score</li>
 * <li>Best SEQUEST DCn score</li>
 * <li>Best Mascot Ion score</li>
 * <li>Best Mascot Identity score</li>
 * <li>Best Mascot Delta Ion Score</li>
 * <li>Best X! Tandem -log(e) score</li>
 * <li>Number of identified spectra (total, not unique)</li>
 * <li>Number of identified +1H spectra</li>
 * <li>Number of identified +2H spectra</li>
 * <li>Number of identified +3H spectra</li>
 * <li>Number of identified +4H spectra</li>
 * </ul>
 * These fields are not stored directly, but are read to calculate statistics or map to peptide information:
 * <ul>
 * <li>Spectrum name</li>
 * <li>Spectrum charge</li>
 * <li>Peptide identification probability</li>
 * <li>SEQUEST XCorr score</li>
 * <li>SEQUEST DCn score</li>
 * <li>Mascot Ion score</li>
 * <li>Mascot Identity score</li>
 * <li>Mascot Delta Ion Score</li>
 * <li>X! Tandem -log(e) score</li>
 * </ul>
 *
 * @author Roman Zenka
 */
public class PeptideSpectrumMatch extends PersistableBase {
	/**
	 * Peptide that is identified by this PSM. This means peptide sequence + modifications.
	 */
	private IdentifiedPeptide peptide;

	/**
	 * Previous amino acid. Used e.g. to distinguish whether the algorithm could have
	 * thought this was an actual tryptic peptide (probabilities for those can vary).
	 * This was not actually observed by the instrument and it depends on which protein the algorithm
	 * assigned the peptide to.
	 */
	private char previousAminoAcid;

	/**
	 * Next amino acid. See {@link #previousAminoAcid} for more info.
	 */
	private char nextAminoAcid;

	/**
	 * Number of enzymatic termini - to distinguish missed cleavage hits from enzymatic. Can be 0-2.
	 */
	private int numberOfEnzymaticTerminii;

	/**
	 * Best Peptide identification probability - maximum over all matched spectra.
	 * Probability of 100% is stored as 1.0
	 */
	private double bestPeptideIdentificationProbability;

	/**
	 * Best SEQUEST XCorr score - maximum over all matched  spectra.
	 */
	private double bestSequestXcorrScore;

	/**
	 * Best SEQUEST DCn score - maximum over all matched spectra.
	 */
	private double bestSequestDcnScore;

	/**
	 * Best Mascot Ion score - maximum over all matched spectra.
	 */
	private double bestMascotIonScore;

	/**
	 * Best Mascot Identity score - maximum over all matched spectra.
	 */
	private double bestMascotIdentityScore;

	/**
	 * Best Mascot Delta Ion Score - maximum over all matched spectra.
	 */
	private double bestMascotDeltaIonScore;

	/**
	 * Best X! Tandem -log(e) score - maximum over all matched spectra
	 */
	private double bestXTandemLogEScore;

	/**
	 * Number of identified spectra (total, not unique)
	 */
	private int numberOfIdentifiedSpectra;

	/**
	 * Number of identified +1H spectra
	 */
	private int numberOfIdentified1HSpectra;

	/**
	 * Number of identified +2H spectra
	 */
	private int numberOfIdentified2HSpectra;

	/**
	 * Number of identified +3H spectra
	 */
	private int numberOfIdentified3HSpectra;

	/**
	 * Number of identified +4H spectra
	 */
	private int numberOfIdentified4HSpectra;

	public IdentifiedPeptide getPeptide() {
		return peptide;
	}

	public void setPeptide(IdentifiedPeptide peptide) {
		this.peptide = peptide;
	}

	public char getPreviousAminoAcid() {
		return previousAminoAcid;
	}

	public void setPreviousAminoAcid(char previousAminoAcid) {
		this.previousAminoAcid = previousAminoAcid;
	}

	public char getNextAminoAcid() {
		return nextAminoAcid;
	}

	public void setNextAminoAcid(char nextAminoAcid) {
		this.nextAminoAcid = nextAminoAcid;
	}

	public int getNumberOfEnzymaticTerminii() {
		return numberOfEnzymaticTerminii;
	}

	public void setNumberOfEnzymaticTerminii(int numberOfEnzymaticTerminii) {
		this.numberOfEnzymaticTerminii = numberOfEnzymaticTerminii;
	}

	public double getBestPeptideIdentificationProbability() {
		return bestPeptideIdentificationProbability;
	}

	public void setBestPeptideIdentificationProbability(double bestPeptideIdentificationProbability) {
		this.bestPeptideIdentificationProbability = bestPeptideIdentificationProbability;
	}

	public double getBestSequestXcorrScore() {
		return bestSequestXcorrScore;
	}

	public void setBestSequestXcorrScore(double bestSequestXcorrScore) {
		this.bestSequestXcorrScore = bestSequestXcorrScore;
	}

	public double getBestSequestDcnScore() {
		return bestSequestDcnScore;
	}

	public void setBestSequestDcnScore(double bestSequestDcnScore) {
		this.bestSequestDcnScore = bestSequestDcnScore;
	}

	public double getBestMascotIonScore() {
		return bestMascotIonScore;
	}

	public void setBestMascotIonScore(double bestMascotIonScore) {
		this.bestMascotIonScore = bestMascotIonScore;
	}

	public double getBestMascotIdentityScore() {
		return bestMascotIdentityScore;
	}

	public void setBestMascotIdentityScore(double bestMascotIdentityScore) {
		this.bestMascotIdentityScore = bestMascotIdentityScore;
	}

	public double getBestMascotDeltaIonScore() {
		return bestMascotDeltaIonScore;
	}

	public void setBestMascotDeltaIonScore(double bestMascotDeltaIonScore) {
		this.bestMascotDeltaIonScore = bestMascotDeltaIonScore;
	}

	public double getBestXTandemLogEScore() {
		return bestXTandemLogEScore;
	}

	public void setBestXTandemLogEScore(double bestXTandemLogEScore) {
		this.bestXTandemLogEScore = bestXTandemLogEScore;
	}

	public int getNumberOfIdentifiedSpectra() {
		return numberOfIdentifiedSpectra;
	}

	public void setNumberOfIdentifiedSpectra(int numberOfIdentifiedSpectra) {
		this.numberOfIdentifiedSpectra = numberOfIdentifiedSpectra;
	}

	public int getNumberOfIdentified1HSpectra() {
		return numberOfIdentified1HSpectra;
	}

	public void setNumberOfIdentified1HSpectra(int numberOfIdentified1HSpectra) {
		this.numberOfIdentified1HSpectra = numberOfIdentified1HSpectra;
	}

	public int getNumberOfIdentified2HSpectra() {
		return numberOfIdentified2HSpectra;
	}

	public void setNumberOfIdentified2HSpectra(int numberOfIdentified2HSpectra) {
		this.numberOfIdentified2HSpectra = numberOfIdentified2HSpectra;
	}

	public int getNumberOfIdentified3HSpectra() {
		return numberOfIdentified3HSpectra;
	}

	public void setNumberOfIdentified3HSpectra(int numberOfIdentified3HSpectra) {
		this.numberOfIdentified3HSpectra = numberOfIdentified3HSpectra;
	}

	public int getNumberOfIdentified4HSpectra() {
		return numberOfIdentified4HSpectra;
	}

	public void setNumberOfIdentified4HSpectra(int numberOfIdentified4HSpectra) {
		this.numberOfIdentified4HSpectra = numberOfIdentified4HSpectra;
	}
}
