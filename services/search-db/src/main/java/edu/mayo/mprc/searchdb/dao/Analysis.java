package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Extracted information about how the analysis was performed that should get stored into the LIMS.
 *
 * @author Roman Zenka
 */
public final class Analysis extends PersistableBase {
	private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00%", DecimalFormatSymbols.getInstance(Locale.US));
	/**
	 * Scaffold version as a string. Can be null if the version could not be determined.
	 */
	private String scaffoldVersion;

	/**
	 * Date and time of when the analysis was run, as recorded in the report. This information
	 * is somewhat duplicated (we know when the user submitted the search).
	 * It can be null if the date could not be determined.
	 */
	private Date analysisDate;

	/**
	 * A list of all biological samples defined within the Scaffold analysis report.
	 */
	private List<BiologicalSample> biologicalSamples;

	/**
	 * Empty constructor for Hibernate.
	 */
	public Analysis() {
	}

	public Analysis(String scaffoldVersion, Date analysisDate, List<BiologicalSample> biologicalSamples) {
		this.scaffoldVersion = scaffoldVersion;
		this.analysisDate = analysisDate;
		this.biologicalSamples = biologicalSamples;
	}

	public String getScaffoldVersion() {
		return scaffoldVersion;
	}

	public void setScaffoldVersion(String scaffoldVersion) {
		this.scaffoldVersion = scaffoldVersion;
	}

	public Date getAnalysisDate() {
		return analysisDate;
	}

	public void setAnalysisDate(Date analysisDate) {
		this.analysisDate = analysisDate;
	}

	public List<BiologicalSample> getBiologicalSamples() {
		return biologicalSamples;
	}

	public void setBiologicalSamples(List<BiologicalSample> biologicalSamples) {
		this.biologicalSamples = biologicalSamples;
	}

	/**
	 * Emulates the Scaffold's peptide report.
	 *
	 * @return String similar to Scaffold's peptide report. For testing mostly.
	 */
	public String peptideReport() {
		StringBuilder builder = new StringBuilder();
		builder.append(
				"Experiment name\t" +
						"Biological sample category\t" +
						"Biological sample name\t" +
						"MS/MS sample name\t" +
						"Protein name\t" +
						"Protein accession numbers\t" +
						"Database sources\t" +
						"Protein molecular weight (Da)\t" +
						"Protein identification probability\t" +
						"Number of Unique Peptides\t" +
						"Number of Unique Spectra\t" +
						"Number of Total Spectra\t" +
						"Percentage of Total Spectra\t" +
						"Percentage Sequence Coverage\t" +
						"Peptide Sequence\t" +
						"Previous Amino Acid\t" +
						"Next Amino Acid\t" +
						"Best Peptide Identification Probability\t" +
						"Best SEQUEST XCorr score\t" +
						"Best SEQUEST DCn score\t" +
						"Best Mascot Ion score\t" +
						"Best Mascot Identity score\t" +
						"Best Mascot Delta Ion score\t" +
						"Best X! Tandem -log(e) score\t" +
						"Number of identified +1H spectra\t" +
						"Number of identified +2H spectra\t" +
						"Number of identified +3H spectra\t" +
						"Number of identified +4H spectra\t" +
						"Number of enzymatic termini\n");
		for (BiologicalSample sample : getBiologicalSamples()) {
			for (SearchResult result : sample.getSearchResults()) {
				for (ProteinGroup proteinGroup : result.getProteinGroups()) {
					for (PeptideSpectrumMatch psm : proteinGroup.getPeptideSpectrumMatches()) {
						builder
								.append("").append('\t')
								.append(sample.getCategory()).append('\t')
								.append(sample.getSampleName()).append('\t')
								.append("").append('\t')
								.append("").append('\t')
								.append("").append('\t')
								.append("").append('\t')
								.append("").append('\t')
								.append(percent(proteinGroup.getProteinIdentificationProbability())).append('\t')
								.append(proteinGroup.getNumberOfUniquePeptides()).append('\t')
								.append(proteinGroup.getNumberOfUniqueSpectra()).append('\t')
								.append(proteinGroup.getNumberOfTotalSpectra()).append('\t')
								.append(percent(proteinGroup.getPercentageOfTotalSpectra())).append('\t')
								.append(percent(proteinGroup.getPercentageSequenceCoverage())).append('\t')
								.append(psm.getPeptide().getSequence().getSequence()).append('\t')
								.append(psm.getPreviousAminoAcid()).append('\t')
								.append(psm.getNextAminoAcid()).append('\t')
								.append(psm.getBestPeptideIdentificationProbability()).append('\t')
								.append(psm.getBestSearchEngineScores().getSequestXcorrScore()).append('\t')
								.append(psm.getBestSearchEngineScores().getSequestDcnScore()).append('\t')
								.append(psm.getBestSearchEngineScores().getMascotIonScore()).append('\t')
								.append(psm.getBestSearchEngineScores().getMascotIdentityScore()).append('\t')
								.append(psm.getBestSearchEngineScores().getMascotDeltaIonScore()).append('\t')
								.append(psm.getBestSearchEngineScores().getTandemHyperScore()).append('\t')
								.append(psm.getSpectrumIdentificationCounts().getNumberOfIdentified1HSpectra()).append('\t')
								.append(psm.getSpectrumIdentificationCounts().getNumberOfIdentified2HSpectra()).append('\t')
								.append(psm.getSpectrumIdentificationCounts().getNumberOfIdentified3HSpectra()).append('\t')
								.append(psm.getSpectrumIdentificationCounts().getNumberOfIdentified4HSpectra()).append('\t')
								.append(psm.getNumberOfEnzymaticTerminii()).append('\n');
					}
				}
			}
		}
		return builder.toString();
	}

	private String percent(double percent) {
		return PERCENT_FORMAT.format(percent);
	}
}
