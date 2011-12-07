package edu.mayo.mprc.swift.ui.server;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.swift.db.SwiftDao;
import edu.mayo.mprc.swift.dbmapping.*;
import edu.mayo.mprc.swift.params2.*;
import edu.mayo.mprc.swift.params2.mapping.ParamsInfo;
import edu.mayo.mprc.swift.params2.mapping.ParamsValidations;
import edu.mayo.mprc.swift.params2.mapping.Validation;
import edu.mayo.mprc.swift.params2.mapping.ValidationList;
import edu.mayo.mprc.swift.ui.client.rpc.*;
import edu.mayo.mprc.unimod.ModSet;
import edu.mayo.mprc.unimod.ModSpecificity;
import edu.mayo.mprc.unimod.Unimod;
import edu.mayo.mprc.unimod.UnimodDao;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.workspace.User;
import edu.mayo.mprc.workspace.WorkspaceDao;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Creates instances of classes under {@link edu.mayo.mprc.swift.ui.client.rpc}
 * that wrap classes in {@link edu.mayo.mprc.swift.params2}.
 * <p/>
 * This royally sucks.
 */
public final class ClientProxyGenerator {
	private UnimodDao unimodDao;
	private WorkspaceDao workspaceDao;
	private SwiftDao swiftDao;
	private Unimod cachedUnimod;
	private File browseRoot;

	public ClientProxyGenerator(UnimodDao unimodDao, WorkspaceDao workspaceDao, SwiftDao swiftDao, File browseRoot) {
		this.unimodDao = unimodDao;
		this.workspaceDao = workspaceDao;
		this.swiftDao = swiftDao;
		this.browseRoot = browseRoot;
	}

	public static ClientParamSetList getClientParamSetList(List<SavedSearchEngineParameters> savedSets, List<ClientParamSet> temporarySets) {
		List<ClientParamSet> list = new ArrayList<ClientParamSet>(savedSets.size() + temporarySets.size());
		for (SavedSearchEngineParameters params : savedSets) {
			list.add(new ClientParamSet(params.getId(), params.getName(), params.getUser().getUserName(), params.getUser().getInitials()));
		}
		for (ClientParamSet temporarySet : temporarySets) {
			list.add(temporarySet);
		}
		return new ClientParamSetList(list);
	}

	public ClientParamSetValues convertValues(SearchEngineParameters ps, ParamsValidations validations) {
		List<ClientParam> values = new ArrayList<ClientParam>(ParamName.values().length);
		for (ParamName paramName : ParamName.values()) {
			values.add(new ClientParam(paramName.getId(), convert(ps.getValue(paramName)), convertTo(validations.getValidationFor(paramName))));
		}
		return new ClientParamSetValues(values);
	}

	public static final class ConversionException extends MprcException {
		private static final long serialVersionUID = 20071220L;

		public ConversionException(String message) {
			super(message);
		}

		public ConversionException(Throwable throwable) {
			super(throwable);
		}

		public ConversionException() {
		}
	}

	public ClientParamsValidations convertTo(ParamsValidations validations) {
		Map<String, ClientValidationList> map = new HashMap<String, ClientValidationList>();
		for (Map.Entry<ParamName, ValidationList> entry : validations.getValidationMap().entrySet()) {
			map.put(entry.getKey().getId(), convertTo(entry.getValue()));
		}
		return new ClientParamsValidations(map);
	}

	public ClientUser convertTo(User user) {
		return new ClientUser(user.getUserName(), user.getFirstName() + " " + user.getLastName(), user.getInitials(), user.isParameterEditorEnabled(), user.isOutputPathChangeEnabled());
	}

	public ClientValidation convertTo(Validation v) {
		ClientValidation cv = new ClientValidation();
		cv.setMessage(v.getMessage());
		cv.setSeverity(v.getSeverity().rank);
		cv.setParamId(v.getParam().getId());
		if (v.getValue() != null) {
			cv.setValue(convert(v.getValue()));
		}
		if (v.getThrowable() != null) {
			Throwable t = v.getThrowable();
			cv.setThrowableMessage(t.getMessage());
			cv.setThrowableStackTrace(MprcException.getDetailedMessage(t));
		}
		return cv;
	}

	public ClientValidationList convertTo(ValidationList l) {
		ClientValidationList list = new ClientValidationList(l.size());
		for (Validation v : l) {
			list.add(convertTo(v));
		}
		return list;
	}

