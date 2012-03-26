package edu.mayo.mprc.peaks.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Class is a data structure for PeaksOnline search parameters.
 */
public final class PeaksSearchParameters {

	public static final String SUBMIT_SEARCH_TITLE = "title";
	public static final String SUBMIT_SEARCH_DATAFILE = "datafile";
	public static final String SUBMIT_SEARCH_PEAKSCLIENT = "peaksclient";
	public static final String SUBMIT_SEARCH_CLIENTFIXEDMIDIES = "clientFixedModies";
	public static final String SUBMIT_SEARCH_CLIENTVARIABLEMODIES = "clientVariableModies";
	public static final String SUBMIT_SEARCH_CLIENTTAX = "clientTax";
	public static final String SUBMIT_SEARCH_CLIENTKNOWNPROTEIN = "clientKnownProtein";
	public static final String SUBMIT_SEARCH_INPUTFIXEDMODIES = "inputFixedModies";
	public static final String SUBMIT_SEARCH_INPUTVARIABLEMODIES = "inputVariableModies";
	public static final String SUBMIT_SEARCH_KNOWNPROTEINSEQ = "knownProteinSeq";
	public static final String SUBMIT_SEARCH_SELECTGROUP = "selectgroup";
	public static final String SUBMIT_SEARCH_ENZYME = "enzyme";
	public static final String SUBMIT_SEARCH_MISSCLEAVAGE = "missCleavage";
	public static final String SUBMIT_SEARCH_FIXEDMODIES = "fixedModies";
	public static final String SUBMIT_SEARCH_VARIABLEMODIES = "variableModies";
	public static final String SUBMIT_SEARCH_MAXVARIABLEMODI = "maxVariableModi";
	public static final String SUBMIT_SEARCH_FUNCTION = "function";
	public static final String SUBMIT_SEARCH_USEANZRESULT = "useAnzResult";
	public static final String SUBMIT_SEARCH_DEFAULTUSEANZRESULT = "defaultUseAnzResult";
	public static final String SUBMIT_SEARCH_KNOWNPROTEIN = "knownProtein";
	public static final String SUBMIT_SEARCH_DATABASE = "database";
	public static final String SUBMIT_SEARCH_PREPROCESS = "preProcess";
	public static final String SUBMIT_SEARCH_DEFAULTPREPROCESS = "defaultPreProcess";
	public static final String SUBMIT_SEARCH_DEFAULTFILTER = "defaultFilter";
	public static final String SUBMIT_SEARCH_SPECTRUMMERGETOLERANCE = "spectrumMergeTolerance";
	public static final String SUBMIT_SEARCH_SPECTRUMMERGETOLERANCEUNIT = "spectrumMergeToleranceUnit";
	public static final String SUBMIT_SEARCH_INSTRUMENT = "instrument";
	public static final String SUBMIT_SEARCH_PARENTTOLERANCE = "parentTolerance";
	public static final String SUBMIT_SEARCH_PARENTTOLERANCEUNIT = "parentToleranceUnit";
	public static final String SUBMIT_SEARCH_FRAGMENTTOLERANCE = "fragmentTolerance";
	public static final String SUBMIT_SEARCH_FRAGMENTTOLERANCEUNIT = "fragmentToleranceUnit";
	public static final String SUBMIT_SEARCH_USEPRECURSORAVERAGEMASS = "usePrecursorAverageMass";

	private Map<String, PeaksParameter> parameters;

