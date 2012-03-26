package edu.mayo.mprc.swift.commands;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ServiceConfig;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.fastadb.FastaDbWorker;
import edu.mayo.mprc.qa.RAWDumpWorker;
import edu.mayo.mprc.scaffold3.Scaffold3SpectrumExportWorkPacket;
import edu.mayo.mprc.scaffold3.Scaffold3Worker;
import edu.mayo.mprc.scaffoldparser.spectra.ScaffoldSpectraReader;
import edu.mayo.mprc.searchdb.SearchDbWorker;
import edu.mayo.mprc.swift.db.SwiftDao;
import edu.mayo.mprc.swift.search.SwiftSearcher;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.progress.ProgressInfo;
import edu.mayo.mprc.utilities.progress.ProgressListener;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * @author Roman Zenka
 */
public class LoadToSearchDb implements SwiftCommand {
	private static final Logger LOGGER = Logger.getLogger(LoadToSearchDb.class);
	public static final long TIMEOUT = 100;
	public static final int LOW_PRIORITY = 3;

	private DaemonConnection rawDump;
	private DaemonConnection scaffold3;
	private DaemonConnection fastaDb;
	private DaemonConnection searchDb;
	private SwiftDao dao;

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
	public void run(final SwiftEnvironment environment) {
		try {
			final SwiftSearcher.Config config = getSearcher(environment);
			initializeConnections(environment, config);

			Object database = environment.createResource(config.getDatabase());

			// This is the input parameter - which report to load into the database
			final long reportDataId = getReportDataId(environment.getParameter());

			loadData(reportDataId);

		} catch (Exception e) {
			throw new MprcException("Could not load into Swift search database", e);
		}
	}

	private void loadData(long reportDataId) {
		getDao().begin();
		try {

			// Scaffold file can be looked up using the id of the report
			final File scaffoldFile = getScaffoldFile(reportDataId);

			// Scaffold spectrum export is easily created by changing the extension
			final File scaffoldSpectrumExport = new File(
					scaffoldFile.getParentFile(),
					FileUtilities.getFileNameWithoutExtension(scaffoldFile) + ScaffoldSpectraReader.EXTENSION);

			// We will ask Scaffold3 to ensure the spectrum is exported properly
			final Scaffold3SpectrumExportWorkPacket spectrumExport = new Scaffold3SpectrumExportWorkPacket("export1", false,
					scaffoldFile, scaffoldSpectrumExport);

			runJob(scaffold3, spectrumExport);

//			Map<String, RawFileMetaData> fileMetaDataMap = getFileMetaDataMap();
//			new SearchDbWorkPacket("searchdb1", false, reportDataId, scaffoldSpectrumExport, fileMetaDataMap);

			getDao().commit();
		} catch (Exception e) {
			getDao().rollback();
		}
	}

	/**
	 * @param reportDataId Id of a Swift report.
	 * @return Scaffold file (.sf3 or .sfd) for a particular report id.
	 */
	private File getScaffoldFile(final long reportDataId) {
		return dao.getReportForId(reportDataId).getReportFile();
	}

	/**
	 * Will send given job to given connection and wait until it completes.
	 *
	 * @param daemonConnection Daemon to send work to.
	 * @param workPacket       Work to be performed.
	 */
	private void runJob(final DaemonConnection daemonConnection, final WorkPacket workPacket) {
		final WorkProgress listener = new WorkProgress(workPacket);
		daemonConnection.sendWork(workPacket, LOW_PRIORITY, listener);
		while (true) {
			synchronized (listener.getLock()) {
				try {
					listener.getLock().wait(TIMEOUT);
					if (listener.isFinished()) {
						break;
					}
				} catch (InterruptedException e) {
					LOGGER.error("Interrupted waiting for the job to complete");
					break;
				}
			}
		}
		if (listener.getError() != null) {
			throw new MprcException(listener.getError());
		}
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

	private SwiftSearcher.Config getSearcher(final SwiftEnvironment environment) {
		final List<ResourceConfig> searchers = environment.getDaemonConfig().getApplicationConfig().getModulesOfConfigType(SwiftSearcher.Config.class);
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

	private static class WorkProgress implements ProgressListener {
		private Exception error;
		private final WorkPacket workPacket;
		private boolean finished = false;
		private final Object lock = new Object();

		public WorkProgress(final WorkPacket workPacket) {
			this.workPacket = workPacket;
		}

		public Object getLock() {
			return lock;
		}

		public boolean isFinished() {
			return finished;
		}

		public Exception getError() {
			return error;
		}

		@Override
		public void requestEnqueued(final String hostString) {
			LOGGER.info(workPacket.getTaskId() + ": work enqueued");
		}

		@Override
		public void requestProcessingStarted() {
			LOGGER.info(workPacket.getTaskId() + ": processing started");
		}

		@Override
		public void requestProcessingFinished() {
			LOGGER.info(workPacket.getTaskId() + ": processing finished");
			synchronized (lock) {
				finished = true;
				lock.notifyAll();
			}
		}

		@Override
		public void requestTerminated(final Exception e) {
			synchronized (lock) {
				error = e;
				finished = true;
				LOGGER.error(workPacket.getTaskId() + ": error - " + error);
				lock.notifyAll();
			}
		}

		@Override
		public void userProgressInformation(final ProgressInfo progressInfo) {
		}
	}
}
