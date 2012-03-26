package edu.mayo.mprc.scaffold.report;

import edu.mayo.mprc.utilities.FileUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

final class ScaffoldReportBuilder {
	//public static final int EXPERIMENT_NAME = 0; // Experiment name
	//public static final int BIOLOGICAL_SAMPLE_CATEGORY = 1; // Biological sample category
	public static final int BIOLOGICAL_SAMPLE_NAME = 2; // Biological sample name
	//public static final int MS_MS_SAMPLE_NAME = 3; // MS/MS sample name
	public static final int PROTEIN_NAME = 4; // Protein name
	public static final int PROTEIN_ACCESSION_NUMBERS = 5; // Protein accession numbers
	//public static final int DATABASE_SOURCES = 6; // Database sources
	public static final int PROTEIN_MOLECULAR_WEIGHT_DA = 7; // Protein molecular weight (Da)
	public static final int PROTEIN_ID_PROBABILITY = 8; // Protein identification probability
	public static final int NUMBER_OF_UNIQUE_PEPTIDES = 9; // Number of unique peptides
	//public static final int NUMBER_OF_UNIQUE_SPECTRA = 10; // Number of unique spectra
	//public static final int NUMBER_OF_TOTAL_SPECTRA = 11; // Number of total spectra
	//public static final int PERCENTAGE_OF_TOTAL_SPECTRA = 12; // Percentage of total spectra
	public static final int PERCENTAGE_SEQUENCE_COVERAGE = 13; // Percentage sequence coverage
	public static final int PEPTIDE_SEQUENCE = 14; // Peptide sequence
	//public static final int PREVIOUS_AMINO_ACID = 15; // Previous amino acid
	//public static final int NEXT_AMINO_ACID = 16; // Next amino acid
	//public static final int BEST_PEPTIDE_ID_PROBABILITY = 17; // Best Peptide identification probability
	//public static final int BEST_SEQUEST_XCORR_SCORE = 18; // Best SEQUEST XCorr score
	//public static final int BEST_SEQUEST_DCN_SCORE = 19; // Best SEQUEST DCn score
	//public static final int BEST_MASCOT_ION_SCORE = 20; // Best Mascot Ion score
	//public static final int BEST_MASCOT_IDENTITY_SCORE = 21; // Best Mascot Identity score
	//public static final int BEST_X_TANDEM_LOG_E_SCORE = 22; // Best X! Tandem -log(e) score
	//public static final int NUMBER_OF_IDENTIFIED_1H_SPECTRA = 23; // Number of identified +1H spectra
	//public static final int NUMBER_OF_IDENTIFIED_2H_SPECTRA = 24; // Number of identified +2H spectra
	//public static final int NUMBER_OF_IDENTIFIED_3H_SPECTRA = 25; // Number of identified +3H spectra
	//public static final int NUMBER_OF_IDENTIFIED_4H_SPECTRA = 26; // Number of identified +4H spectra
	//public static final int NUMBER_OF_ENZYMATIC_TERMINI = 27; // Number of enzymatic termini
	//public static final int CALCULATED_1H_PEPTIDE_MASS_AMU = 28; // Calculated +1H Peptide Mass (AMU)
	//public static final int PEPTIDE_START_INDEX = 29; // Peptide start index
	//public static final int PEPTIDE_STOP_INDEX = 30; // Peptide stop index
	//public static final int ASSIGNED = 31; // Assigned
	//public static final int OTHER_PROTEINS = 32; // Other Proteins

	private static final int[] PEPTIDE_COLUMNS = new int[]{
			BIOLOGICAL_SAMPLE_NAME, // 0
			PROTEIN_NAME, // 1
			PROTEIN_ACCESSION_NUMBERS, // 2
			PROTEIN_MOLECULAR_WEIGHT_DA, // 3
			PROTEIN_ID_PROBABILITY, // 4
			NUMBER_OF_UNIQUE_PEPTIDES, // 5
			PERCENTAGE_SEQUENCE_COVERAGE, // 6
			PEPTIDE_SEQUENCE // 7
	};
	private static final ListComparator PEPTIDE_COMPARATOR = new ListComparator(
			new int[]{0 /* group by */, 5, 2, 7},
			new String[]{"a", "di", "a", "a"});
	private static final int PEPTIDE_GROUP_BY = 0;