	/**
	 * Creates PeaksOnlineSearchParameter with default parameters.
	 * The following parameters are not given default values.
	 * <p/>
	 * SUBMIT_SEARCH_DATAFILE
	 * SUBMIT_SEARCH_TITLE
	 * SUBMIT_SEARCH_DATABASE
	 * SUBMIT_SEARCH_ENZYME
	 */
	public PeaksSearchParameters() {
		parameters = new HashMap();

		parameters.put(SUBMIT_SEARCH_PEAKSCLIENT, new PeaksParameter(SUBMIT_SEARCH_PEAKSCLIENT, ""));
		parameters.put(SUBMIT_SEARCH_CLIENTFIXEDMIDIES, new PeaksParameter(SUBMIT_SEARCH_CLIENTFIXEDMIDIES, ""));
		parameters.put(SUBMIT_SEARCH_CLIENTVARIABLEMODIES, new PeaksParameter(SUBMIT_SEARCH_CLIENTVARIABLEMODIES, ""));
		parameters.put(SUBMIT_SEARCH_CLIENTTAX, new PeaksParameter(SUBMIT_SEARCH_CLIENTTAX, ""));
		parameters.put(SUBMIT_SEARCH_CLIENTKNOWNPROTEIN, new PeaksParameter(SUBMIT_SEARCH_CLIENTKNOWNPROTEIN, ""));
		parameters.put(SUBMIT_SEARCH_INPUTFIXEDMODIES, new PeaksParameter(SUBMIT_SEARCH_INPUTFIXEDMODIES, ""));
		parameters.put(SUBMIT_SEARCH_INPUTVARIABLEMODIES, new PeaksParameter(SUBMIT_SEARCH_INPUTVARIABLEMODIES, ""));
		parameters.put(SUBMIT_SEARCH_KNOWNPROTEINSEQ, new PeaksParameter(SUBMIT_SEARCH_KNOWNPROTEINSEQ, ""));
		parameters.put(SUBMIT_SEARCH_SELECTGROUP, new PeaksParameter(SUBMIT_SEARCH_SELECTGROUP, "1"));
		parameters.put(SUBMIT_SEARCH_ENZYME, new PeaksParameter(SUBMIT_SEARCH_ENZYME, ""));
		parameters.put(SUBMIT_SEARCH_MISSCLEAVAGE, new PeaksParameter(SUBMIT_SEARCH_MISSCLEAVAGE, "3"));
		parameters.put(SUBMIT_SEARCH_MISSCLEAVAGE, new PeaksParameter(SUBMIT_SEARCH_FIXEDMODIES, ""));
		parameters.put(SUBMIT_SEARCH_VARIABLEMODIES, new PeaksParameter(SUBMIT_SEARCH_VARIABLEMODIES, ""));
		parameters.put(SUBMIT_SEARCH_MAXVARIABLEMODI, new PeaksParameter(SUBMIT_SEARCH_MAXVARIABLEMODI, "3"));
		parameters.put(SUBMIT_SEARCH_FUNCTION, new PeaksParameter(SUBMIT_SEARCH_FUNCTION, "2"));
		parameters.put(SUBMIT_SEARCH_USEANZRESULT, new PeaksParameter(SUBMIT_SEARCH_USEANZRESULT, "on"));
		parameters.put(SUBMIT_SEARCH_DEFAULTUSEANZRESULT, new PeaksParameter(SUBMIT_SEARCH_DEFAULTUSEANZRESULT, "true"));
		parameters.put(SUBMIT_SEARCH_KNOWNPROTEIN, new PeaksParameter(SUBMIT_SEARCH_KNOWNPROTEIN, "0"));
		parameters.put(SUBMIT_SEARCH_DATABASE, new PeaksParameter(SUBMIT_SEARCH_DATABASE, ""));
		parameters.put(SUBMIT_SEARCH_PREPROCESS, new PeaksParameter(SUBMIT_SEARCH_PREPROCESS, "on"));
		parameters.put(SUBMIT_SEARCH_DEFAULTPREPROCESS, new PeaksParameter(SUBMIT_SEARCH_DEFAULTPREPROCESS, "true"));
		parameters.put(SUBMIT_SEARCH_DEFAULTFILTER, new PeaksParameter(SUBMIT_SEARCH_DEFAULTFILTER, "false"));
		parameters.put(SUBMIT_SEARCH_SPECTRUMMERGETOLERANCE, new PeaksParameter(SUBMIT_SEARCH_SPECTRUMMERGETOLERANCE, "0.0"));
		parameters.put(SUBMIT_SEARCH_SPECTRUMMERGETOLERANCEUNIT, new PeaksParameter(SUBMIT_SEARCH_SPECTRUMMERGETOLERANCEUNIT, "Da"));
		parameters.put(SUBMIT_SEARCH_INSTRUMENT, new PeaksParameter(SUBMIT_SEARCH_INSTRUMENT, "FT-trap"));
		parameters.put(SUBMIT_SEARCH_PARENTTOLERANCE, new PeaksParameter(SUBMIT_SEARCH_PARENTTOLERANCE, "10.0"));
		parameters.put(SUBMIT_SEARCH_PARENTTOLERANCEUNIT, new PeaksParameter(SUBMIT_SEARCH_PARENTTOLERANCEUNIT, "ppm"));
		parameters.put(SUBMIT_SEARCH_FRAGMENTTOLERANCE, new PeaksParameter(SUBMIT_SEARCH_FRAGMENTTOLERANCE, "0.8"));
		parameters.put(SUBMIT_SEARCH_FRAGMENTTOLERANCEUNIT, new PeaksParameter(SUBMIT_SEARCH_FRAGMENTTOLERANCEUNIT, "Da"));
		parameters.put(SUBMIT_SEARCH_USEPRECURSORAVERAGEMASS, new PeaksParameter(SUBMIT_SEARCH_USEPRECURSORAVERAGEMASS, "0"));
	}

