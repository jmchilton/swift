package edu.mayo.mprc.swift.commands;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ServiceConfig;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.fastadb.FastaDbWorker;
import edu.mayo.mprc.qa.RAWDumpWorker;
import edu.mayo.mprc.scaffold3.Scaffold3Worker;
import edu.mayo.mprc.searchdb.SearchDbWorker;
import edu.mayo.mprc.searchdb.dao.SearchDbDao;
import edu.mayo.mprc.swift.ExitCode;
import edu.mayo.mprc.swift.db.SwiftDao;
import edu.mayo.mprc.swift.dbmapping.FileSearch;
import edu.mayo.mprc.swift.dbmapping.ReportData;
import edu.mayo.mprc.swift.dbmapping.SwiftSearchDefinition;
import edu.mayo.mprc.swift.search.SwiftSearcher;
import edu.mayo.mprc.swift.search.task.*;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.progress.ProgressReport;
import edu.mayo.mprc.workflow.engine.SearchMonitor;
import edu.mayo.mprc.workflow.engine.TaskBase;
import edu.mayo.mprc.workflow.engine.WorkflowEngine;
import edu.mayo.mprc.workflow.persistence.TaskState;
import org.apache.log4j.Logger;
import org.joda.time.Interval;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Zenka
 */
public final class LoadToSearchDb implements SwiftCommand {
	private static final Logger LOGGER = Logger.getLogger(LoadToSearchDb.class);
	public static final int BATCH_SIZE = 3;
	public static final int MAX_INPUT_FILE_SIZE = 5;

	private DaemonConnection rawDump;
	private DaemonConnection scaffold3;
	private DaemonConnection fastaDb;
	private DaemonConnection searchDb;
	private SwiftDao dao;
	private SearchDbDao searchDbDao;
	private FileTokenFactory fileTokenFactory;
	private int totalToLoad;
	private int loaded;


	@Override
	public String getName() {
		return "load-to-search-db";
	}

	@Override
	public String getDescription() {
		return "Loads a specified Swift search results (using the search database id) into the search database.";
	}

	/**
	 * Load given search results into the database.
	 * This is equivalent to a "shortened" Swift search that:
	 * 1) dumps .RAW metadata
	 * 2) dumps Scaffold spectrum report (if missing) using Scaffold 3
	 * 3) loads the FASTA database
	 * 4) loads the Scaffold dump
	 *
	 * @param environment The Swift environment to execute within.
	 */
	public ExitCode run(final SwiftEnvironment environment) {
		try {
			final long start = System.currentTimeMillis();

			final SwiftSearcher.Config config = getSearcher(environment.getDaemonConfig());
			initializeConnections(environment, config);

			// Set up the database
			initializeDatabase(environment, config);

			// This is the input parameter - which report to load into the database
			final String loadParameter = environment.getParameters().get(0);

			if ("all".equalsIgnoreCase(loadParameter)) {
				loadAllData();
			} else {
				final long reportDataId = getReportDataId(loadParameter);

				WorkflowEngine workflowEngine = loadData(reportDataId, 0);

				if (workflowEngine != null) {

					// Run the workflow
					while (!workflowEngine.isDone()) {
						workflowEngine.run();
					}
				}
			}

			final long end = System.currentTimeMillis();

			Interval interval = new Interval(start, end);
			LOGGER.info("Elapsed time: " + interval.toDuration().toString());

			return ExitCode.Ok;

		} catch (Exception e) {
			throw new MprcException("Could not load into Swift search database", e);
		}
	}

	/**
	 * Initialize the database referenced by given Swift searcher.
	 *
	 * @param environment Swift environment.
	 * @param config      The configuration of the Swift searcher.
	 */
	public static void initializeDatabase(SwiftEnvironment environment, SwiftSearcher.Config config) {
		final Object database = environment.createResource(config.getDatabase());
	}

	private void loadAllData() {
		List<Long> reportsWithoutAnalysis;
		getSearchDbDao().begin();
		try {
			reportsWithoutAnalysis = getSearchDbDao().getReportIdsWithoutAnalysis();
			getSearchDbDao().commit();
		} catch (Exception e) {
			getSearchDbDao().rollback();
			throw new MprcException("Could not obtain the list of reports to load", e);
		}

		totalToLoad = reportsWithoutAnalysis.size();
		LOGGER.info("Total reports with analysis missing: " + totalToLoad);
		int count = 0;
		List<WorkflowEngine> engines = new ArrayList<WorkflowEngine>(100);
		for (Long reportId : reportsWithoutAnalysis) {
			count++;
			LOGGER.info(MessageFormat.format("Loading report #{0} ({1} of {2})", reportId, count, totalToLoad));
			try {
				final WorkflowEngine engine = loadData(reportId, MAX_INPUT_FILE_SIZE);
				if (engine != null) {
					engines.add(engine);
				}
			} catch (Exception e) {
				// SWALLOWED: We keep going
				LOGGER.error("Could not load " + reportId + "\n - " + MprcException.getDetailedMessage(e));
			}
			if (engines.size() == BATCH_SIZE) {
				runTillDone(engines);
			}
		}
		runTillDone(engines);
	}

