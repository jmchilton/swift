package edu.mayo.mprc.searchdb;

import com.google.common.base.Splitter;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.chem.AminoAcidSet;
import edu.mayo.mprc.scaffoldparser.spectra.ScaffoldSpectraReader;
import edu.mayo.mprc.searchdb.dao.*;
import edu.mayo.mprc.unimod.IndexedModSet;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Summarizes Scaffold spectra report into a collection of objects suitable to be loaded into the database.
 * The object that should be identical are allocated just once - e.g. two peptides with identical modifications will
 * be stored as a single object. Our goal is to limit the database space needed as these tables can grow very large
 * with many experiments.
 * <p/>
 * We assume the report was generated with Scaffold Batch version 3. We make this true upstream by rerunning Scaffold if the
 * versions do not match.
 *
 * @author Roman Zenka
 */
public class ScaffoldSpectraSummarizer extends ScaffoldSpectraReader {
	private static final String REPORT_DATE_PREFIX_KEY = "Spectrum report created on ";
	private static final String SCAFFOLD_VERSION_KEY = "Scaffold Version";
	private static final Splitter SPLITTER = Splitter.on('\t').trimResults();
	private static final double HUNDRED_PERCENT = 100.0;
	private static final AminoAcidSet SUPPORTED_AMINO_ACIDS = AminoAcidSet.DEFAULT;

	/**
	 * To parse Scaffold-reported mods.
	 */
	private ScaffoldModificationFormat format;
	private Analysis analysis;
	private SummarizerCache cache;

	// Current line parsed into columns, with data in fields trimmed
	private String[] currentLine;

	// Column indices for different fields.
	private int biologicalSampleName;
	private int biologicalSampleCategory;
	private int numColumns;
	private int msmsSampleName;
	private int proteinAccessionNumbers;
	private int numberOfTotalSpectra;
	private int numberOfUniqueSpectra;
	private int numberOfUniquePeptides;
	private int percentageOfTotalSpectra;
	private int percentageSequenceCoverage;
	private int proteinIdentificationProbability;
	private int peptideSequence;
	private int previousAminoAcid;
	private int nextAminoAcid;
	private int numberOfEnzymaticTerminii;
	private int fixedModifications;
	private int variableModifications;
	private int spectrumName;
	private int spectrumCharge;
	private int peptideIdentificationProbability;

	private int sequestXcorrScore;
	private int sequestDcnScore;
	private int sequestSp;
	private int sequestSpRank;
	private int sequestPeptidesMatched;
	private int mascotIonScore;
	private int mascotIdentityScore;
	private int mascotHomologyScore;
	private int mascotDeltaIonScore;
	private int tandemHyperScore;
	private int tandemLadderScore;

	public ScaffoldSpectraSummarizer(IndexedModSet modSet) {
		analysis = new Analysis();
		format = new ScaffoldModificationFormat(modSet);
	}

	/**
	 * @return Result of the parse process.
	 */
	public Analysis getAnalysis() {
		return analysis;
	}

	@Override
	public void processMetadata(String key, String value) {
		if (key == null) {
			int datePos = value.indexOf(REPORT_DATE_PREFIX_KEY);
			if (datePos >= 0) {
				String date = value.substring(datePos + REPORT_DATE_PREFIX_KEY.length());
				final Date reportDate = parseAnalysisDate(date);
				analysis.setAnalysisDate(reportDate);
			}
		} else if (SCAFFOLD_VERSION_KEY.equalsIgnoreCase(key)) {
			analysis.setScaffoldVersion(value);
		}
	}

