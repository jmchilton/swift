package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;

import java.util.List;
import java.util.Set;

/**
 * Maps the following columns from the Scaffold spectrum report:
 * <ul>
 * <li>Protein accession numbers</li>
 * <li>Protein identification probability</li>
 * <li>Number of unique peptides</li>
 * <li>Number of unique spectra</li>
 * <li>Number of total spectra</li>
 * <li>Percentage of total spectra</li>
 * <li>Percentage sequence coverage</li>
 * </ul>
 *
 * @author Roman Zenka
 */
public class ProteinGroup extends PersistableBase {
	/**
	 * List of protein sequences belonging to the group.
	 */
	private Set<ProteinSequence> proteinSequences;

	/**
	 * List of peptides matched to spectra that belong to this protein group.
	 */
	private Set<PeptideSpectrumMatch> peptideSpectrumMatches;

	/**
	 * Scaffold's calculated probability that the protein identification is correct. 100% probability is stored as 1.0
	 */
	private double proteinIdentificationProbability;

	/**
	 * The number of unique peptides that matched to the identified protein.
	 */
	private int numberOfUniquePeptides;

	/**
	 * Two spectra are unique if they match different peptides (even if the peptides overlap),
	 * or match two different charge states of the same peptide, or match both a peptide and a modified form of the peptide.
	 * The Number of unique spectra is a count of these unique spectra.
	 */
	private int numberOfUniqueSpectra;

	/**
	 * How many spectra total mapped to a peptide that mapped to this protein.
	 */
	private int numberOfTotalSpectra;

	/**
	 * How many percent of total spectra mapped to this protein group? 100% is stored as 1.0
	 */
	private double percentageOfTotalSpectra;

	/**
	 * How many percent of the protein sequence were covered by identified peptides? 100% is stored as 1.0
	 */
	private double percentageSequenceCoverage;

	public Set<ProteinSequence> getProteinSequences() {
		return proteinSequences;
	}

	public void setProteinSequences(Set<ProteinSequence> proteinSequences) {
		this.proteinSequences = proteinSequences;
	}

	public Set<PeptideSpectrumMatch> getPeptideSpectrumMatches() {
		return peptideSpectrumMatches;
	}

	public void setPeptideSpectrumMatches(Set<PeptideSpectrumMatch> peptideSpectrumMatches) {
		this.peptideSpectrumMatches = peptideSpectrumMatches;
	}

	public double getProteinIdentificationProbability() {
		return proteinIdentificationProbability;
	}

	public void setProteinIdentificationProbability(double proteinIdentificationProbability) {
		this.proteinIdentificationProbability = proteinIdentificationProbability;
	}

	public int getNumberOfUniquePeptides() {
		return numberOfUniquePeptides;
	}

	public void setNumberOfUniquePeptides(int numberOfUniquePeptides) {
		this.numberOfUniquePeptides = numberOfUniquePeptides;
	}

	public int getNumberOfUniqueSpectra() {
		return numberOfUniqueSpectra;
	}

	public void setNumberOfUniqueSpectra(int numberOfUniqueSpectra) {
		this.numberOfUniqueSpectra = numberOfUniqueSpectra;
	}

	public int getNumberOfTotalSpectra() {
		return numberOfTotalSpectra;
	}

	public void setNumberOfTotalSpectra(int numberOfTotalSpectra) {
		this.numberOfTotalSpectra = numberOfTotalSpectra;
	}

	public double getPercentageOfTotalSpectra() {
		return percentageOfTotalSpectra;
	}

	public void setPercentageOfTotalSpectra(double percentageOfTotalSpectra) {
		this.percentageOfTotalSpectra = percentageOfTotalSpectra;
	}

	public double getPercentageSequenceCoverage() {
		return percentageSequenceCoverage;
	}

	public void setPercentageSequenceCoverage(double percentageSequenceCoverage) {
		this.percentageSequenceCoverage = percentageSequenceCoverage;
	}
}
