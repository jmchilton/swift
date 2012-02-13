package edu.mayo.mprc.scaffold.report;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.utilities.progress.ProgressReporter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class ScaffoldReportWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(ScaffoldReportWorker.class);
	public static final String TYPE = "scaffoldReport";
	public static final String NAME = "Scaffold Report";
	public static final String DESC = "Automatically exports an excel peptide report from Scaffold. Useful if you want to provide reports to customers unable or unwilling to use Scaffold. Requires 2.2.03 or newer version of Scaffold Batch.";

	/**
	 * Null Constructor
	 */
	public ScaffoldReportWorker() {
	}

	/**
	 * Processes given request data.
	 * The is responsible for a call {@link edu.mayo.mprc.utilities.progress.ProgressReporter#reportSuccess()} or {@link edu.mayo.mprc.utilities.progress.ProgressReporter#reportFailure(Throwable)}
	 * to signalize whether it succeeded or failed. This call can be performed after the method is done executing,
	 * e.g. be scheduled for later time. You can also report failure or success and keep executing, as long as you do not
	 * report success or failure twice in a row.
	 *
	 * @param workPacket       Work packet to be processed.
	 * @param progressReporter To report progress, success or failures.
	 */
	public void processRequest(WorkPacket workPacket, ProgressReporter progressReporter) {
		try {
			progressReporter.reportStart();
			process(workPacket);
			workPacket.synchronizeFileTokensOnReceiver();
			progressReporter.reportSuccess();
		} catch (Exception t) {
			progressReporter.reportFailure(t);
		}
	}

	private void process(WorkPacket workPacket) {
		if (workPacket instanceof ScaffoldReportWorkPacket) {

			ScaffoldReportWorkPacket scaffoldReportWorkPacket = ScaffoldReportWorkPacket.class.cast(workPacket);

			File peptideReport = scaffoldReportWorkPacket.getPeptideReportFile();
			File proteinReport = scaffoldReportWorkPacket.getProteinReportFile();

			if (peptideReport.exists() && peptideReport.length() > 0 && proteinReport.exists() && proteinReport.length() > 0) {
				LOGGER.info("Scaffold report output files: " + peptideReport.getName() + " and " + proteinReport.getName() + " already exist. Skipping scaffold report generation.");
				return;
			}

			List<File> fileArrayList = new ArrayList<File>(scaffoldReportWorkPacket.getScaffoldOutputFiles().size());

			for (File file : scaffoldReportWorkPacket.getScaffoldOutputFiles()) {
				fileArrayList.add(file);
			}

			try {
				ScaffoldReportBuilder.buildReport(fileArrayList, peptideReport, proteinReport);
			} catch (IOException e) {
				throw new MprcException("Failed to process scaffold report work packet.", e);
			}

		} else {
			throw new MprcException("Failed to process scaffold report work packet, expecting type " +
					ScaffoldReportWorkPacket.class.getName() + " instead of " + workPacket.getClass().getName());
		}
	}


	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		@Override
		public Worker create(Config config, DependencyResolver dependencies) {
			return new ScaffoldReportWorker();
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {

		public Config() {
		}

		@Override
		public Map<String, String> save(DependencyResolver resolver) {
			return new TreeMap<String, String>();
		}

		@Override
		public void load(Map<String, String> values, DependencyResolver resolver) {
			//Do nothing
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			// No UI needed
		}
	}
}