	@Override
	public void processHeader(String line) {
		HashMap<String, Integer> map = buildColumnMap(line);
		numColumns = map.size();
		currentLine = new String[numColumns];

		// Store the column numbers for faster parsing
		biologicalSampleName = getColumn(map, ScaffoldSpectraReader.BIOLOGICAL_SAMPLE_NAME);
		biologicalSampleCategory = getColumn(map, ScaffoldSpectraReader.BIOLOGICAL_SAMPLE_CATEGORY);
		msmsSampleName = getColumn(map, ScaffoldSpectraReader.MS_MS_SAMPLE_NAME);
		proteinAccessionNumbers = getColumn(map, ScaffoldSpectraReader.PROTEIN_ACCESSION_NUMBERS);
		numberOfTotalSpectra = getColumn(map, ScaffoldSpectraReader.NUMBER_OF_TOTAL_SPECTRA);
		numberOfUniqueSpectra = getColumn(map, ScaffoldSpectraReader.NUMBER_OF_UNIQUE_SPECTRA);
		numberOfUniquePeptides = getColumn(map, ScaffoldSpectraReader.NUMBER_OF_UNIQUE_PEPTIDES);
		percentageOfTotalSpectra = getColumn(map, ScaffoldSpectraReader.PERCENTAGE_OF_TOTAL_SPECTRA);
		percentageSequenceCoverage = getColumn(map, ScaffoldSpectraReader.PERCENTAGE_SEQUENCE_COVERAGE);
		proteinIdentificationProbability = getColumn(map, ScaffoldSpectraReader.PROTEIN_ID_PROBABILITY);
		peptideSequence = getColumn(map, ScaffoldSpectraReader.PEPTIDE_SEQUENCE);
		previousAminoAcid = getColumn(map, ScaffoldSpectraReader.PREVIOUS_AMINO_ACID);
		nextAminoAcid = getColumn(map, ScaffoldSpectraReader.NEXT_AMINO_ACID);
		numberOfEnzymaticTerminii = getColumn(map, ScaffoldSpectraReader.NUMBER_OF_ENZYMATIC_TERMINII);
		fixedModifications = getColumn(map, ScaffoldSpectraReader.FIXED_MODIFICATIONS);
		variableModifications = getColumn(map, ScaffoldSpectraReader.VARIABLE_MODIFICATIONS);
		spectrumName = getColumn(map, ScaffoldSpectraReader.SPECTRUM_NAME);
		spectrumCharge = getColumn(map, ScaffoldSpectraReader.SPECTRUM_CHARGE);
		peptideIdentificationProbability = getColumn(map, ScaffoldSpectraReader.PEPTIDE_ID_PROBABILITY);

		sequestXcorrScore = getColumnOptional(map, ScaffoldSpectraReader.SEQUEST_XCORR_SCORE);
		sequestDcnScore = getColumnOptional(map, ScaffoldSpectraReader.SEQUEST_DCN_SCORE);
		sequestSp = getColumnOptional(map, ScaffoldSpectraReader.SEQUEST_SP);
		sequestSpRank = getColumnOptional(map, ScaffoldSpectraReader.SEQUEST_SP_RANK);
		sequestPeptidesMatched = getColumnOptional(map, ScaffoldSpectraReader.SEQUEST_PEPTIDES_MATCHED);
		mascotIonScore = getColumnOptional(map, ScaffoldSpectraReader.MASCOT_ION_SCORE);
		mascotIdentityScore = getColumnOptional(map, ScaffoldSpectraReader.MASCOT_IDENTITY_SCORE);
		mascotHomologyScore = getColumnOptional(map, ScaffoldSpectraReader.MASCOT_HOMOLOGY_SCORE);
		mascotDeltaIonScore = getColumnOptional(map, ScaffoldSpectraReader.MASCOT_DELTA_ION_SCORE);
		tandemHyperScore = getColumnOptional(map, ScaffoldSpectraReader.X_TANDEM_HYPER_SCORE);
		tandemLadderScore = getColumnOptional(map, ScaffoldSpectraReader.X_TANDEM_LADDER_SCORE);

		// Prepare for loading all the data
		analysis.setBiologicalSamples(new ArrayList<BiologicalSample>(5));
		cache = new SummarizerCache(format);
	}

	private int getColumn(HashMap<String, Integer> columnPositions, String columnName) {
		final Integer column = getColumnNumber(columnPositions, columnName);
		if (null == column) {
			throw new MprcException("Missing column [" + columnName + "]");
		} else {
			return column;
		}
	}

	private int getColumnOptional(HashMap<String, Integer> columnPositions, String columnName) {
		final Integer column = getColumnNumber(columnPositions, columnName);
		return column == null ? -1 : column;
	}

	private Integer getColumnNumber(HashMap<String, Integer> columnPositions, String columnName) {
		return columnPositions.get(columnName.toUpperCase(Locale.US));
	}

