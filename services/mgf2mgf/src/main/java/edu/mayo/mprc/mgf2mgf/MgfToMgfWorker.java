package edu.mayo.mprc.mgf2mgf;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.io.mgf.MgfCleanup;
import edu.mayo.mprc.utilities.progress.ProgressReporter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class MgfToMgfWorker implements Worker {

	public static final String TYPE = "mgf2mgf";
	public static final String NAME = ".mgf Cleanup";
	public static final String DESC = "Swift expects <tt>.mgf</tt> headers to be in certain format (indicate the spectrum), so the results of the search engines can be more easily pieced together. If you want to search .mgf files directly, the cleaner has to check that the headers are okay and modify them if they are not. Without this module, Swift cannot process <tt>.mgf</tt> files.";

	public void processRequest(WorkPacket workPacket, ProgressReporter progressReporter) {
		try {
			progressReporter.reportStart();
			process(workPacket, progressReporter);
			workPacket.synchronizeFileTokensOnReceiver();
			progressReporter.reportSuccess();
		} catch (Exception t) {
			progressReporter.reportFailure(t);
		}
	}

	private void process(WorkPacket wp, ProgressReporter reporter) {
		MgfTitleCleanupWorkPacket workPacket = (MgfTitleCleanupWorkPacket) wp;
		File mgfFile = workPacket.getMgfToCleanup();
		File cleanedMgf = workPacket.getCleanedMgf();

		boolean cleanupNeeded = false;
		if (!cleanedMgf.exists()) {
			cleanupNeeded = new MgfCleanup(mgfFile).produceCleanedMgf(cleanedMgf);
		} else {
			// The mgf is already there, therefore it must have been cleaned before, therefore cleanup WAS needed.
			// We must return true, otherwise the caller would use the mgfFile instead of cleanedMgf, although we
			// technically did NOT need to perform a cleanup.
			cleanupNeeded = true;
		}
		// Report whether we did perform the cleanup
		reporter.reportProgress(new MgfTitleCleanupResult(cleanupNeeded));
	}


	public String toString() {
		return "Mgf Title Cleanup";
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		@Override
		public Worker create(Config config, DependencyResolver dependencies) {
			return new MgfToMgfWorker();
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
			return new HashMap<String, String>(1);
		}

		@Override
		public void load(Map<String, String> values, DependencyResolver resolver) {
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
		}
	}
}
