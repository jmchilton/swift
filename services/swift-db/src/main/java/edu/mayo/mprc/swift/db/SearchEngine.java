package edu.mayo.mprc.swift.db;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.swift.params2.*;
import edu.mayo.mprc.swift.params2.mapping.*;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Collection;

/**
 * Object representing a configured search engine.
 * <p/>
 * Can be used to run searches, deploy and undeploy databases,
 * and map search engine parameters to engine-specific format (both directions).
 * <p/>
 * Since the object is very heavy-weight, it should not be stored and transferred as is. Instead use the {@link #getCode()} method
 * and {@link #getForId}.
 */
public final class SearchEngine implements Serializable, Comparable<SearchEngine>, Cloneable {
	private static final long serialVersionUID = 20080212L;
	private static final Logger LOGGER = Logger.getLogger(SearchEngine.class);

	private String code;
	private String resultExtension;
	private String doSearchXmlAttribute;
	private String friendlyName;
	private boolean isOnByDefault;
	private String outputDirName;
	private ParamsInfo paramsInfo;

	private transient MappingFactory mappingFactory;
	private transient DaemonConnection searchDaemon;
	private transient DaemonConnection dbDeployDaemon;

	public static SearchEngine getForId(String id, Collection<SearchEngine> engines) {
		for (SearchEngine engine : engines) {
			if (engine.getCode().equalsIgnoreCase(id)) {
				return engine;
			}
		}
		return null;
	}

	/**
	 * Take the parameters as entered by the user and produce a file per each search engine in the given folder.
	 * The files will have default names as specified by {@link edu.mayo.mprc.swift.params2.mapping.MappingFactory#getCanonicalParamFileName()}.
	 *
	 * @param parameters  Parameters to map.
	 * @param folder      Where to write parameter files.
	 * @param engines     List of engines to map.
	 * @param validations Validations for all the parameters for all the search engines.
	 */
	public static void mapToParameterFiles(SearchEngineParameters parameters, File folder, Collection<SearchEngine> engines, ParamsValidations validations) {
		if (validations == null) {
			validations = new ParamsValidations();
		}
		for (SearchEngine engine : engines) {
			engine.writeSearchEngineParameterFile(folder, parameters, validations);
		}
	}

	/**
	 * Validate a list of parameters against a list of search engines.
	 *
	 * @param parameters Parameters to validate.
	 * @param engines    List of engines that has to have valid parameter mappings.
	 * @return Object with a list of validations for each parameter
	 */
	public static ParamsValidations validate(SearchEngineParameters parameters, Collection<SearchEngine> engines) {
		ParamsValidations validations = new ParamsValidations();
		for (SearchEngine engine : engines) {
			engine.validate(parameters, validations);
		}
		return validations;
	}

	/**
	 * Produce a parameter file for the search engine, with default name, in the given folder.
	 *
	 * @param folder      Folder to put the param file to.
	 * @param params      Generic search engine parameter set.
	 * @param validations Object to be filled with parameter file validations
	 * @return The generated parameter file.
	 */
	public File writeSearchEngineParameterFile(File folder, SearchEngineParameters params, ParamsValidations validations) {
		if (getMappingFactory() == null) {
			// This engine does not support mapping (e.g. Scaffold).
			return null;
		}

		final File paramFile = new File(folder, getMappingFactory().getCanonicalParamFileName());
		final Writer writer = FileUtilities.getWriter(paramFile);

		writeSearchEngineParameters(params, validations, writer);

		return paramFile;
	}

	/**
	 * Same as {@link #writeSearchEngineParameterFile} only writes the parameters into a string.
	 */
	public String writeSearchEngineParameterString(SearchEngineParameters parameters, ParamsValidations validations) {
		if (getMappingFactory() == null) {
			// This engine does not support mapping (e.g. Scaffold).
			return null;
		}

		StringWriter writer = new StringWriter();
		writeSearchEngineParameters(parameters, validations, writer);
		return writer.toString();
	}

