package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;
import edu.mayo.mprc.utilities.MprcDoubles;

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
 * <li>{@link #bestPeptideIdentificationProbability}</li>
 * <li>{@link SearchEngineScores}</li>
 * <li>{@link SpectrumIdentificationCounts}</li>
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
	public static final double PERCENT_TOLERANCE = 0.00001;
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
	 * Peptide identification probability - the best one over all the spectra.
	 * Probability of 100% is stored as 1.0
	 */
	private double bestPeptideIdentificationProbability;

	/**
	 * Best search engine scores encountered so far. Each component is maximized separately.
	 */
	private SearchEngineScores bestSearchEngineScores = new SearchEngineScores();

	/**
	 * How many spectra have we seen so far for different charge states.
	 */
	private SpectrumIdentificationCounts spectrumIdentificationCounts = new SpectrumIdentificationCounts();

	/**
	 * Number of enzymatic termini - to distinguish missed cleavage hits from enzymatic. Can be 0-2.
	 */
	private int numberOfEnzymaticTerminii;

	public PeptideSpectrumMatch() {
	}

	public PeptideSpectrumMatch(IdentifiedPeptide peptide, char previousAminoAcid, char nextAminoAcid, double bestPeptideIdentificationProbability, SearchEngineScores bestSearchEngineScores, SpectrumIdentificationCounts spectrumIdentificationCounts, int numberOfEnzymaticTerminii) {
		this.peptide = peptide;
		this.previousAminoAcid = previousAminoAcid;
		this.nextAminoAcid = nextAminoAcid;
		this.bestPeptideIdentificationProbability = bestPeptideIdentificationProbability;
		this.bestSearchEngineScores = bestSearchEngineScores;
		this.spectrumIdentificationCounts = spectrumIdentificationCounts;
		this.numberOfEnzymaticTerminii = numberOfEnzymaticTerminii;
	}

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

	public SearchEngineScores getBestSearchEngineScores() {
		return bestSearchEngineScores;
	}

	public void setBestSearchEngineScores(SearchEngineScores bestSearchEngineScores) {
		this.bestSearchEngineScores = bestSearchEngineScores;
	}

	public SpectrumIdentificationCounts getSpectrumIdentificationCounts() {
		return spectrumIdentificationCounts;
	}

	public void setSpectrumIdentificationCounts(SpectrumIdentificationCounts spectrumIdentificationCounts) {
		this.spectrumIdentificationCounts = spectrumIdentificationCounts;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PeptideSpectrumMatch that = (PeptideSpectrumMatch) o;

		if (!MprcDoubles.within(that.getBestPeptideIdentificationProbability(), getBestPeptideIdentificationProbability(), PERCENT_TOLERANCE)) {
			return false;
		}
		if (getNextAminoAcid() != that.getNextAminoAcid()) {
			return false;
		}
		if (getNumberOfEnzymaticTerminii() != that.getNumberOfEnzymaticTerminii()) {
			return false;
		}
		if (getPreviousAminoAcid() != that.getPreviousAminoAcid()) {
			return false;
		}
		if (getBestSearchEngineScores() != null ? !getBestSearchEngineScores().equals(that.getBestSearchEngineScores()) : that.getBestSearchEngineScores() != null) {
			return false;
		}
		if (getPeptide() != null ? !getPeptide().equals(that.getPeptide()) : that.getPeptide() != null) {
			return false;
		}
		if (getSpectrumIdentificationCounts() != null ? !getSpectrumIdentificationCounts().equals(that.getSpectrumIdentificationCounts()) : that.getSpectrumIdentificationCounts() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = getPeptide() != null ? getPeptide().hashCode() : 0;
		result = 31 * result + (int) getPreviousAminoAcid();
		result = 31 * result + (int) getNextAminoAcid();
		temp = getBestPeptideIdentificationProbability() != +0.0d ? Double.doubleToLongBits(getBestPeptideIdentificationProbability()) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (getBestSearchEngineScores() != null ? getBestSearchEngineScores().hashCode() : 0);
		result = 31 * result + (getSpectrumIdentificationCounts() != null ? getSpectrumIdentificationCounts().hashCode() : 0);
		result = 31 * result + getNumberOfEnzymaticTerminii();
		return result;
	}
}
