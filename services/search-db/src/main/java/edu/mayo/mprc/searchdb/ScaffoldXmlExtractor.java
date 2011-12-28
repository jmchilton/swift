package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.scaffoldparser.Experiment;
import edu.mayo.mprc.scaffoldparser.ProteinGroup;
import edu.mayo.mprc.scaffoldparser.Scaffold;
import edu.mayo.mprc.scaffoldparser.ScaffoldParser;
import edu.mayo.mprc.scafml.ScafmlExport;
import edu.mayo.mprc.searchdb.dao.*;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Loads information from Scaffold.xml file and summarizes it on peptide level.
 * <p/>
 * See {@link ScaffoldParser} and {@link ScafmlExport#appendScaffoldXmlExport(StringBuilder, String)}
 * for more information about this format.
 * <p/>
 * Since the Scaffold parser loads entire .xml into the memory, we can create the full dataset and present it
 * as a whole, instead of streaming.
 * <p/>
 * The information presented has following levels:
 * <p/>
 * <ol>
 * <li>{@link Analysis} - for the entire invocation of Scaffold. Mostly to provide Scaffold version</li>
 * <li>{@link BiologicalSample} - A biological sample denotes what goes together. It is important to know, because
 * protein-level calculations and thresholds are done per sample.</li>
 * <li>{@link TandemMassSpectrometrySearchResult}</li> - search results for a particular mass spec sample from one run on the instrument.
 * <li>{@link PeptideIdentification} - Each mass spec sample has multiple peptides identified. Provide total count of spectra for given peptide+modification combination.</li>
 * </ol>
 *
 * @author Roman Zenka
 */
public class ScaffoldXmlExtractor {
	private static final int TYPICAL_BIOLOGICAL_SAMPLES = 10;
	private static final int TYPICAL_MASS_SPEC_PER_BIOLOGICAL = 2;
	private static final int TYPICAL_PEPTIDE_IDS_PER_SAMPLE = 100;
	private static final String FRACTION_NAME_EXTENSION = ".tar.gz";

	/**
	 * An object capable of extracting information from mass spec files. This object would
	 * be typically dependent on the context of a particular search, because names of files will be typically provided
	 * without absolute paths.
	 */
	private MassSpecDataExtractor dataExtractor;

	public MassSpecDataExtractor getDataExtractor() {
		return dataExtractor;
	}

	public void setDataExtractor(MassSpecDataExtractor dataExtractor) {
		this.dataExtractor = dataExtractor;
	}

	/**
	 * Loads Scaffold.xml export file and populates the summaries.
	 *
	 * @param scaffoldStream Scaffold .xml stream to load.
	 * @return The main object summarizing information about entire Scaffold analysis.
	 */
	public Analysis load(InputStream scaffoldStream) {
		final Scaffold scaffold = ScaffoldParser.loadScaffoldXml(scaffoldStream);

		Date analysisDate = null;
		boolean firstExperiment = true;
		ArrayList<BiologicalSample> biologicalSamples = new ArrayList<BiologicalSample>(TYPICAL_BIOLOGICAL_SAMPLES);

		for (Experiment experiment : scaffold.getExperiments()) {
			if (firstExperiment) {
				firstExperiment = false;
				analysisDate = getAnalysisDate(experiment);
			}

			for (edu.mayo.mprc.scaffoldparser.BiologicalSample biologicalSample : experiment.getBiologicalSamples()) {
				biologicalSamples.add(processBiologicalSample(biologicalSample));
			}
		}

		return new Analysis(scaffold.getVersion(), analysisDate, biologicalSamples);
	}

	/**
	 * Turn Scaffold-parsed biological sample info into our data structure.
	 * This simply maps all the mass spec samples within the biological one.
	 *
	 * @param biologicalSample Information from Scaffold.
	 * @return Our extracted data for biological sample.
	 */
	private BiologicalSample processBiologicalSample(edu.mayo.mprc.scaffoldparser.BiologicalSample biologicalSample) {
		List<TandemMassSpectrometrySearchResult> searchResults = new ArrayList<TandemMassSpectrometrySearchResult>(TYPICAL_MASS_SPEC_PER_BIOLOGICAL);
		for (edu.mayo.mprc.scaffoldparser.TandemMassSpectrometrySample massSpectrometrySample : biologicalSample.getTandemMassSpectrometrySamples()) {
			TandemMassSpectrometrySearchResult searchResult = processTandemMassSpectrometrySample(biologicalSample, massSpectrometrySample);
			searchResults.add(searchResult);
		}
		return new BiologicalSample(biologicalSample.getSampleName(), biologicalSample.getCategory(), searchResults);
	}

	/**
	 * The process of obtaining mass spectrometry search results is somewhat tricky. We need to map the fraction information
	 * from the file to actual file. Then we need to extract data from the file to fill in our data structures. We
	 * do not have enough information to do this properly, so we rely on a previously provided interface to do it for us.
	 *
	 * @param biologicalSample The biological sample containing this mass-spec sample.
	 * @param massSpecSample   Scaffold tandem mass spectrum sample to process.
	 * @return Processed, extracted information.
	 */
	private TandemMassSpectrometrySearchResult processTandemMassSpectrometrySample(
			edu.mayo.mprc.scaffoldparser.BiologicalSample biologicalSample,
			edu.mayo.mprc.scaffoldparser.TandemMassSpectrometrySample massSpecSample) {

		String biologicalSampleName = biologicalSample.getSampleName();
		String fractionName = extractFractionName(massSpecSample.getFractionName());
		TandemMassSpectrometrySample massSpecInfo = dataExtractor.getTandemMassSpectrometrySample(biologicalSampleName, fractionName);

		List<PeptideIdentification> peptideIds = new ArrayList<PeptideIdentification>(TYPICAL_PEPTIDE_IDS_PER_SAMPLE);
		for (ProteinGroup group : massSpecSample.getProteinGroups()) {

		}

		return new TandemMassSpectrometrySearchResult(massSpecInfo, peptideIds);
	}

	/**
	 * @param fractionName Fraction name as specified in the Scaffold .xml file.
	 * @return The standalone name that can be matched against an input file.
	 */
	private static String extractFractionName(String fractionName) {
		if (fractionName.endsWith(FRACTION_NAME_EXTENSION)) {
			return fractionName.substring(0, fractionName.length() - FRACTION_NAME_EXTENSION.length());
		}
		return fractionName;
	}

	private static Date getAnalysisDate(Experiment experiment) {
		try {
			final DateFormat dateFormat = getDateFormat();
			return dateFormat.parse(experiment.getAnalysisDate());
		} catch (ParseException ignore) {
			// SWALLOWED: If we cannot parse the date, we just report null
			return null;
		}
	}

	/**
	 * Date formats are not thread safe, we need to recreate them for each parsing.
	 *
	 * @return Date format used to parse Scaffold's dates.
	 */
	private static DateFormat getDateFormat() {
		return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.US);
	}
}