	/**
	 * No writing of parameters, only the validations object gets filled.
	 */
	public void validate(SearchEngineParameters parameters, ParamsValidations validations) {
		if (getMappingFactory() == null) {
			// This engine does not support mapping (e.g. Scaffold).
			return;
		}

		/** Pass a dummy writer that does nothing */
		Writer writer = new Writer() {
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				// Do nothing
			}

			@Override
			public void flush() throws IOException {
				// Do nothing
			}

			@Override
			public void close() throws IOException {
				// Do nothing
			}
		};
		writeSearchEngineParameters(parameters, validations, writer);
	}

	private void writeSearchEngineParameters(SearchEngineParameters params, ParamsValidations validations, Writer writer) {
		if (validations == null) {
			validations = new ParamsValidations();
		}
		ParamValidationsMappingContext context = new ParamValidationsMappingContext(validations, paramsInfo);

		// Initialize the mappings object
		final Mappings mapping = getMappingFactory().createMapping();
		final Reader baseSettings = mapping.baseSettings();
		mapping.read(baseSettings);
		FileUtilities.closeQuietly(baseSettings);

		// Map each parameter
		context.startMapping(ParamName.PeptideTolerance);
		mapping.setPeptideTolerance(context, params.getPeptideTolerance());

		context.startMapping(ParamName.FragmentTolerance);
		mapping.setFragmentTolerance(context, params.getFragmentTolerance());

		context.startMapping(ParamName.MissedCleavages);
		mapping.setMissedCleavages(context, params.getMissedCleavages());

		context.startMapping(ParamName.Database);
		mapping.setSequenceDatabase(context, params.getDatabase().getShortName());

		context.startMapping(ParamName.Enzyme);
		mapping.setProtease(context, params.getProtease());

		context.startMapping(ParamName.FixedMods);
		mapping.setFixedMods(context, params.getFixedModifications());

		context.startMapping(ParamName.VariableMods);
		mapping.setVariableMods(context, params.getVariableModifications());

		context.startMapping(ParamName.Instrument);
		mapping.setInstrument(context, params.getInstrument());

		if (!context.noErrors()) {
			// Errors detected with this parameter set.
			throw new MprcException("Search engine parameters have following errors:\n" + validations.toString(ValidationSeverity.ERROR));
		}

		mapping.write(mapping.baseSettings(), writer);

		try {
			writer.close();
		} catch (IOException e) {
			throw new MprcException("Could not close the output stream when mapping search engine parameters", e);
		}
	}

	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return Engine code (e.g. MASCOT). Same as {@link edu.mayo.mprc.swift.dbmapping.SearchEngineConfig#code}
	 */
	public String getCode() {
		if (code != null) {
			return code;
		} else if (mappingFactory != null) {
			return mappingFactory.getSearchEngineCode();
		} else {
			throw new MprcException("Unknown search engine code");
		}
	}

	public void setResultExtension(String resultExtension) {
		this.resultExtension = resultExtension;
	}

	public void setDoSearchXmlAttribute(String doSearchXmlAttribute) {
		this.doSearchXmlAttribute = doSearchXmlAttribute;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	/**
	 * @return User-friendly name for the engine.
	 */
	public String getFriendlyName() {
		return friendlyName;
	}

	/**
	 * @return File extension of the resulting files this engine produces.
	 */
	public String getResultExtension() {
		return resultExtension;
	}

	/**
	 * @return Attribute in the search definition xml file that specifies whether given engine should be used
	 *         for given file.
	 */
	public String getDoSearchXmlAttribute() {
		return doSearchXmlAttribute;
	}

	public DaemonConnection getSearchDaemon() {
		return searchDaemon;
	}

	public void setSearchDaemon(DaemonConnection searchDaemon) {
		this.searchDaemon = searchDaemon;
	}

	public MappingFactory getMappingFactory() {
		return mappingFactory;
	}

	public void setMappingFactory(MappingFactory mappingFactory) {
		this.mappingFactory = mappingFactory;
	}

	public DaemonConnection getDbDeployDaemon() {
		return dbDeployDaemon;
	}

	public void setDbDeployDaemon(DaemonConnection dbDeployDaemon) {
		this.dbDeployDaemon = dbDeployDaemon;
	}

	public boolean isEnabled() {
		return dbDeployDaemon != null && searchDaemon != null;
	}

	public ParamsInfo getParamsInfo() {
		return paramsInfo;
	}

	public void setParamsInfo(ParamsInfo paramsInfo) {
		this.paramsInfo = paramsInfo;
	}

	/**
	 * @return True, if the user interface should offer this search engine to be enabled by default.
	 */
	public boolean isOnByDefault() {
		return isOnByDefault;
	}

	public void setOnByDefault(boolean onByDefault) {
		isOnByDefault = onByDefault;
	}

	public void setOutputDirName(String outputDirName) {
		this.outputDirName = outputDirName;
	}

	public String getOutputDirName() {
		return outputDirName;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SearchEngine)) {
			return false;
		}

		SearchEngine that = (SearchEngine) o;

		if (!getCode().equals(that.getCode())) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return getCode().hashCode();
	}

	public int compareTo(SearchEngine o) {
		return getCode().compareTo(o.getCode());
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
