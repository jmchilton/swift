package edu.mayo.mprc.scaffoldparser.spectra;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;

import java.io.*;

/**
 * An abstract class for reading Scaffold's spectrum report. Calls abstract methods that provide the actual functionality.
 *
 * @author Roman Zenka
 */
public abstract class AbstractScaffoldSpectraReader {
	/**
	 * Default extension - as Scaffold produces it.
	 */
	public static final String EXTENSION = ".spectra.txt";

	/**
	 * Version of Scaffold that produced the report (Currently "2" for Scaffold 2 or "3" for Scaffold 3).
	 */
	private String scaffoldVersion;

	/**
	 * Name of the Scaffold spectra data source being loaded for exception handling (typically the filename).
	 */
	private final String dataSourceName;

	// Scaffold files are terminated with this marker
	private static final String END_OF_FILE = "END OF FILE";

	public static final String EXPERIMENT_NAME = "Experiment name";
	public static final String BIOLOGICAL_SAMPLE_CATEGORY = "Biological sample category";
	public static final String BIOLOGICAL_SAMPLE_NAME = "Biological sample name";
	public static final String MS_MS_SAMPLE_NAME = "MS/MS sample name";
	public static final String PROTEIN_NAME = "Protein name";
	public static final String PROTEIN_ACCESSION_NUMBERS = "Protein accession numbers";
	public static final String DATABASE_SOURCES = "Database sources";
	public static final String PROTEIN_MOLECULAR_WEIGHT_DA = "Protein molecular weight (Da)";
	public static final String PROTEIN_ID_PROBABILITY = "Protein identification probability";
	public static final String NUMBER_OF_UNIQUE_PEPTIDES = "Number of unique peptides";
	public static final String NUMBER_OF_UNIQUE_SPECTRA = "Number of unique spectra";
	public static final String NUMBER_OF_TOTAL_SPECTRA = "Number of total spectra";
	public static final String PERCENTAGE_OF_TOTAL_SPECTRA = "Percentage of total spectra";
	public static final String PERCENTAGE_SEQUENCE_COVERAGE = "Percentage sequence coverage";
	public static final String MANUAL_VALIDATION = "Manual validation";
	public static final String ASSIGNED = "Assigned";
	public static final String SPECTRUM_NAME = "Spectrum name";
	public static final String PEPTIDE_SEQUENCE = "Peptide sequence";
	public static final String PREVIOUS_AMINO_ACID = "Previous amino acid";
	public static final String NEXT_AMINO_ACID = "Next amino acid";
	public static final String PEPTIDE_ID_PROBABILITY = "Peptide identification probability";
	public static final String SEQUEST_XCORR_SCORE = "SEQUEST XCorr score";
	public static final String SEQUEST_DCN_SCORE = "SEQUEST DCn score";
	public static final String MASCOT_ION_SCORE = "Mascot Ion score";
	public static final String MASCOT_IDENTITY_SCORE = "Mascot Identity score";
	public static final String MASCOT_DELTA_ION_SCORE = "Mascot Delta Ion Score";
	public static final String X_TANDEM_LOG_E_SCORE = "X! Tandem -log(e) score";
	public static final String NUMBER_OF_ENZYMATIC_TERMINII = "Number of enzymatic termini";
	public static final String FIXED_MODIFICATIONS = "Fixed modifications identified by spectrum";
	public static final String VARIABLE_MODIFICATIONS = "Variable modifications identified by spectrum";
	public static final String OBSERVED_MZ = "Observed m/z";
	public static final String ACTUAL_PEPTIDE_MASS_AMU = "Actual peptide mass (AMU)";
	public static final String CALCULATED_1H_PEPTIDE_MASS_AMU = "Calculated +1H Peptide Mass (AMU)";
	public static final String SPECTRUM_CHARGE = "Spectrum charge";
	public static final String PEPTIDE_DELTA_AMU = "Actual minus calculated peptide mass (AMU)";
	public static final String PEPTIDE_DELTA_PPM = "Actual minus calculated peptide mass (PPM)";
	public static final String PEPTIDE_START_INDEX = "Peptide start index";
	public static final String PEPTIDE_STOP_INDEX = "Peptide stop index";
	public static final String EXCLUSIVE = "Exclusive";
	public static final String OTHER_PROTEINS = "Other Proteins";
	public static final String STARRED = "Starred";

	/**
	 * How to tell the header of the file.
	 */
	private static final String FIRST_HEADER_COLUMN = EXPERIMENT_NAME;

	/**
	 * Start loading scaffold spectra file.
	 *
	 * @param scaffoldSpectraFile Spectrum file to load.
	 * @param scaffoldVersion     {@link #scaffoldVersion}
	 */
	public AbstractScaffoldSpectraReader(File scaffoldSpectraFile, String scaffoldVersion) {
		dataSourceName = scaffoldSpectraFile.getAbsolutePath();
		this.scaffoldVersion = scaffoldVersion;
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(scaffoldSpectraFile);
			processReader(fileReader);
		} catch (Exception t) {
			throw new MprcException("Cannot parse Scaffold spectra file [" + dataSourceName + "].", t);
		} finally {
			FileUtilities.closeQuietly(fileReader);
		}
	}

	/**
	 * Start loading scaffold spectra file from a given reader.
	 *
	 * @param reader          Reader to load from. Will be closed upon load.
	 * @param dataSourceName  Information about where the spectra data came from - displayed when throwing exceptions.
	 * @param scaffoldVersion {@link #scaffoldVersion}
	 */
	public AbstractScaffoldSpectraReader(Reader reader, String dataSourceName, String scaffoldVersion) {
		this.dataSourceName = dataSourceName;
		this.scaffoldVersion = scaffoldVersion;
		try {
			processReader(reader);
		} catch (Exception t) {
			throw new MprcException("Cannot parse Scaffold spectra file [" + dataSourceName + "].", t);
		} finally {
			FileUtilities.closeQuietly(reader);
		}
	}

	private void processReader(Reader reader) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(reader);
			// Skip the header portion of the file, process the header line
			String line;
			while (true) {
				line = br.readLine();
				if (line == null) {
					throw new MprcException("End of file reached before we could find the header line in Scaffold spectra file [" + dataSourceName + "].");
				}
				if (line.startsWith(FIRST_HEADER_COLUMN + "\t")) {
					break;
				}
			}

			processHeader(line);
			loadContents(br);
		} catch (Exception e) {
			throw new MprcException("Cannot parse Scaffold spectra file [" + dataSourceName + "].", e);
		} finally {
			FileUtilities.closeQuietly(br);
		}
	}

	/**
	 * Process the Scaffold spectra file header.
	 *
	 * @param line Scaffold spectra file header, defining all the data columns. The header is tab-separated.
	 */
	public abstract void processHeader(String line);

	/**
	 * Process one row from the spectra file.
	 *
	 * @param line Scaffold spectra row, tab-separated, format matches the header supplied by {@link #processHeader(String)}.
	 */
	public abstract void processRow(String line);

	private void loadContents(BufferedReader reader) throws IOException {
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				throw new MprcException("End of file reached before finding Scaffold's " + END_OF_FILE + " marker [" + dataSourceName + "].");
			}
			if (END_OF_FILE.equals(line)) {
				break;
			}
			processRow(line);
		}
	}


	/**
	 * @return Version of Scaffold used to generate information for these spectra.
	 */
	public String getScaffoldVersion() {
		return scaffoldVersion;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}
}
