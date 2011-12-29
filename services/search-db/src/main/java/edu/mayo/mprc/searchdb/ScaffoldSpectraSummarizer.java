package edu.mayo.mprc.searchdb;

import com.google.common.base.Splitter;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.scaffoldparser.spectra.AbstractScaffoldSpectraReader;
import edu.mayo.mprc.searchdb.dao.Analysis;
import edu.mayo.mprc.searchdb.dao.BiologicalSample;
import edu.mayo.mprc.searchdb.dao.ProteinGroup;
import edu.mayo.mprc.searchdb.dao.TandemMassSpectrometrySearchResult;

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
public class ScaffoldSpectraSummarizer extends AbstractScaffoldSpectraReader {
	private static final String REPORT_DATE_PREFIX_KEY = "Spectrum report created on ";
	private static final String SCAFFOLD_VERSION_KEY = "Scaffold Version";
	private static final Splitter SPLITTER = Splitter.on('\t').trimResults();
	private static final double HUNDRED_PERCENT = 100.0;

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

	public ScaffoldSpectraSummarizer() {
		analysis = new Analysis();
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
		HashMap<String, Integer> columnPositions = buildColumnMap(line);
		numColumns = columnPositions.size();
		currentLine = new String[numColumns];

		biologicalSampleName = getColumn(columnPositions, AbstractScaffoldSpectraReader.BIOLOGICAL_SAMPLE_NAME);
		biologicalSampleCategory = getColumn(columnPositions, AbstractScaffoldSpectraReader.BIOLOGICAL_SAMPLE_CATEGORY);
		msmsSampleName = getColumn(columnPositions, AbstractScaffoldSpectraReader.MS_MS_SAMPLE_NAME);
		proteinAccessionNumbers = getColumn(columnPositions, AbstractScaffoldSpectraReader.PROTEIN_ACCESSION_NUMBERS);
		numberOfTotalSpectra = getColumn(columnPositions, AbstractScaffoldSpectraReader.NUMBER_OF_TOTAL_SPECTRA);
		numberOfUniqueSpectra = getColumn(columnPositions, AbstractScaffoldSpectraReader.NUMBER_OF_UNIQUE_SPECTRA);
		numberOfUniquePeptides = getColumn(columnPositions, AbstractScaffoldSpectraReader.NUMBER_OF_UNIQUE_PEPTIDES);
		percentageOfTotalSpectra = getColumn(columnPositions, AbstractScaffoldSpectraReader.PERCENTAGE_OF_TOTAL_SPECTRA);
		percentageSequenceCoverage = getColumn(columnPositions, AbstractScaffoldSpectraReader.PERCENTAGE_SEQUENCE_COVERAGE);
		proteinIdentificationProbability = getColumn(columnPositions, AbstractScaffoldSpectraReader.PROTEIN_ID_PROBABILITY);
		peptideSequence = getColumn(columnPositions, AbstractScaffoldSpectraReader.PEPTIDE_SEQUENCE);
		previousAminoAcid = getColumn(columnPositions, AbstractScaffoldSpectraReader.PREVIOUS_AMINO_ACID);
		nextAminoAcid = getColumn(columnPositions, AbstractScaffoldSpectraReader.NEXT_AMINO_ACID);
		numberOfEnzymaticTerminii = getColumn(columnPositions, AbstractScaffoldSpectraReader.NUMBER_OF_ENZYMATIC_TERMINII);
		fixedModifications = getColumn(columnPositions, AbstractScaffoldSpectraReader.FIXED_MODIFICATIONS);
		variableModifications = getColumn(columnPositions, AbstractScaffoldSpectraReader.VARIABLE_MODIFICATIONS);
		spectrumName = getColumn(columnPositions, AbstractScaffoldSpectraReader.SPECTRUM_NAME);
		spectrumCharge = getColumn(columnPositions, AbstractScaffoldSpectraReader.SPECTRUM_CHARGE);
		peptideIdentificationProbability = getColumn(columnPositions, AbstractScaffoldSpectraReader.PEPTIDE_ID_PROBABILITY);

		getColumn(columnPositions, AbstractScaffoldSpectraReader.MASCOT_ION_SCORE);
		getColumn(columnPositions, AbstractScaffoldSpectraReader.MASCOT_IDENTITY_SCORE);
		getColumn(columnPositions, AbstractScaffoldSpectraReader.MASCOT_DELTA_ION_SCORE);
		getColumn(columnPositions, AbstractScaffoldSpectraReader.SEQUEST_XCORR_SCORE);
		getColumn(columnPositions, AbstractScaffoldSpectraReader.SEQUEST_DCN_SCORE);
		getColumn(columnPositions, AbstractScaffoldSpectraReader.X_TANDEM_LOG_E_SCORE);

		// Prepare for loading all the data
		analysis.setBiologicalSamples(new ArrayList<BiologicalSample>(5));
		cache = new SummarizerCache();
	}