	public void setTitle(final String title) {
		parameters.put(SUBMIT_SEARCH_TITLE, new PeaksParameter(SUBMIT_SEARCH_TITLE, title));
	}

	public String getTitle() {
		final PeaksParameter peaksOnlineParameter = parameters.get(SUBMIT_SEARCH_TITLE);

		if (peaksOnlineParameter != null) {
			return peaksOnlineParameter.getParameterValue().toString();
		}

		return null;
	}

	/**
	 * The data file path must be locally accessible to the Peaks search engine.
	 *
	 * @param dataFile
	 */
	public void setDataFile(final File dataFile) {
		parameters.put(SUBMIT_SEARCH_DATABASE, new PeaksParameter(SUBMIT_SEARCH_DATAFILE, dataFile));
	}

	/**
	 * @return Peaks search engine locally accessible data file.
	 */
	public File getDataFile() {
		final PeaksParameter peaksOnlineParameter = parameters.get(SUBMIT_SEARCH_DATAFILE);

		if (peaksOnlineParameter != null) {
			return (File) peaksOnlineParameter.getParameterValue();
		}

		return null;
	}

	public Map<String, PeaksParameter> getParameters() {
		return parameters;
	}

	public int getNumberOfParameters() {
		return parameters.size();
	}

	public void setParameter(final PeaksParameter peaksOnlineParameter) {
		parameters.put(peaksOnlineParameter.getParameterName(), peaksOnlineParameter);
	}

	public void removeParameter(final String parameterName) {
		parameters.remove(parameterName);
	}

	public PeaksParameter getParameter(final String parameterName) {
		return parameters.get(parameterName);
	}

	public Object getParameterValue(final String parameterName) {
		return parameters.get(parameterName).getParameterValue();
	}

	/**
	 * Sets parameter in this parameter set.
	 *
	 * @param parameterName
	 * @param parameterValue String representation of this parameter value
	 */
	public void setParameter(final String parameterName, final String parameterValue) {
		setParameter(new PeaksParameter(parameterName, parameterName.equals(SUBMIT_SEARCH_DATAFILE) ? new File(parameterValue) : parameterValue));
	}

	/**
	 * @param parameters parameter name and parameter value pairs.
	 */
	public void setParameters(final Map<String, String> parameters) {
		for (final Map.Entry<String, String> me : parameters.entrySet()) {
			setParameter(me.getKey(), me.getValue());
		}
	}
}