	private void runTillDone(final List<WorkflowEngine> engines) {
		while (true) {
			if (runEngines(engines) == true) {
				break;
			}
		}
		engines.clear();
	}

	private boolean runEngines(final List<WorkflowEngine> engines) {
		boolean allDone = true;
		for (WorkflowEngine engine : engines) {
			if (!engine.isDone()) {
				allDone = false;
				try {
					engine.run();
				} catch (MprcException e) {
					// SWALLOWED: We are okay with the engine failing, we log it and go on
					LOGGER.error("The load failed\n" + MprcException.getDetailedMessage(e));
				}
			}
		}
		return allDone;
	}

	private WorkflowEngine loadData(final long reportDataId, final int maxInputFileSize) {
		final WorkflowEngine workflowEngine = new WorkflowEngine("load " + reportDataId);

		getDao().begin();
		try {
			// Load the information about the search
			final ReportData reportData = getReportData(reportDataId);
			final SwiftSearchDefinition swiftSearchDefinition = getSwiftSearchDefinition(reportData);

			if (maxInputFileSize != 0) {
				final int inputSize = swiftSearchDefinition.getInputFiles().size();
				if (inputSize > maxInputFileSize) {
					getDao().commit();
					LOGGER.info("The search for report #" + reportDataId + " uses " + inputSize + " input files, which is more than maximum allowed " + maxInputFileSize);
					return null;
				}
			}

			// Scaffold file is defined as a part of the report
			final File scaffoldFile = getScaffoldFile(reportData);

			if (!scaffoldFile.exists()) {
				throw new MprcException("Scaffold file " + scaffoldFile.getAbsolutePath() + " does not exist");
			}

			// Curation can be obtained from the search definition
			final int curationId = getCurationId(swiftSearchDefinition);

			// Load fasta into database
			final FastaDbTask fastaDbTask = new FastaDbTask(fastaDb, fileTokenFactory, false, curationId);
			workflowEngine.addTask(fastaDbTask);

			// Export files from Scaffold
			final ScaffoldSpectraExportTask scaffoldExportTask = new ScaffoldSpectraExportTask(scaffold3, fileTokenFactory, false, scaffoldFile);
			workflowEngine.addTask(scaffoldExportTask);

			// Load scaffold export into database
			final SearchDbTask searchDbTask = new SearchDbTask(searchDb, fileTokenFactory, false, reportDataId, scaffoldExportTask.getSpectrumExportFile());
			searchDbTask.addDependency(scaffoldExportTask);
			searchDbTask.addDependency(fastaDbTask);
			workflowEngine.addTask(searchDbTask);

			SuccessfulLoadCounter counter = new SuccessfulLoadCounter(reportData.getReportFile().getName());
			counter.addDependency(searchDbTask);
			workflowEngine.addTask(counter);

			for (final FileSearch fileSearch : swiftSearchDefinition.getInputFiles()) {
				// Only load files that made it to Scaffold
				if (fileSearch.isSearch("SCAFFOLD3") || fileSearch.isSearch("SCAFFOLD")) {
					final File rawFile = fileSearch.getInputFile();
					if (!"RAW".equalsIgnoreCase(FileUtilities.getExtension(rawFile.getName()))) {
						// We have a file that is not raw.
						throw new MprcException("Could not load search that uses .mgf file: " + rawFile.getAbsolutePath());
					}
					final RAWDumpTask rawDumpTask = new RAWDumpTask(
							rawFile,
							new File(swiftSearchDefinition.getOutputFolder(), QaTask.QA_SUBDIRECTORY),
							rawDump,
							fileTokenFactory,
							false);

					searchDbTask.addRawDumpTask(rawDumpTask);
					searchDbTask.addDependency(rawDumpTask);
					workflowEngine.addTask(rawDumpTask);
				}
			}


			getDao().commit();
		} catch (Exception e) {
			getDao().rollback();
			throw new MprcException(e);
		}

		workflowEngine.addMonitor(new SearchMonitor() {
			@Override
			public void updateStatistics(ProgressReport report) {
				LOGGER.debug(reportDataId + ":" + report.toString());
			}

			@Override
			public void taskChange(TaskBase task) {
				LOGGER.debug(reportDataId + ":" + "Task " + task.getName() + ": " + task.getState().getText());
			}

			@Override
			public void error(TaskBase task, Throwable t) {
				LOGGER.error(reportDataId + ":" + task.getName(), t);
			}

			@Override
			public void error(Throwable e) {
				LOGGER.error(reportDataId + ":" + "Workflow error", e);
			}

			@Override
			public void taskProgress(TaskBase task, Object progressInfo) {
				LOGGER.debug(reportDataId + ":" + "Task " + task.getName() + " progress: " + progressInfo);
			}
		});
		return workflowEngine;
	}