	List<ClientValue> getAllowedValues(ParamName p, ParamsInfo paramsInfo) {
		List<ClientValue> allowedValues = null;
		Iterable<?> vals = paramsInfo.getAllowedValues(p);
		if (vals != null) {
			allowedValues = new ArrayList<ClientValue>();
			for (Object oo : vals) {
				allowedValues.add(convert(oo));
			}
		}
		return allowedValues;
	}

	public ClientTolerance convertTo(Tolerance du) {
		return new ClientTolerance(du.getValue() + " " + du.getUnit().getCode());
	}

	public Tolerance convertFrom(ClientTolerance du) {
		return new Tolerance(du.getValue());
	}

	public ClientSequenceDatabase convertTo(Curation val) {
		return new ClientSequenceDatabase(val.getId(), val.getTitle(), val.getShortName(), val.getOwnerEmail());
	}

	public ClientInstrument convertTo(Instrument val) {
		return new ClientInstrument(val.getName());
	}

	public Curation convertFrom(ClientSequenceDatabase val, Iterable<Curation> allowedValues) {
		for (Curation db : allowedValues) {
			if (val.getShortName().equals(db.getShortName())) {
				return db;
			}
		}
		throw new MprcException("No such database " + val.getShortName());
	}

	public Instrument convertFrom(ClientInstrument val, Iterable<Instrument> allowedValues) {
		for (Instrument inst : allowedValues) {
			if (val.getName().equals(inst.getName())) {
				return inst;
			}
		}
		throw new MprcException("No such instrument " + val.getName());
	}

	public ModSet convertFrom(ClientModSpecificitySet val) {
		ModSet modSpecSet = new ModSet();
		List<ClientModSpecificity> modSpecs = val.getModSpecificities();

		for (ClientModSpecificity modSpec : modSpecs) {
			if (cachedUnimod == null) {
				cachedUnimod = unimodDao.load();
			}
			List<ModSpecificity> modspec = cachedUnimod.getSpecificitiesByMascotName(modSpec.getName());
			modSpecSet.addAll(modspec);
		}
		return modSpecSet;
	}

	public FileSearch convertFrom(ClientFileSearch entry) {
		EnabledEngines enabledEngines = new EnabledEngines();
		for (String engineCode : entry.getEnabledEngineCodes()) {
			if (engineCode != null && engineCode.length() != 0) {
				enabledEngines.add(swiftDao.getSearchEngineConfig(engineCode));
			}
		}

		return new FileSearch(
				new File(browseRoot, entry.getPath()),
				entry.getBiologicalSample(),
				entry.getCategoryName(),
				entry.getExperiment(),
				enabledEngines);
	}

	/**
	 * All files the user can enter in the web interface are relative to browse root.
	 *
	 * @param file File to convert to relative path.
	 * @return Path to the file, relative to browse root. The path uses forward slashes, irregardless of the OS.
	 */
	private String pathRelativeToBrowseRoot(File file) {
		final String relativePath = FileUtilities.getRelativePath(browseRoot.getAbsolutePath(), file.getAbsolutePath());
		if (!"/".equals(File.separator)) {
			return relativePath.replaceAll(Pattern.quote(File.separator), "/");
		}
		return relativePath;
	}

	public ClientSwiftSearchDefinition convertTo(SwiftSearchDefinition definition, ClientParamSetResolver resolver) {
		final String relativeOutputFolderPath = pathRelativeToBrowseRoot(definition.getOutputFolder());

		final List<FileSearch> files = definition.getInputFiles();
		List<ClientFileSearch> clientInputFiles = new ArrayList<ClientFileSearch>(files.size());

		for (final FileSearch fileSearch : files) {
			final File inputFile = fileSearch.getInputFile();
			Long inputFileSize = null;
			if (inputFile.exists() && inputFile.isFile()) {
				inputFileSize = inputFile.length();
			} else {
				inputFileSize = -1L;
			}

			final ClientFileSearch search = new ClientFileSearch(
					pathRelativeToBrowseRoot(inputFile),
					fileSearch.getBiologicalSample(),
					fixedCategoryName(fileSearch),
					fileSearch.getExperiment(),
					fileSearch.getEnabledEngines().toEngineCodeList(),
					inputFileSize);

			clientInputFiles.add(search);
		}

		return new ClientSwiftSearchDefinition(
				definition.getTitle(),
				convertTo(definition.getUser()),
				relativeOutputFolderPath,
				resolver.resolve(definition.getSearchParameters(), definition.getUser()),
				clientInputFiles,
				convertTo(definition.getQa()),
				convertTo(definition.getPeptideReport()),
				Boolean.TRUE.equals(definition.getPublicMgfFiles()),
				Boolean.TRUE.equals(definition.getPublicSearchFiles()),
				0
		);
	}