	private int getColumn(HashMap<String, Integer> columnPositions, String columnName) {
		final Integer column = columnPositions.get(columnName);
		if (null == column) {
			throw new MprcException("Missing column [" + columnName + "]");
		} else {
			return column;
		}
	}

	@Override
	public void processRow(String line) {
		fillCurrentLine(line);
		final BiologicalSample biologicalSample = cache.getBiologicalSample(analysis, currentLine[biologicalSampleName], currentLine[biologicalSampleCategory]);
		final TandemMassSpectrometrySearchResult tandemMassSpecResult = cache.getTandemMassSpecResult(biologicalSample, currentLine[msmsSampleName]);
		final ProteinGroup proteinGroup = cache.getProteinGroup(biologicalSample,
				tandemMassSpecResult,
				currentLine[proteinAccessionNumbers],

				parseInt(currentLine[numberOfTotalSpectra]),
				parseInt(currentLine[numberOfUniquePeptides]),
				parseInt(currentLine[numberOfUniqueSpectra]),
				parseDouble(currentLine[percentageOfTotalSpectra]) / HUNDRED_PERCENT,
				parseDouble(currentLine[percentageSequenceCoverage]) / HUNDRED_PERCENT,
				parseDouble(currentLine[proteinIdentificationProbability]) / HUNDRED_PERCENT);

//		final PeptideSpectrumMatch peptideSpectrumMatch =
//				cache.getPeptideSpectrumMatch(
//						biologicalSample,
//						proteinGroup,
//
//						currentLine[peptideSequence],
//						currentLine[fixedModifications],
//						currentLine[variableModifications],
//
//						currentLine[previousAminoAcid],
//						currentLine[nextAminoAcid],
//						currentLine[numberOfEnzymaticTerminii]);
//
//		cache.recordSpectrum(peptideSpectrumMatch,
//				currentLine[spectrumName],
//				parseInt(currentLine[spectrumCharge]),
//
//				parseDouble(currentLine[peptideIdentificationProbability]) / HUNDRED_PERCENT,
//				parseDouble(currentLine)
//		);

//		private double bestPeptideIdentificationProbability;
//		private double bestSequestXcorrScore;
//		private double bestSequestDcnScore;
//		private double bestMascotIonScore;
//		private double bestMascotIdentityScore;
//		private double bestMascotDeltaIonScore;
//		private double bestXTandemLogEScore;
//		private int numberOfIdentifiedSpectra;
//		private int numberOfIdentified1HSpectra;
//		private int numberOfIdentified2HSpectra;
//		private int numberOfIdentified3HSpectra;
//		private int numberOfIdentified4HSpectra;

	}

	private int parseInt(String s) {
		try {
			return Integer.parseInt(fixCommaSeparatedThousands(s));
		} catch (NumberFormatException e) {
			throw new MprcException("Cannot parse number [" + s + "] as integer.", e);
		}
	}

	private double parseDouble(String s) {
		try {
			return Double.parseDouble(fixCommaSeparatedThousands(cutPercentSign(s)));
		} catch (NumberFormatException e) {
			throw new MprcException("Cannot parse number [" + s + "] as real number.", e);
		}
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
		Date parsedDate = null;
		parsedDate = tryParse(date, DateFormat.getDateTimeInstance(), parsedDate);
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
			columnPositions.put(column, position);
			position++;
		}
		return columnPositions;
	}
}