	/**
	 * This can throw an exception if the particular search run does not store configuration data properly.
	 *
	 * @param swiftSearchDefinition Definition of swift search to obtain the curation from
	 * @return Id of the database used for producing the particular report.
	 */
	private int getCurationId(final SwiftSearchDefinition swiftSearchDefinition) {
		final Curation curation = swiftSearchDefinition.getSearchParameters().getDatabase();
		if (curation == null || curation.getId() == null) {
			throw new MprcException("The search report does not define a database");
		}
		return curation.getId();
	}

	private SwiftSearchDefinition getSwiftSearchDefinition(final ReportData reportData) {
		final Integer swiftSearchDefinitionId = reportData.getSearchRun().getSwiftSearch();
		if (swiftSearchDefinitionId == null) {
			throw new MprcException("The search report does not define search parameters");
		}
		return dao.getSwiftSearchDefinition(swiftSearchDefinitionId);
	}

	/**
	 * @param reportData Data about the report, obtain using {@link #getReportData(long)}
	 * @return Scaffold file (.sf3 or .sfd) for a particular report id.
	 */
	private File getScaffoldFile(final ReportData reportData) {
		return reportData.getReportFile();
	}

	private ReportData getReportData(final long reportDataId) {
		return dao.getReportForId(reportDataId);
	}

	/**
	 * @param parameter Command line parameter.
	 * @return id of the report to load into database from the command line.
	 */
	private long getReportDataId(final String parameter) {
		try {
			return Long.parseLong(parameter);
		} catch (NumberFormatException e) {
			throw new MprcException("Could not parse report # [" + parameter + "]", e);
		}
	}

	public static SwiftSearcher.Config getSearcher(final DaemonConfig daemonConfig) {
		final List<ResourceConfig> searchers = daemonConfig.getApplicationConfig().getModulesOfConfigType(SwiftSearcher.Config.class);
		if (searchers.size() != 1) {
			throw new MprcException("More than one Swift Searcher defined in this Swift install");
		}
		return (SwiftSearcher.Config) searchers.get(0);
	}

	private void initializeConnections(final SwiftEnvironment environment, final SwiftSearcher.Config config) {
		rawDump = getConnection(environment, config.getRawdump(), RAWDumpWorker.NAME);
		scaffold3 = getConnection(environment, config.getScaffold3(), Scaffold3Worker.NAME);
		fastaDb = getConnection(environment, config.getFastaDb(), FastaDbWorker.NAME);
		searchDb = getConnection(environment, config.getSearchDb(), SearchDbWorker.NAME);
	}

	private DaemonConnection getConnection(final SwiftEnvironment environment, final ServiceConfig serviceConfig, final String workerName) {
		final DaemonConnection connection = environment.getConnection(serviceConfig);
		if (connection == null) {
			throw new MprcException("No " + workerName + " worker defined.");
		}
		return connection;
	}

	public SwiftDao getDao() {
		return dao;
	}

	public void setDao(final SwiftDao dao) {
		this.dao = dao;
	}

	public SearchDbDao getSearchDbDao() {
		return searchDbDao;
	}

	public void setSearchDbDao(SearchDbDao searchDbDao) {
		this.searchDbDao = searchDbDao;
	}

	public FileTokenFactory getFileTokenFactory() {
		return fileTokenFactory;
	}

	public void setFileTokenFactory(final FileTokenFactory fileTokenFactory) {
		this.fileTokenFactory = fileTokenFactory;
	}

	/**
	 * Counts all the successful DB loads.
	 */
	private class SuccessfulLoadCounter extends TaskBase {
		private final String scaffoldName;

		private SuccessfulLoadCounter(String scaffoldName) {
			this.scaffoldName = scaffoldName;
		}

		@Override
		public void run() {
			loaded++;
			LOGGER.info("Loaded " + scaffoldName + " (" + loaded + " out of " + totalToLoad + ")");
			setState(TaskState.COMPLETED_SUCCESFULLY);
		}
	}
}