	private String fixedCategoryName(FileSearch fileSearch) {
		return fileSearch.getCategoryName() == null ? "none" : fileSearch.getCategoryName();
	}

	private static ClientPeptideReport convertTo(PeptideReport peptideReport) {
		return new ClientPeptideReport(peptideReport != null);
	}

	private static ClientExtractMsnSettings convertTo(ExtractMsnSettings extractMsnSettings) {
		return new ClientExtractMsnSettings(extractMsnSettings.getCommandLineSwitches());
	}

	private ClientScaffoldSettings convertTo(ScaffoldSettings scaffoldSettings) {
		return new ClientScaffoldSettings(
				scaffoldSettings.getProteinProbability(),
				scaffoldSettings.getPeptideProbability(),
				scaffoldSettings.getMinimumPeptideCount(),
				scaffoldSettings.getMinimumNonTrypticTerminii(),
				convertTo(scaffoldSettings.getStarredProteins()),
				scaffoldSettings.isSaveOnlyIdentifiedSpectra(),
				scaffoldSettings.isSaveNoSpectra(),
				scaffoldSettings.isConnectToNCBI(),
				scaffoldSettings.isAnnotateWithGOA()
		);
	}

	private ClientStarredProteins convertTo(StarredProteins starredProteins) {
		if (starredProteins == null) {
			return null;
		}
		return new ClientStarredProteins(
				starredProteins.getStarred(),
				starredProteins.getDelimiter(),
				starredProteins.isRegularExpression(),
				starredProteins.isMatchName());
	}

	private ClientSpectrumQa convertTo(SpectrumQa qa) {
		if (qa == null || qa.getParamFilePath() == null) {
			return new ClientSpectrumQa();
		}
		return new ClientSpectrumQa(qa.getParamFilePath());
	}

	public ExtractMsnSettings convertFrom(ClientExtractMsnSettings extractMsnSettings) {
		if (extractMsnSettings == null) {
			return ExtractMsnSettings.DEFAULT;
		}
		return new ExtractMsnSettings(extractMsnSettings.getCommandLineSwitches());
	}

	public ScaffoldSettings convertFrom(ClientScaffoldSettings scaffoldSettings) {
		if (scaffoldSettings == null) {
			return ScaffoldSettings.DEFAULT;
		}
		return new ScaffoldSettings(
				scaffoldSettings.getProteinProbability(),
				scaffoldSettings.getPeptideProbability(),
				scaffoldSettings.getMinimumPeptideCount(),
				scaffoldSettings.getMinimumNonTrypticTerminii(),
				convertFrom(scaffoldSettings.getStarredProteins()),
				scaffoldSettings.isSaveOnlyIdentifiedSpectra(),
				scaffoldSettings.isSaveNoSpectra(),
				scaffoldSettings.isConnectToNCBI(),
				scaffoldSettings.isAnnotateWithGOA()
		);
	}

	private StarredProteins convertFrom(ClientStarredProteins starredProteins) {
		if (starredProteins == null) {
			return null;
		}
		return new StarredProteins(
				starredProteins.getStarred(),
				starredProteins.getDelimiter(),
				starredProteins.isRegularExpression(),
				starredProteins.isMatchName());
	}

	public SwiftSearchDefinition convertFrom(ClientSwiftSearchDefinition definition, SearchEngineParameters parameters) {
		List<ClientFileSearch> fileTable = definition.getInputFiles();
		List<FileSearch> fileEntries = new ArrayList<FileSearch>(fileTable.size());
		for (ClientFileSearch entry : fileTable) {
			fileEntries.add(convertFrom(entry));
		}

		return new SwiftSearchDefinition(
				definition.getSearchTitle().trim(),
				workspaceDao.getUserByEmail(definition.getUser().getEmail()),
				new File(browseRoot, definition.getOutputFolder()),
				convertFrom(definition.getSpectrumQa()),
				convertFrom(definition.getPeptideReport()),
				parameters,
				fileEntries,
				definition.isPublicMgfFiles(),
				definition.isPublicSearchFiles());
	}

	public PeptideReport convertFrom(ClientPeptideReport clientPeptideReport) {
		return clientPeptideReport.isScaffoldPeptideReportEnabled() ? new PeptideReport() : null;
	}

