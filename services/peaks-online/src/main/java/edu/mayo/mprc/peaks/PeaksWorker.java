package edu.mayo.mprc.peaks;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.peaks.core.*;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.progress.ProgressInfo;
import edu.mayo.mprc.utilities.progress.ProgressReporter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Performs Peak Online search.
 */
public final class PeaksWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(PeaksWorker.class);
	public static final String TYPE = "peaks";
	public static final String NAME = "Peaks";
	public static final String DESC = "Peaks Online search engine support. <p>Peaks is used for de-novo sequencing.</p><p><b>Peaks is currently not supported.</b></p>";

	private Peaks peaksOnline;
	private static final int PEAK_SEARCH_TIMEOUT_HOURS = 24;

	public PeaksWorker(final Peaks peaksOnline) {
		this.peaksOnline = peaksOnline;
	}

	public void processRequest(final WorkPacket workPacket, final ProgressReporter progressReporter) {
		try {
			progressReporter.reportStart();
			process(workPacket, progressReporter);
			workPacket.synchronizeFileTokensOnReceiver();
			progressReporter.reportSuccess();
		} catch (Exception t) {
			// SWALLOWED: We are reporting the exception instead of throwing it
			progressReporter.reportFailure(t);
		}
	}

	private void process(final WorkPacket workPacket, final ProgressReporter progressReporter) {
		if (!(workPacket instanceof PeaksWorkPacket)) {
			throw new DaemonException("Unexpected packet type " + workPacket.getClass().getName() + ", expected " + PeaksWorkPacket.class.getName());
		}

		final PeaksWorkPacket peaksWorkPacket = (PeaksWorkPacket) workPacket;

		try {
			final PeaksSearchParameters searchParameters = new PeaksSearchParameters();
			searchParameters.setParameters(getPeaksParameters(peaksWorkPacket));

			String enzymeId = null;
			String databaseId = null;

			//Verify that the enzyme value exists in Peaks search engine. If enzyme exists, set parameter value to the enzyme id instead of name.
			final Object enzymeParameterValue = searchParameters.getParameter(PeaksSearchParameters.SUBMIT_SEARCH_ENZYME).getParameterValue();
			final PeaksAdmin peaksAdmin = peaksOnline.getPeaksOnlineAdmin();
			for (final PeaksEnzyme enzyme : peaksAdmin.getAllEnzymes()) {
				if (enzyme.getEnzymeName().equals(enzymeParameterValue)) {
					enzymeId = enzyme.getEnzymeId();
					searchParameters.setParameter(PeaksSearchParameters.SUBMIT_SEARCH_ENZYME, enzymeId);
					break;
				}
			}

			if (enzymeId == null) {
				throw new DaemonException("Peaks search engine enzyme, " + enzymeParameterValue + ", does not exist. Create enzyme with same name in Peaks search engine and again.");
			}

			//Verify that the database has been deployed to Peaks search engine. If database is deployed, set paramater value to the database id instead of name.
			final Object databaseParameterValue = searchParameters.getParameter(PeaksSearchParameters.SUBMIT_SEARCH_DATABASE).getParameterValue();
			for (final PeaksDatabase database : peaksAdmin.getAllDatabases()) {
				if (database.getDatabaseName().equals(databaseParameterValue)) {
					databaseId = database.getDatabaseId();
					searchParameters.setParameter(PeaksSearchParameters.SUBMIT_SEARCH_DATABASE, databaseId);
					break;
				}
			}

			if (databaseId == null) {
				throw new DaemonException("Peaks search engine database, " + databaseParameterValue + ", does not exists. Database deployment may have failed or may have not occurred.");
			}

			//Set MGF data file parameter.
			final File mgfFile = peaksWorkPacket.getMgfFile();
			searchParameters.setDataFile(mgfFile);

			//Set Peaks title to mgf file name.
			searchParameters.setTitle(FileUtilities.getFileNameWithoutExtension(mgfFile).toUpperCase(Locale.ENGLISH));

			final PeaksSearch peaksOnlineSearch = peaksOnline.getPeaksOnlineSearch();
			final String searchId = peaksOnlineSearch.submitSearch(searchParameters);
			final CountDownLatch searchDone = new CountDownLatch(1);

			final PeaksSearchMonitor searchMonitor = new PeaksSearchMonitor(searchId, peaksOnline.getPeaksOnlineResult());
			searchMonitor.addPeaksOnlineMonitorListener(new PeaksMonitorListener() {

				public void searchCompleted(final PeaksSearchStatusEvent event) {
					progressReporter.reportSuccess();
					LOGGER.info("Peaks search finished successfully");

					searchMonitor.stop();
					searchDone.countDown();
				}

				public void searchRunning(final PeaksSearchStatusEvent event) {
					final String message = "Peaks search id: " + event.getSearchId() + " is " + event.getStatus();
					progressReporter.reportProgress(new PeaksProgressInfo(message));
					LOGGER.info(message);
				}

				public void searchWaiting(final PeaksSearchStatusEvent event) {
					final String message = "Peaks search id: " + event.getSearchId() + " is " + event.getStatus();
					progressReporter.reportProgress(new PeaksProgressInfo(message));
					LOGGER.info(message);
				}

				public void searchNotFound(final PeaksSearchStatusEvent event) {
					LOGGER.error("Peaks search running");
					progressReporter.reportFailure(new MprcException("Peaks search id: " + event.getSearchId() + " could not be found in Peaks Online search engine."));

					searchMonitor.stop();
					searchDone.countDown();
				}
			});

			searchMonitor.start(5000);
			searchDone.await(PEAK_SEARCH_TIMEOUT_HOURS, TimeUnit.HOURS);

		} catch (IOException e) {
			throw new DaemonException("Peaks search failed while processing work packet: " + workPacket.toString(), e);
		} catch (InterruptedException e) {
			throw new DaemonException("Peaks search failed while waiting for search to be completed. Work packet: " + workPacket.toString(), e);
		}
	}

	private static Map<String, String> getPeaksParameters(final PeaksWorkPacket workPacket) throws IOException {
		if (workPacket.getParamsFile() == null) {
			throw new DaemonException("Peaks parameter file can not be null.");
		}
		final File paramsFile = workPacket.getParamsFile();

		final Properties properties = new Properties();
		final FileInputStream inStream = new FileInputStream(paramsFile);
		try {
			properties.load(inStream);
		} finally {
			inStream.close();
		}

		final Map<String, String> map = new HashMap<String, String>();
		for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
			map.put(entry.getKey().toString(), entry.getValue().toString());
		}
		return map;
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		@Override
		public Worker create(final Config config, final DependencyResolver dependencies) {
			PeaksWorker worker = null;
			try {
				worker = new PeaksWorker(new Peaks(new PeaksURIs(new URI(config.getBaseURI())), config.getUserName(), config.getPassword()));
			} catch (Exception e) {
				throw new MprcException("Peaks worker could not be created.", e);
			}
			return worker;
		}
	}

	public static final class Config extends PeaksConfig {
		public Config() {
		}

		public Config(final String baseURI, final String userName, final String password) {
			super(baseURI, userName, password);
		}
	}

	public static final class Ui implements ServiceUiFactory {
		public void createUI(final DaemonConfig daemon, final ResourceConfig resource, final UiBuilder builder) {
			builder.property("baseURI", "Base URL", "Peaks Online base URL").required()
					.property("userName", "Username", "Administrator account user name").required()
					.property("password", "Password", "Administrator account password").required();
		}
	}

	private static class PeaksProgressInfo implements ProgressInfo {
		private static final long serialVersionUID = 20121221L;
		private final String message;

		public PeaksProgressInfo(final String message) {
			this.message = message;
		}

		public String toString() {
			return message;
		}
	}
}