	private static final int[] PROTEIN_COLUMNS = new int[]{
			BIOLOGICAL_SAMPLE_NAME, // 0
			PROTEIN_NAME, // 1
			PROTEIN_ACCESSION_NUMBERS, // 2
			PROTEIN_MOLECULAR_WEIGHT_DA, // 3
			PROTEIN_ID_PROBABILITY, // 4
			NUMBER_OF_UNIQUE_PEPTIDES, // 5
			PERCENTAGE_SEQUENCE_COVERAGE // 6
	};
	private static final ListComparator PROTEIN_COMPARATOR = new ListComparator(
			new int[]{0 /* group by */, 5, 2},
			new String[]{"a", "di", "a"});
	private static final int PROTEIN_GROUP_BY = 0;


	private ScaffoldReportBuilder() {
	}

	/**
	 * Builds an easy to read XLS output from multiple scaffold peptide reports.
	 * <p/>
	 * The output is grouped by biological sample name. Each sample is separated by a blank line from other samples.
	 * Within a sample, the rows are ordered by
	 * <ol>
	 * <li>number of unique peptides, descending</li>
	 * <li>peptide accession number, ascending</li>
	 * <li>peptide sequence, ascending</li>
	 * </ol>
	 *
	 * @param inputReports        Input files (Scaffold peptide report).
	 * @param outputPeptideReport Output .xls file for peptide-level list (actually tab-separated).
	 * @param outputProteinReport Output .xls file for protein-level list (actually tab-separated) - differs from peptide-level by omitting the peptide sequence.
	 */
	public static void buildReport(final List<File> inputReports, final File outputPeptideReport, final File outputProteinReport) throws IOException {

		BufferedWriter peptideWriter = null;
		BufferedWriter proteinWriter = null;

		try {
			peptideWriter = new BufferedWriter(new FileWriter(outputPeptideReport));
			proteinWriter = new BufferedWriter(new FileWriter(outputProteinReport));
			boolean first = true;
			for (final File inputReport : inputReports) {
				//Leave a empty line between tables.
				if (!first) {
					peptideWriter.newLine();
					peptideWriter.newLine();
					proteinWriter.newLine();
					proteinWriter.newLine();
				}

				peptideWriter.write(getPeptideList(inputReport, first));
				proteinWriter.write(getProteinList(inputReport, first));

				first = false;
			}

		} finally {
			FileUtilities.closeQuietly(peptideWriter);
			FileUtilities.closeQuietly(proteinWriter);
			FileUtilities.restoreUmaskRights(outputPeptideReport, false);
			FileUtilities.restoreUmaskRights(outputProteinReport, false);
		}
	}

	private static String getPeptideList(final File inputReport, final boolean includeHeaders) throws IOException {
		final ScaffoldOutputReader scaffoldOutputReader = new ScaffoldOutputReader(inputReport);
		try {
			return scaffoldOutputReader.getRowSortedDataTableContent(includeHeaders, PEPTIDE_COLUMNS, PEPTIDE_GROUP_BY, PEPTIDE_COMPARATOR);
		} finally {
			FileUtilities.closeQuietly(scaffoldOutputReader);
		}
	}

	private static String getProteinList(final File inputReport, final boolean includeHeaders) throws IOException {
		final ScaffoldOutputReader scaffoldOutputReader = new ScaffoldOutputReader(inputReport);
		try {
			return scaffoldOutputReader.getRowSortedDataTableContent(includeHeaders, PROTEIN_COLUMNS, PROTEIN_GROUP_BY, PROTEIN_COMPARATOR);
		} finally {
			FileUtilities.closeQuietly(scaffoldOutputReader);
		}
	}
}