	public SpectrumQa convertFrom(ClientSpectrumQa clientSpectrumQa) {
		return clientSpectrumQa.isEnabled() ? new SpectrumQa(clientSpectrumQa.getParamFilePath(), SpectrumQa.DEFAULT_ENGINE) : null;
	}

	public ClientModSpecificitySet convertTo(ModSet val) {
		List<ClientModSpecificity> vals = new ArrayList<ClientModSpecificity>();
		for (ModSpecificity modspec : val.getModifications()) {
			vals.add(convertTo(modspec));
		}
		return new ClientModSpecificitySet(vals);
	}

	public ClientModSpecificity convertTo(ModSpecificity modspec) {
		ClientModSpecificity cmod = new ClientModSpecificity();
		cmod.setName(modspec.toString());
		cmod.setClassification(modspec.getClassification());
		cmod.setTerm(modspec.getTerm().toString());
		cmod.setSite(modspec.getSite());
		cmod.setRecordID(modspec.getModification().getRecordID());
		cmod.setComposition(modspec.getModification().getComposition());
		cmod.setMonoisotopic(modspec.getModification().getMassMono());
		cmod.setProteinOnly(modspec.isProteinOnly());
		cmod.setHidden(modspec.getHidden());
		if (modspec.getModification().getAltNames() != null) {
			int len = modspec.getModification().getAltNames().size();
			List<String> list = new ArrayList<String>(len);
			list.addAll(modspec.getModification().getAltNames());
			cmod.setAltNames(list);
		}
		cmod.setComments(modspec.getComments());

		return cmod;
	}

	public Protease convertFrom(ClientProtease val,
	                            Iterable<Protease> allowedValues) {
		for (Protease p : allowedValues) {
			if (val.getName().equals(p.getName())) {
				return p;
			}
		}
		throw new ConversionException("Can't seem to find Protease " + val.getName());
	}

	public ClientProtease convertTo(Protease val) {
		return new ClientProtease(val.getName());
	}

	public ClientValue convert(Object val) {
		if (val instanceof Tolerance) {
			return convertTo((Tolerance) val);
		} else if (val instanceof Validation) {
			return convertTo((Validation) val);
		} else if (val instanceof ValidationList) {
			return convertTo((ValidationList) val);
		} else if (val instanceof Curation) {
			return convertTo((Curation) val);
		} else if (val instanceof Instrument) {
			return convertTo((Instrument) val);
		} else if (val instanceof ModSpecificity) {
			return convertTo((ModSpecificity) val);
		} else if (val instanceof ModSet) {
			return convertTo((ModSet) val);
		} else if (val instanceof Protease) {
			return convertTo((Protease) val);
		} else if (val instanceof ExtractMsnSettings) {
			return convertTo((ExtractMsnSettings) val);
		} else if (val instanceof ScaffoldSettings) {
			return convertTo((ScaffoldSettings) val);
		} else if (val instanceof Integer) {
			return new ClientInteger((Integer) val);
		} else {
			throw new MprcException("Can't convert " + val.getClass().getName() + " to client proxy.");
		}
	}

	public Object convert(ClientValue val, Object allowedValues) {
		if (val instanceof ClientInteger) {
			return ((ClientInteger) val).getValue();
		} else if (val instanceof ClientTolerance) {
			return convertFrom((ClientTolerance) val);
		} else if (val instanceof ClientSequenceDatabase) {
			return convertFrom((ClientSequenceDatabase) val,
					(Iterable<Curation>) allowedValues);
		} else if (val instanceof ClientInstrument) {
			return convertFrom((ClientInstrument) val,
					(Iterable<Instrument>) allowedValues);
		} else if (val instanceof ClientModSpecificitySet) {
			return convertFrom((ClientModSpecificitySet) val
			);
		} else if (val instanceof ClientProtease) {
			return convertFrom((ClientProtease) val,
					(Iterable<Protease>) allowedValues);
		} else if (val instanceof ClientExtractMsnSettings) {
			return convertFrom((ClientExtractMsnSettings) val);
		} else if (val instanceof ClientScaffoldSettings) {
			return convertFrom((ClientScaffoldSettings) val);
		} else if (val instanceof ClientProtease) {
			return convertFrom((ClientProtease) val,
					(Iterable<Protease>) allowedValues);

		} else {
			throw new MprcException("Can't convert " + val.getClass().getName() + " from client proxy");
		}
	}
}
