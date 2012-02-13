package edu.mayo.mprc.scaffold3;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.scaffold.ScaffoldLogMonitor;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ProcessCaller;
import edu.mayo.mprc.utilities.progress.ProgressReporter;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public final class Scaffold3Worker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(Scaffold3Worker.class);
	private static final String SCAFFOLD_BATCH_SCRIPT = "scaffoldBatchScript";
	private static final String REPORT_DECOY_HITS = "reportDecoyHits";
	public static final String TYPE = "scaffold3";
	public static final String NAME = "Scaffold 3";
	public static final String DESC = "Scaffold 3 integrates results from multiple search engines into a single file. You need Scaffold 3 Batch license from <a href=\"http://www.proteomesoftware.com/\">http://www.proteomesoftware.com/</a>";

	private File scaffoldBatchScript;
	private boolean reportDecoyHits;

	public Scaffold3Worker() {
	}

	@Override
	public final void processRequest(WorkPacket workPacket, ProgressReporter progressReporter) {
		try {
			progressReporter.reportStart();
			process(workPacket, progressReporter);
			workPacket.synchronizeFileTokensOnReceiver();
			progressReporter.reportSuccess();
		} catch (Exception t) {
			progressReporter.reportFailure(t);
		}
	}

	private void process(final WorkPacket workPacket, final ProgressReporter progressReporter) {
		if (!(workPacket instanceof Scaffold3WorkPacket)) {
			throw new DaemonException("Unexpected packet type " + workPacket.getClass().getName() + ", expected " + Scaffold3WorkPacket.class.getName());
		}

		Scaffold3WorkPacket scaffoldWorkPacket = (Scaffold3WorkPacket) workPacket;
		LOGGER.debug("Scaffold 3 search processing request");

		File outputFolder = scaffoldWorkPacket.getOutputFolder();
		// Make sure the output folder is there
		FileUtilities.ensureFolderExists(outputFolder);

		File scaffoldWorkFolder = getScaffoldBatchScript().getParentFile();
		File scafmlFile = createScafmlFile(scaffoldWorkPacket, outputFolder);

		ProcessBuilder processBuilder = new ProcessBuilder(getScaffoldBatchScript().getAbsolutePath(), scafmlFile.getAbsolutePath())
				.directory(scaffoldWorkFolder);

		ProcessCaller caller = new ProcessCaller(processBuilder);
		caller.setOutputMonitor(new ScaffoldLogMonitor(progressReporter));

		caller.run();
		int exitValue = caller.getExitValue();

		FileUtilities.restoreUmaskRights(outputFolder, true);

		LOGGER.debug("Scaffold finished with exit value " + exitValue);
		if (exitValue != 0) {
			throw new DaemonException("Non-zero exit value=" + exitValue + " for call " + caller.getCallDescription() + "\n\tStandard out:"
					+ caller.getOutputLog() + "\n\tStandard error:"
					+ caller.getErrorLog());
		}
	}

	public File createScafmlFile(Scaffold3WorkPacket workPacket, File outputFolder) {
		// Create the .scafml file
		workPacket.getScafmlFile().getExperiment().setReportDecoyHits(isReportDecoyHits());
		String scafmlDocument = workPacket.getScafmlFile().getDocument();
		File scafmlFile = new File(outputFolder, workPacket.getExperimentName() + ".scafml");
		FileUtilities.writeStringToFile(scafmlFile, scafmlDocument, true);
		return scafmlFile;
	}


	private File getScaffoldBatchScript() {
		return scaffoldBatchScript;
	}

	private void setScaffoldBatchScript(File scaffoldBatchScript) {
		this.scaffoldBatchScript = scaffoldBatchScript;
	}

	public boolean isReportDecoyHits() {
		return reportDecoyHits;
	}

	public void setReportDecoyHits(boolean reportDecoyHits) {
		this.reportDecoyHits = reportDecoyHits;
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {

		@Override
		public Worker create(Config config, DependencyResolver dependencies) {
			Scaffold3Worker worker = new Scaffold3Worker();
			worker.setScaffoldBatchScript(new File(config.getScaffoldBatchScript()).getAbsoluteFile());
			worker.setReportDecoyHits(config.isReportDecoyHits());

			return worker;
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {

		private String scaffoldBatchScript;
		private boolean reportDecoyHits;

		public Config() {
		}

		public Config(String scaffoldBatchScript) {
			this.scaffoldBatchScript = scaffoldBatchScript;
		}

		public String getScaffoldBatchScript() {
			return scaffoldBatchScript;
		}

		public void setScaffoldBatchScript(String scaffoldBatchScript) {
			this.scaffoldBatchScript = scaffoldBatchScript;
		}

		public boolean isReportDecoyHits() {
			return reportDecoyHits;
		}

		public void setReportDecoyHits(boolean reportDecoyHits) {
			this.reportDecoyHits = reportDecoyHits;
		}

		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put(SCAFFOLD_BATCH_SCRIPT, getScaffoldBatchScript());
			map.put(REPORT_DECOY_HITS, Boolean.toString(isReportDecoyHits()));
			return map;
		}

		public void load(Map<String, String> values, DependencyResolver resolver) {
			setScaffoldBatchScript(values.get(SCAFFOLD_BATCH_SCRIPT));
			setReportDecoyHits(Boolean.parseBoolean(values.get(REPORT_DECOY_HITS)));
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder
					.property(SCAFFOLD_BATCH_SCRIPT, "ScaffoldBatch3 path", "Path to the ScaffoldBatch3 script<p>Default for Linux: <code>/opt/Scaffold3/ScaffoldBatch3</code></p>")
					.defaultValue("/opt/Scaffold3/ScaffoldBatch3")
					.required()
					.executable(Arrays.asList("-v"))

					.property(REPORT_DECOY_HITS, "Report Decoy Hits",
							"<p>When checked, Scaffold will utilize the accession number patterns to distinguish decoy from forward hits.<p>" +
									"<p>This causes FDR rates to be calculated using the number of decoy hits. Scaffold will also display the reverse hits in pink.</p>")
					.boolValue()
					.defaultValue(Boolean.toString(Boolean.TRUE));
		}
	}

}
