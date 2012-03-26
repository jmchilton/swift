package edu.mayo.mprc.searchdb.builder;

import edu.mayo.mprc.searchdb.dao.IdentifiedPeptide;
import edu.mayo.mprc.searchdb.dao.PeptideSpectrumMatch;
import edu.mayo.mprc.searchdb.dao.SearchEngineScores;

/**
 * @author Roman Zenka
 */
public class PeptideSpectrumMatchBuilder implements Builder<PeptideSpectrumMatch> {
	private IdentifiedPeptide peptide;
	private char previousAminoAcid;
	private char nextAminoAcid;
	private double bestPeptideIdentificationProbability;
	private SearchEngineScores bestSearchEngineScores = new SearchEngineScores();
	private SpectrumIdentificationCountsBuilder spectrumIdentificationCounts = new SpectrumIdentificationCountsBuilder();
	private int numberOfEnzymaticTerminii;

	@Override
	public PeptideSpectrumMatch build() {
		return new PeptideSpectrumMatch(peptide, previousAminoAcid, nextAminoAcid, bestPeptideIdentificationProbability,
				bestSearchEngineScores, spectrumIdentificationCounts.build(), numberOfEnzymaticTerminii);
	}

	/**
	 * @param spectrumName   Name of the spectrum. In Swift-friendly format (filename.fromScan.toScan.charge.dta)
	 * @param spectrumCharge Charge as extracted by Scaffold.
	 * @param peptideIdentificationProbability
	 *                       Probability that this ID is correct as assigned by Scaffold.
	 */
	public void recordSpectrum(
			final String spectrumName,
			final int spectrumCharge,
			final double peptideIdentificationProbability,
			final SearchEngineScores searchEngineScores
	) {
		updateScores(peptideIdentificationProbability, searchEngineScores);
		addSpectrum(spectrumCharge);
	}

	/**
	 * Bump up best scores based on a new bunch.
	 *
	 * @param peptideIdentificationProbability
	 *               Potentially better ID probability.
	 * @param scores Potentially better search engine scores
	 */
	public void updateScores(final double peptideIdentificationProbability,
	                         final SearchEngineScores scores) {
		bestPeptideIdentificationProbability = Math.max(bestPeptideIdentificationProbability, peptideIdentificationProbability);
		bestSearchEngineScores.setMax(scores);
	}

	/**
	 * Count another spectrum towards the PSM.
	 *
	 * @param spectrumCharge Charge of the new spectrum.
	 */
	public void addSpectrum(final int spectrumCharge) {
		spectrumIdentificationCounts.addSpectrum(spectrumCharge);
	}

	public IdentifiedPeptide getPeptide() {
		return peptide;
	}

	public void setPeptide(final IdentifiedPeptide peptide) {
		this.peptide = peptide;
	}

	public SpectrumIdentificationCountsBuilder getSpectrumIdentificationCounts() {
		return spectrumIdentificationCounts;
	}

	public char getPreviousAminoAcid() {
		return previousAminoAcid;
	}

	public void setPreviousAminoAcid(final char previousAminoAcid) {
		this.previousAminoAcid = previousAminoAcid;
	}

	public char getNextAminoAcid() {
		return nextAminoAcid;
	}

	public void setNextAminoAcid(final char nextAminoAcid) {
		this.nextAminoAcid = nextAminoAcid;
	}

	public double getBestPeptideIdentificationProbability() {
		return bestPeptideIdentificationProbability;
	}

	public void setBestPeptideIdentificationProbability(final double bestPeptideIdentificationProbability) {
		this.bestPeptideIdentificationProbability = bestPeptideIdentificationProbability;
	}

	public SearchEngineScores getBestSearchEngineScores() {
		return bestSearchEngineScores;
	}

	public void setBestSearchEngineScores(final SearchEngineScores bestSearchEngineScores) {
		this.bestSearchEngineScores = bestSearchEngineScores;
	}

	public int getNumberOfEnzymaticTerminii() {
		return numberOfEnzymaticTerminii;
	}

	public void setNumberOfEnzymaticTerminii(final int numberOfEnzymaticTerminii) {
		this.numberOfEnzymaticTerminii = numberOfEnzymaticTerminii;
	}
}