	@Override
	public void processRow(String line) {
		fillCurrentLine(line);
		final BiologicalSample biologicalSample = cache.getBiologicalSample(analysis, currentLine[biologicalSampleName], currentLine[biologicalSampleCategory]);
		final SearchResult searchResult = cache.getTandemMassSpecResult(biologicalSample, currentLine[msmsSampleName]);
		final ProteinGroup proteinGroup = cache.getProteinGroup(biologicalSample,
				searchResult,
				currentLine[proteinAccessionNumbers],

				parseInt(currentLine[numberOfTotalSpectra]),
				parseInt(currentLine[numberOfUniquePeptides]),
				parseInt(currentLine[numberOfUniqueSpectra]),
				parseDouble(currentLine[percentageOfTotalSpectra]) / HUNDRED_PERCENT,
				parseDouble(currentLine[percentageSequenceCoverage]) / HUNDRED_PERCENT,
				parseDouble(currentLine[proteinIdentificationProbability]) / HUNDRED_PERCENT);

		final PeptideSpectrumMatch peptideSpectrumMatch =
				cache.getPeptideSpectrumMatch(
						biologicalSample,
						searchResult,
						proteinGroup,

						currentLine[peptideSequence],
						currentLine[fixedModifications],
						currentLine[variableModifications],

						parseAminoAcid(currentLine[previousAminoAcid]),
						parseAminoAcid(currentLine[nextAminoAcid]),
						parseInt(currentLine[numberOfEnzymaticTerminii]));

		cache.recordSpectrum(peptideSpectrumMatch,
				currentLine[spectrumName],
				parseInt(currentLine[spectrumCharge]),
				parseDouble(currentLine[peptideIdentificationProbability]) / HUNDRED_PERCENT,
				new SearchEngineScores(
						parseOptionalDouble(sequestXcorrScore),
						parseOptionalDouble(sequestDcnScore),
						parseOptionalDouble(sequestSp),
						parseOptionalDouble(sequestSpRank),
						parseOptionalDouble(sequestPeptidesMatched),
						parseOptionalDouble(mascotIonScore),
						parseOptionalDouble(mascotIdentityScore),
						parseOptionalDouble(mascotHomologyScore),
						parseOptionalDouble(mascotDeltaIonScore),
						parseOptionalDouble(tandemHyperScore),
						parseOptionalDouble(tandemLadderScore))
		);
	}

	/**
	 * Check that the string corresponds to a single known amino acid.
	 *
	 * @param s String to check.
	 * @return One-char amino acid code.
	 */
	private char parseAminoAcid(String s) {
		if (s == null || s.length() != 1) {
			throw new MprcException("Wrong single-letter format for an amino acid: [" + s + "]");
		}
		char aminoAcid = s.charAt(0);
		if (aminoAcid != '-' && SUPPORTED_AMINO_ACIDS.getForSingleLetterCode(s) == null) {
			throw new MprcException("Unsupported amino acid code [" + aminoAcid + "]");
		}
		return Character.toUpperCase(aminoAcid);
	}

	private int parseInt(String s) {
		try {
			return Integer.parseInt(fixCommaSeparatedThousands(s));
		} catch (NumberFormatException e) {
			throw new MprcException("Cannot parse number [" + s + "] as integer.", e);
		}
	}

	/**
	 * Parse a double number. If the number is missing, Double.NaN is returned
	 *
	 * @param s
	 * @return
	 */
	private double parseDouble(String s) {
		if ("".equals(s)) {
			return Double.NaN;
		}
		try {
			return Double.parseDouble(fixCommaSeparatedThousands(cutPercentSign(s)));
		} catch (NumberFormatException e) {
			throw new MprcException("Cannot parse number [" + s + "] as real number.", e);
		}
	}

	/**
	 * @param column Column index to convert to double.
	 * @return If column not defined (-1), return Double.NaN, otherwise parse the value for the column and return that.
	 */
	private double parseOptionalDouble(int column) {
		if (column < 0) {
			return Double.NaN;
		}
		return parseDouble(currentLine[column]);
	}

	private static String cutPercentSign(String s) {
		return s.endsWith("%") ? s.substring(0, s.length() - 1) : s;
	}

	private void fillCurrentLine(String line) {
		final Iterator<String> iterator = SPLITTER.split(line).iterator();
		for (int i = 0; i < currentLine.length; i++) {
			if (iterator.hasNext()) {
				currentLine[i] = iterator.next();
			} else {
				currentLine[i] = "";
			}
		}
	}

	/**
	 * Try multiple formats to parse the date.
	 *
	 * @param date Date from Scaffold.
	 * @return Parsed date.
	 */
	private static Date parseAnalysisDate(String date) {
		Date parsedDate = tryParse(date, DateFormat.getDateTimeInstance(), null);
		parsedDate = tryParse(date, DateFormat.getDateInstance(), parsedDate);
		parsedDate = tryParse(date, DateFormat.getDateInstance(DateFormat.SHORT, Locale.US), parsedDate);
		return parsedDate;
	}

	private static Date tryParse(String date, DateFormat format, Date parsedDate) {
		if (parsedDate == null) {
			try {
				parsedDate = format.parse(date);
			} catch (ParseException ignore) {
				// SWALLOWED - try another option
			}
		}
		return parsedDate;
	}

	private static HashMap<String, Integer> buildColumnMap(String line) {
		HashMap<String, Integer> columnPositions = new HashMap<String, Integer>(30);
		int position = 0;
		for (String column : SPLITTER.split(line)) {
			columnPositions.put(column.toUpperCase(Locale.US), position);
			position++;
		}
		return columnPositions;
	}
}
