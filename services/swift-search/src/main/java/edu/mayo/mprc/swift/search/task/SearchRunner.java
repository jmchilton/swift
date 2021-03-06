package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.swift.db.SearchEngine;
import edu.mayo.mprc.swift.dbmapping.FileSearch;
import edu.mayo.mprc.swift.dbmapping.SearchEngineConfig;
import edu.mayo.mprc.swift.dbmapping.SpectrumQa;
import edu.mayo.mprc.swift.dbmapping.SwiftSearchDefinition;
import edu.mayo.mprc.swift.params2.ExtractMsnSettings;
import edu.mayo.mprc.swift.params2.SearchEngineParameters;
import edu.mayo.mprc.swift.search.SwiftSearchWorkPacket;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.Tuple;
import edu.mayo.mprc.workflow.engine.Resumer;
import edu.mayo.mprc.workflow.engine.SearchMonitor;
import edu.mayo.mprc.workflow.engine.Task;
import edu.mayo.mprc.workflow.engine.WorkflowEngine;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Performs swift search, one {@link #run()} call at a time. To do that, it
 * first creates a workflow, that is then being executed by {@link edu.mayo.mprc.workflow.engine.WorkflowEngine}.
 * <h3>Workflow creation</h3>
 * {@link #searchDefinitionToLists(edu.mayo.mprc.swift.dbmapping.SwiftSearchDefinition)} turns
 * the search definition into lists of tasks to do ({@link #rawToMgfConversions},
 * {@link #mgfCleanups},
 * {@link #databaseDeployments},
 * {@link #engineSearches},
 * {@link #scaffoldCalls}) and {@link #spectrumQaTasks}.
 * <p/>
 * The lists of tasks get collected and added to the workflow engine by {@link #collectAllTasks()}.
 * <h3>Workflow execution</h3>
 * {@link #run()} method performs next step of the search by calling the workflow
 * engine.
 */
public final class SearchRunner implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(SearchRunner.class);

	private SwiftSearchWorkPacket packet;
	private SwiftSearchDefinition searchDefinition;

	private CurationDao curationDao;

	/**
	 * Key: (raw file, raw settings) tuple, obtained by {@link #getRawToMgfConversionHashKey(java.io.File, edu.mayo.mprc.swift.params2.ExtractMsnSettings)}.<br/>
	 * Value: Raw->MGF conversion task.
	 */
	private Map<Tuple<String, File>, RawToMgfTask> rawToMgfConversions = new HashMap<Tuple<String, File>, RawToMgfTask>();

	/**
	 * Key: .mgf file obtained by {@link #getMgfCleanupHashKey(java.io.File)}.<br/>
	 * Value: Mgf cleanup task
	 */
	private Map<File, MgfOutput> mgfCleanups = new HashMap<File, MgfOutput>();

	/**
	 * Key: raw file<br/>
	 * Value: RAW dump task
	 */
	private Map<File, RAWDumpTask> rawDumpTask = new HashMap<File, RAWDumpTask>();

	/**
	 * Key: input file<br/>
	 * Value: MSMSEvalFilter task
	 */
	private Map<File, SpectrumQaTask> spectrumQaTasks = new HashMap<File, SpectrumQaTask>();

	/**
	 * Key: (engine, param file) tuple, obtained by {@link #getDbDeploymentHashKey(edu.mayo.mprc.swift.db.SearchEngine)}.<br/>
	 * Value: Database deployment task.
	 */
	private Map<SearchEngine, DatabaseDeployment> databaseDeployments = new HashMap<SearchEngine, DatabaseDeployment>();

	/**
	 * Key: "engine:input file" tuple, obtained by {@link #getEngineSearchHashKey(edu.mayo.mprc.swift.db.SearchEngine, java.io.File)}.<br/>
	 * Value: Engine search task.
	 */
	private Map<String, EngineSearchTask> engineSearches = new HashMap<String, EngineSearchTask>();

	/**
	 * Key: scaffold call specification<br/>
	 * Value: Scaffold caller task
	 */
	private Map<ScaffoldCall, ScaffoldTaskI> scaffoldCalls = new HashMap<ScaffoldCall, ScaffoldTaskI>();

	/**
	 * One and only QA task for the entire search == more practical
	 */
	private QaTask qaTask;

	/**
	 * List of task reports.
	 */
	private List<Task> reportCalls = new LinkedList<Task>();

	private DaemonConnection raw2mgfDaemon;
	private DaemonConnection mgfCleanupDaemon;
	private DaemonConnection rawDumpDaemon;
	private DaemonConnection msmsEvalDaemon;
	private DaemonConnection scaffoldReportDaemon;
	private DaemonConnection qaDaemon;

	private Collection<SearchEngine> searchEngines = null;

	private WorkflowEngine workflowEngine;

	private boolean initializationDone = false;

	private ProgressReporter reporter;
	private ExecutorService service;

	private FileTokenFactory fileTokenFactory;
	private Map<SearchEngine, File> parameterFiles;

	/**
	 * Making files distinct in case the search uses same file name several times.
	 */
	private DistinctFiles distinctFiles = new DistinctFiles();
	private static final String DEFAULT_SPECTRUM_QA_FOLDER = "spectrum_qa";
	private static final String DEFAULT_PARAMS_FOLDER = "params";

	public SearchRunner(
			SwiftSearchWorkPacket packet,
			SwiftSearchDefinition searchDefinition,
			DaemonConnection raw2mgfDaemon,
			DaemonConnection mgfCleanupDaemon,
			DaemonConnection rawDumpDaemon,
			DaemonConnection msmsEvalDaemon,
			DaemonConnection scaffoldReportDaemon,
			DaemonConnection qaDaemon,
			Collection<SearchEngine> searchEngines,
			ProgressReporter reporter,
			ExecutorService service,
			CurationDao curationDao,
			FileTokenFactory fileTokenFactory) {
		this.searchDefinition = searchDefinition;
		this.packet = packet;
		this.raw2mgfDaemon = raw2mgfDaemon;
		this.mgfCleanupDaemon = mgfCleanupDaemon;
		this.rawDumpDaemon = rawDumpDaemon;
		this.msmsEvalDaemon = msmsEvalDaemon;
		this.scaffoldReportDaemon = scaffoldReportDaemon;
		this.qaDaemon = qaDaemon;
		this.workflowEngine = new WorkflowEngine(packet.getTaskId());
		this.searchEngines = searchEngines;
		this.reporter = reporter;
		this.service = service;
		this.curationDao = curationDao;
		this.fileTokenFactory = fileTokenFactory;
		assertValid();
	}

	public void initialize() {
		if (!initializationDone) {
			LOGGER.debug("Initializing search " + this.searchDefinition.getTitle());
			createParameterFiles();
			searchDefinitionToLists(this.searchDefinition);
			addReportTasks(this.searchDefinition);
			collectAllTasks();
			assertValid();
			initializationDone = true;
		}
	}

	public void run() {
		while (true) {
			try {
				workflowEngine.run();
				if (workflowEngine.isDone()) {
					packet.synchronizeFileTokensOnReceiver();
					reporter.reportSuccess();
					break;
				} else if (workflowEngine.isWorkAvailable()) {
					// Yield - go into the loop again and process the available work
					yield();
				} else {
					workflowEngine.resumeOnWork(new MyResumer(this));
					break;
				}
			} catch (Exception t) {
				workflowEngine.reportError(t);
				reporter.reportFailure(t);
				break;
			}
		}
	}

	private void yield() {
		// Currently does nothing, the engine immediatelly keeps processing more work
	}

	public void assertValid() {
		assert curationDao != null : "Curation DAO has to be set up";
		assert searchEngines != null : "Search engine set must not be null";
		if (this.searchDefinition != null) {
			assert workflowEngine.getNumTasks() ==
					rawToMgfConversions.size() +
							mgfCleanups.size() +
							rawDumpTask.size() +
							databaseDeployments.size() +
							engineSearches.size() +
							scaffoldCalls.size() +
							reportCalls.size() +
							(qaTask == null ? 0 : 1) : "All tasks must be a collection of *ALL* tasks";
		}
	}

	private void collectAllTasks() {
		workflowEngine.addAllTasks(databaseDeployments.values());
		workflowEngine.addAllTasks(rawToMgfConversions.values());
		workflowEngine.addAllTasks(mgfCleanups.values());
		workflowEngine.addAllTasks(rawDumpTask.values());
		workflowEngine.addAllTasks(spectrumQaTasks.values());
		workflowEngine.addAllTasks(engineSearches.values());
		workflowEngine.addAllTasks(scaffoldCalls.values());
		workflowEngine.addAllTasks(reportCalls);
		if (qaTask != null) {
			workflowEngine.addTask(qaTask);
		}
	}

	private void addReportTasks(SwiftSearchDefinition searchDefinition) {
		if (searchDefinition.getPeptideReport() != null) {
			addScaffoldReportStep(searchDefinition);
		}
	}

	private void searchDefinitionToLists(SwiftSearchDefinition searchDefinition) {
		// Now let us fill in all the lists
		File file = null;

		for (FileSearch inputFile : searchDefinition.getInputFiles()) {
			file = inputFile.getInputFile();
			if (file.exists()) {
				addInputFileToLists(inputFile, Boolean.TRUE.equals(searchDefinition.getPublicSearchFiles()));
			} else {
				LOGGER.info("Skipping nonexistent input file [" + file.getAbsolutePath() + "]");
			}
		}
	}

	private SearchEngine getSearchEngine(String code) {
		for (SearchEngine engine : searchEngines) {
			if (engine.getCode().equalsIgnoreCase(code)) {
				return engine;
			}
		}
		return null;
	}

	private SearchEngine getScaffoldEngine() {
		return getSearchEngine("SCAFFOLD");
	}

	private SearchEngine getScaffold3Engine() {
		return getSearchEngine("SCAFFOLD3");
	}

	/**
	 * Save parameter files to the disk.
	 */
	private void createParameterFiles() {
		// Obtain a set of all search engines that were requested
		// This way we only create config files that we need
		Set<String> enabledEngines = new HashSet<String>();
		for (FileSearch fileSearch : searchDefinition.getInputFiles()) {
			if (fileSearch != null) {
				for (SearchEngineConfig config : fileSearch.getEnabledEngines().getEngineConfigs()) {
					enabledEngines.add(config.getCode());
				}
			}
		}

		File paramFolder = new File(searchDefinition.getOutputFolder(), DEFAULT_PARAMS_FOLDER);
		FileUtilities.ensureFolderExists(paramFolder);
		parameterFiles = new HashMap<SearchEngine, File>();
		SearchEngineParameters params = searchDefinition.getSearchParameters();
		if (!enabledEngines.isEmpty()) {
			FileUtilities.ensureFolderExists(paramFolder);
			for (String engineCode : enabledEngines) {
				final SearchEngine engine = getSearchEngine(engineCode);
				final File file = engine.writeSearchEngineParameterFile(paramFolder, params, null /*We do not validate, validation should be already done*/);
				addParamFile(engineCode, file);
			}
		}
	}

	void addInputFileToLists(FileSearch inputFile, boolean publicSearchFiles) {
		MgfOutput mgfOutput = addMgfProducingProcess(inputFile);
		addInputAnalysis(inputFile, mgfOutput);

		SearchEngine scaffold = getScaffoldEngine();
		DatabaseDeployment scaffoldDeployment = null;
		if (scaffold != null && inputFile.isSearch("SCAFFOLD")) {
			scaffoldDeployment =
					addDatabaseDeployment(scaffold, null/*scaffold has no param file*/,
							searchDefinition.getSearchParameters().getDatabase());
		}
		SearchEngine scaffold3 = getScaffold3Engine();
		DatabaseDeployment scaffold3Deployment = null;
		if (scaffold3 != null && inputFile.isSearch("SCAFFOLD3")) {
			scaffold3Deployment =
					addDatabaseDeployment(scaffold3, null/*scaffold has no param file*/,
							searchDefinition.getSearchParameters().getDatabase());
		}

		ScaffoldTaskI scaffoldTask = null;
		ScaffoldTaskI scaffold3Task = null;

		// Go through all possible search engines this file requires
		for (SearchEngine engine : searchEngines) {
			// All non-scaffold searches get normal entries
			// While building these, the Scaffold search entry itself is initialized in a separate list
			if (!isScaffoldEngine(engine) && inputFile.getEnabledEngines().isEnabled(engine.getCode())) {
				File paramFile = getParamFile(engine);

				DatabaseDeploymentResult deploymentResult = null;
				// Sometimes the database deployment is not needed for a particular tool.
				if ("SEQUEST".equalsIgnoreCase(engine.getCode()) && noSequestDeployment()) {
					deploymentResult = new NoSequestDeploymentResult(curationDao.findCuration(searchDefinition.getSearchParameters().getDatabase().getShortName()).getCurationFile());
				} else {
					deploymentResult = addDatabaseDeployment(engine, paramFile, searchDefinition.getSearchParameters().getDatabase());
				}
				File outputFolder = getOutputFolderForSearchEngine(engine);
				EngineSearchTask search = addEngineSearch(engine, paramFile, inputFile, outputFolder, mgfOutput, deploymentResult, publicSearchFiles);
				if (inputFile.isSearch("SCAFFOLD")) {
					if (scaffoldDeployment == null) {
						throw new MprcException("Scaffold search submitted without having Scaffold service enabled.");
					}
					scaffoldTask = addScaffoldCall(inputFile, search, scaffoldDeployment);

					if (searchDefinition.getQa() != null) {
						addQaTask(inputFile, scaffoldTask, mgfOutput);
					}
				}
				if (inputFile.isSearch("SCAFFOLD3")) {
					if (scaffold3Deployment == null) {
						throw new MprcException("Scaffold search submitted without having Scaffold 3 service enabled.");
					}
					scaffold3Task = addScaffold3Call(inputFile, search, scaffold3Deployment);

					if (searchDefinition.getQa() != null) {
						addQaTask(inputFile, scaffold3Task, mgfOutput);
					}
				}
			}
		}
	}

	private boolean isScaffoldEngine(SearchEngine engine) {
		return "SCAFFOLD".equalsIgnoreCase(engine.getCode()) || "SCAFFOLD3".equalsIgnoreCase(engine.getCode());
	}

	private void addParamFile(String engineCode, File file) {
		parameterFiles.put(getSearchEngine(engineCode), file);
	}

	private File getParamFile(SearchEngine engine) {
		return parameterFiles.get(engine);
	}

	/**
	 * Adds steps to analyze the contents of the input file. This means spectrum QA (e.g. using msmsEval)
	 * as well as metadata extraction.
	 *
	 * @param inputFile Input file to analyze.
	 * @param mgf       Mgf of the input file.
	 */
	private void addInputAnalysis(FileSearch inputFile, MgfOutput mgf) {
		// TODO: Extract metadata from the input file

		// Analyze spectrum quality if requested
		if (searchDefinition.getQa() != null && searchDefinition.getQa().getParamFilePath() != null) {
			addSpectrumQualityAnalysis(inputFile, mgf);
		}
	}

	private File getOutputFolderForSearchEngine(SearchEngine engine) {
		return new File(searchDefinition.getOutputFolder(), engine.getOutputDirName());
	}

	/**
	 * @return <code>true</code> if the input file has Sequest deployment disabled.
	 */
	private boolean noSequestDeployment() {
		// Non-specific proteases (do not define restrictions for Rn-1 and Rn prevent sequest from deploying database index
		return "".equals(searchDefinition.getSearchParameters().getProtease().getRn()) &&
				"".equals(searchDefinition.getSearchParameters().getProtease().getRnminus1());
	}

	/**
	 * Add a process that produces an mgf file.
	 * <ul>
	 * <li>If the file is a .RAW, we perform conversion.</li>
	 * <li>If the file is already in .mgf format, instead of converting raw->mgf,
	 * we clean the mgf up, making sure the title contains expected information.</li>
	 * </ul>
	 *
	 * @param inputFile file to convert.
	 * @return Task capable of producing an mgf (either by conversion or by cleaning up an existing mgf).
	 */
	MgfOutput addMgfProducingProcess(FileSearch inputFile) {
		File file = inputFile.getInputFile();

		MgfOutput mgfOutput = null;
		// First, make sure we have a valid mgf, no matter what input we got
		if (file.getName().endsWith(".mgf")) {
			mgfOutput = addMgfCleanupStep(inputFile);
		} else {
			mgfOutput = addRaw2MgfConversionStep(inputFile);
		}
		return mgfOutput;
	}

	private MgfOutput addRaw2MgfConversionStep(FileSearch inputFile) {
		File file = inputFile.getInputFile();
		final Tuple<String, File> hashKey = getRawToMgfConversionHashKey(file, searchDefinition.getSearchParameters().getExtractMsnSettings());
		RawToMgfTask task = rawToMgfConversions.get(hashKey);

		if (task == null) {
			File mgfFile = getMgfFileLocation(inputFile);

			task = new RawToMgfTask(
					/*Input file*/ file,
					/*Mgf file location*/ mgfFile,
					/*raw2mgf command line*/ searchDefinition.getSearchParameters().getExtractMsnSettings().getCommandLineSwitches(),
					Boolean.TRUE.equals(searchDefinition.getPublicMgfFiles()),
					raw2mgfDaemon, fileTokenFactory, isFromScratch());

			rawToMgfConversions.put(hashKey, task);
		}

		return task;
	}

	/**
	 * We have already made .mgf file. Because it can be problematic, we need to clean it up
	 */
	private MgfOutput addMgfCleanupStep(FileSearch inputFile) {
		File file = inputFile.getInputFile();
		MgfOutput mgfOutput = mgfCleanups.get(getMgfCleanupHashKey(file));
		if (mgfOutput == null) {
			File outputFile = getMgfFileLocation(inputFile);
			mgfOutput = new MgfTitleCleanupTask(mgfCleanupDaemon, file, outputFile, fileTokenFactory, isFromScratch());
			mgfCleanups.put(getMgfCleanupHashKey(file), mgfOutput);
		}
		return mgfOutput;
	}

	/**
	 * Adds steps needed to analyze quality of the spectra. This can be done with a tool such as msmsEval or similar.
	 *
	 * @param inputFile Input file
	 * @param mgf       .mgf for the input file
	 */
	private void addSpectrumQualityAnalysis(FileSearch inputFile, MgfOutput mgf) {
		if (inputFile == null) {
			throw new MprcException("Bug: Input file must not be null");
		}
		final SpectrumQa spectrumQa = searchDefinition.getQa();

		if (spectrumQa == null) {
			throw new MprcException("Bug: The spectrum QA step must be enabled to be used");
		}
		// TODO: Check for spectrumQa.paramFile to be != null. Current code is kind of a hack.
		File file = inputFile.getInputFile();

		if (spectrumQaTasks.get(getSpectrumQaHashKey(file)) == null) {
			SpectrumQaTask spectrumQaTask = new SpectrumQaTask(
					msmsEvalDaemon,
					mgf,
					spectrumQa.paramFile() == null ? null : spectrumQa.paramFile().getAbsoluteFile(),
					getSpectrumQaOutputFolder(inputFile),
					fileTokenFactory, isFromScratch());
			spectrumQaTask.addDependency(mgf);

			spectrumQaTasks.put(getSpectrumQaHashKey(file), spectrumQaTask);
		}
	}

	private void addScaffoldReportStep(SwiftSearchDefinition searchDefinition) {

		List<File> scaffoldOutputFiles = new ArrayList<File>(scaffoldCalls.size());

		for (ScaffoldTaskI scaffoldTask : scaffoldCalls.values()) {
			scaffoldOutputFiles.add(scaffoldTask.getScaffoldPeptideReportFile());
		}

		File peptideReportFile = new File(scaffoldOutputFiles.get(0).getParentFile(), "Swift Peptide Report For " + searchDefinition.getTitle() + ".xls");
		File proteinReportFile = new File(scaffoldOutputFiles.get(0).getParentFile(), "Swift Protein Report For " + searchDefinition.getTitle() + ".xls");

		ScaffoldReportTask scaffoldReportTask = new ScaffoldReportTask(scaffoldReportDaemon, scaffoldOutputFiles, peptideReportFile, proteinReportFile, fileTokenFactory, isFromScratch());

		for (ScaffoldTaskI scaffoldTask : scaffoldCalls.values()) {
			scaffoldReportTask.addDependency(scaffoldTask);
		}

		reportCalls.add(scaffoldReportTask);
	}

	private boolean isFromScratch() {
		return packet.isFromScratch();
	}

	private void addQaTask(FileSearch inputFile, ScaffoldTaskI scaffoldTask, MgfOutput mgfOutput) {
		if (qaDaemon != null) {
			if (qaTask == null) {
				qaTask = new QaTask(qaDaemon, fileTokenFactory, isFromScratch());
			}

			// Set up a new experiment dependency. All entries called from now on would be added under that experiment
			qaTask.addExperiment(scaffoldTask.getScaffoldXmlFile(), scaffoldTask.getScaffoldSpectraFile());
			qaTask.addDependency(scaffoldTask);
			qaTask.addDependency(mgfOutput);

			if (isRawFile(inputFile)) {
				File file = inputFile.getInputFile();

				RAWDumpTask rawDumpTask = null;

				if (rawDumpDaemon != null) {
					rawDumpTask = addRawDumpTask(file, qaTask.getQaReportFolder());
					qaTask.addDependency(rawDumpTask);
				}

				qaTask.addMgfToRawEntry(mgfOutput, file, rawDumpTask);
			}

			SpectrumQaTask spectrumQaTask = null;

			if ((spectrumQaTask = spectrumQaTasks.get(getSpectrumQaHashKey(inputFile.getInputFile()))) != null) {
				qaTask.addMgfToMsmsEvalEntry(mgfOutput, spectrumQaTask);
				qaTask.addDependency(spectrumQaTask);
			}

			final SearchEngine myrimatchSearchEngine = SearchEngine.getForId("MYRIMATCH", searchEngines);
			if (myrimatchSearchEngine != null) {
				final EngineSearchTask myrimatchTask = engineSearches.get(getEngineSearchHashKey(myrimatchSearchEngine, inputFile.getInputFile()));
				if (myrimatchTask != null) {
					qaTask.addMgfToAdditionalSearchEngineEntry(mgfOutput, myrimatchTask);
					qaTask.addDependency(myrimatchTask);
				}
			}
		}
	}

	private RAWDumpTask addRawDumpTask(File rawFile, File outputFolder) {
		RAWDumpTask task = rawDumpTask.get(rawFile);

		if (task == null) {
			task = new RAWDumpTask(rawFile, outputFolder, rawDumpDaemon, fileTokenFactory, isFromScratch());
		}

		rawDumpTask.put(rawFile, task);

		return task;
	}

	private static boolean isRawFile(FileSearch inputFile) {
		return !inputFile.getInputFile().getName().endsWith(".mgf");
	}

	/**
	 * @param inputFile The input file entry from the search definition.
	 * @return
	 */
	private File getMgfFileLocation(FileSearch inputFile) {
		File file = inputFile.getInputFile();
		String mgfOutputDir = new File(
				new File(searchDefinition.getOutputFolder(), "dta"),
				getFileTitle(file)).getPath();
		File mgfFile = new File(mgfOutputDir, replaceFileExtension(file, ".mgf").getName());
		// Make sure we never produce same mgf file twice (for instance when we get two identical input mgf file names as input that differ only in the folder).
		return distinctFiles.getDistinctFile(mgfFile);
	}

	/**
	 * @param inputFile The input file entry from the search definition.
	 * @return The location of the msmsEval filtered output for the given input file
	 */
	private File getSpectrumQaOutputFolder(FileSearch inputFile) {
		File file = inputFile.getInputFile();
		// msmsEval directory should be right next to the "dta" folder
		File msmsEvalFolder = getSpectrumQaOutputDirLocation();
		File outputFolder =
				new File(
						msmsEvalFolder,
						getFileTitle(file));
		// Make sure we never produce same folder twice (for instance when we get two identical input mgf file names that should be processed with different params).
		return distinctFiles.getDistinctFile(outputFolder);
	}

	/**
	 * Returns spectrum QA output folder location. If the setting is missing, looks at the dta folder and makes spectrum QA folder next to it.
	 */
	private File getSpectrumQaOutputDirLocation() {
		return new File(searchDefinition.getOutputFolder(), DEFAULT_SPECTRUM_QA_FOLDER);
	}

	/**
	 * Returns output file given search engine, search output folder and name of the input file.
	 */
	private File getSearchResultLocation(SearchEngine engine, File searchOutputFolder, File file) {
		String fileTitle = FileUtilities.stripExtension(file.getName());
		String newFileName = fileTitle + engine.getResultExtension();
		File resultFile = new File(searchOutputFolder, newFileName);
		// Make sure we never produce two identical result files.
		return distinctFiles.getDistinctFile(resultFile);
	}

	private static String getFileTitle(File file) {
		return FileUtilities.stripExtension(file.getName());
	}

	private static File replaceFileExtension(File file, String newExtension) {
		return new File(FileUtilities.stripExtension(file.getName()) + newExtension);
	}

	/**
	 * Make a record for db deployment, if we do not have one already
	 */
	DatabaseDeployment addDatabaseDeployment(SearchEngine engine, File paramFile, Curation dbToDeploy) {
		SearchEngine hashKey;
		// The DB deployment is defined by engine for which it is done
		hashKey = getDbDeploymentHashKey(engine);

		DatabaseDeployment deployment = databaseDeployments.get(hashKey);
		if (deployment == null) {
			deployment = new DatabaseDeployment(engine.getCode(), engine.getFriendlyName(), engine.getDbDeployDaemon(), paramFile, dbToDeploy, fileTokenFactory, isFromScratch());
			databaseDeployments.put(hashKey, deployment);
		}
		return deployment;
	}

	/**
	 * Make a record for the search itself.
	 * The search depends on the engine, and the file to be searched.
	 * If these two things are identical for two entries, then the search can be performed just once.
	 * <p/>
	 * The search also knows about the conversion and db deployment so it can determine when it can run.
	 */
	private EngineSearchTask addEngineSearch(SearchEngine engine, File paramFile, FileSearch inputFile, File searchOutputFolder, MgfOutput mgfOutput, DatabaseDeploymentResult deploymentResult, boolean publicSearchFiles) {
		File rawOrMgfFile = inputFile.getInputFile();
		String searchKey = getEngineSearchHashKey(engine, rawOrMgfFile);
		EngineSearchTask search = engineSearches.get(searchKey);
		if (search == null) {
			File outputFile = getSearchResultLocation(engine, searchOutputFolder, rawOrMgfFile);
			search = new EngineSearchTask(
					engine,
					rawOrMgfFile.getName(),
					mgfOutput,
					deploymentResult,
					outputFile,
					paramFile,
					publicSearchFiles,
					engine.getSearchDaemon(),
					fileTokenFactory,
					isFromScratch());

			// Depend on the .mgf to be done and on the database deployment
			search.addDependency(mgfOutput);
			if (deploymentResult instanceof Task) {
				search.addDependency((Task) deploymentResult);
			}
			engineSearches.put(searchKey, search);
		}
		return search;
	}

	/**
	 * Add a scaffold call (or update existing one) that depends on this input file to be sought through
	 * the given engine search.
	 */
	private ScaffoldTaskI addScaffoldCall(FileSearch inputFile, EngineSearchTask search, DatabaseDeployment scaffoldDbDeployment) {
		String experiment = inputFile.getExperiment();
		final ScaffoldCall key = new ScaffoldCall(experiment, "2");
		ScaffoldTaskI scaffoldTask = scaffoldCalls.get(key);
		if (scaffoldTask == null) {
			File scaffoldOutputDir = getOutputFolderForSearchEngine(getScaffoldEngine());
			scaffoldTask = new ScaffoldTask(
					experiment,
					searchDefinition,
					getScaffoldEngine().getSearchDaemon(),
					scaffoldOutputDir,
					fileTokenFactory,
					isFromScratch());
			scaffoldCalls.put(key, scaffoldTask);
		}
		scaffoldTask.addInput(inputFile, search);
		scaffoldTask.addDatabase(scaffoldDbDeployment.getShortDbName(), scaffoldDbDeployment);
		scaffoldTask.addDependency(search);
		scaffoldTask.addDependency(scaffoldDbDeployment);

		return scaffoldTask;
	}

	/**
	 * Add a scaffold 3 call (or update existing one) that depends on this input file to be sought through
	 * the given engine search.
	 */
	private ScaffoldTaskI addScaffold3Call(FileSearch inputFile, EngineSearchTask search, DatabaseDeployment scaffoldDbDeployment) {
		String experiment = inputFile.getExperiment();
		final ScaffoldCall key = new ScaffoldCall(experiment, "3");
		ScaffoldTaskI scaffoldTask = scaffoldCalls.get(key);
		if (scaffoldTask == null) {
			File scaffoldOutputDir = getOutputFolderForSearchEngine(getScaffold3Engine());
			scaffoldTask = new Scaffold3Task(
					experiment,
					searchDefinition,
					getScaffold3Engine().getSearchDaemon(),
					scaffoldOutputDir,
					fileTokenFactory,
					isFromScratch());
			scaffoldCalls.put(key, scaffoldTask);
		}
		scaffoldTask.addInput(inputFile, search);
		scaffoldTask.addDatabase(scaffoldDbDeployment.getShortDbName(), scaffoldDbDeployment);
		scaffoldTask.addDependency(search);
		scaffoldTask.addDependency(scaffoldDbDeployment);

		return scaffoldTask;
	}

	private static String getEngineSearchHashKey(SearchEngine engine, File file) {
		return engine.getCode() + ':' + file.getAbsolutePath();
	}

	private static File getMgfCleanupHashKey(File file) {
		return file.getAbsoluteFile();
	}

	private static File getSpectrumQaHashKey(File file) {
		return file.getAbsoluteFile();
	}

	private static Tuple<String, File> getRawToMgfConversionHashKey(File inputFile, ExtractMsnSettings extractMsnSettings) {
		return new Tuple<String, File>(extractMsnSettings.getCommandLineSwitches(), inputFile);
	}

	private static SearchEngine getDbDeploymentHashKey(SearchEngine engine) {
		return engine;
	}

	public SwiftSearchDefinition getSearchDefinition() {
		return searchDefinition;
	}

	public void addSearchMonitor(SearchMonitor monitor) {
		this.workflowEngine.addMonitor(monitor);
	}

	private static final class MyResumer implements Resumer {
		private SearchRunner runner;

		private MyResumer(SearchRunner runner) {
			this.runner = runner;
		}

		public void resume() {
			runner.service.execute(runner);
		}
	}
}
