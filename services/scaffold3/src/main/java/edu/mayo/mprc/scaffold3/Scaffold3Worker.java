package edu.mayo.mprc.scaffold3;

import com.jamesmurty.utils.XMLBuilder;
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
import edu.mayo.mprc.scaffold.ScaffoldLogMonitor;
import edu.mayo.mprc.scaffoldparser.spectra.ScaffoldSpectraVersion;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ProcessCaller;
import edu.mayo.mprc.utilities.progress.ProgressReporter;
import org.apache.log4j.Logger;

import javax.xml.transform.OutputKeys;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public final class Scaffold3Worker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(Scaffold3Worker.class);
	private static final String SCAFFOLD_BATCH_SCRIPT = "scaffoldBatchScript";
	public static final String TYPE = "scaffold3";
	public static final String NAME = "Scaffold 3";
	public static final String DESC = "Scaffold 3 integrates results from multiple search engines into a single file. You need Scaffold 3 Batch license from <a href=\"http://www.proteomesoftware.com/\">http://www.proteomesoftware.com/</a>";

	private File scaffoldBatchScript;
	private boolean reportDecoyHits;

	public Scaffold3Worker() {
	}

	@Override
	public final void processRequest(final WorkPacket workPacket, final ProgressReporter progressReporter) {
		try {
			progressReporter.reportStart();
			if (workPacket instanceof Scaffold3WorkPacket) {
				processSearch((Scaffold3WorkPacket) workPacket, progressReporter);
			} else if (workPacket instanceof Scaffold3SpectrumExportWorkPacket) {
				processSpectrumExport((Scaffold3SpectrumExportWorkPacket) workPacket, progressReporter);
			} else {
				throw new DaemonException("Unexpected packet type " + workPacket.getClass().getName() + ", expected " + Scaffold3WorkPacket.class.getName() + " or " + Scaffold3SpectrumExportWorkPacket.class.getName());
			}
			workPacket.synchronizeFileTokensOnReceiver();
			progressReporter.reportSuccess();
		} catch (Exception t) {
			progressReporter.reportFailure(t);
		}
	}

	/**
	 * Run Scaffold search.
	 *
	 * @param scaffoldWorkPacket Work to do.
	 * @param progressReporter   Where to report progress.
	 */
	private void processSearch(final Scaffold3WorkPacket scaffoldWorkPacket, final ProgressReporter progressReporter) {
		LOGGER.debug("Scaffold 3 search processing request");

		final File outputFolder = scaffoldWorkPacket.getOutputFolder();
		// Make sure the output folder is there
		FileUtilities.ensureFolderExists(outputFolder);

		final File scaffoldWorkFolder = getScaffoldBatchScript().getParentFile();
		final File scafmlFile = createScafmlFile(scaffoldWorkPacket, outputFolder);

		runScaffold(progressReporter, scafmlFile, scaffoldWorkFolder, outputFolder);
	}

	/**
	 * Run Scaffold spectrum expprt.
	 *
	 * @param scaffoldWorkPacket Work to do.
	 * @param progressReporter   Where to report progress.
	 */
	private void processSpectrumExport(final Scaffold3SpectrumExportWorkPacket scaffoldWorkPacket, final ProgressReporter progressReporter) {
		LOGGER.debug("Scaffold 3 spectrum export request");

		final File result = scaffoldWorkPacket.getSpectrumExportFile();
		if (result.exists() && result.isFile() && result.canRead()) {
			// The file is there. Is it the correct version?
			if (isScaffold3SpectrumExport(result)) {
				LOGGER.info("The spectrum export has already been performed: [" + result.getAbsolutePath() + "]");
				return;
			} else {
				if (!result.delete()) {
					throw new MprcException("Could not delete old version of Scaffold spectrum report: [" + result.getAbsolutePath() + "]");
				}
			}
		}
		final File outputFolder = result.getParentFile();
		// Make sure the parent folder is there
		FileUtilities.ensureFolderExists(outputFolder);

		final File scaffoldWorkFolder = getScaffoldBatchScript().getParentFile();
		final File scafmlFile = createSpectrumExportScafmlFile(scaffoldWorkPacket, outputFolder);

		runScaffold(progressReporter, scafmlFile, scaffoldWorkFolder, outputFolder);

		if (!isScaffold3SpectrumExport(result)) {
			throw new MprcException("Even after rerunning Scaffold, the spectrum report is still the old version");
		}
	}

	/**
	 * @param export The Scaffold spectrum export to check.
	 * @return True if this is a proper Scaffold 3 spectrum export (not an older version).
	 */
	private boolean isScaffold3SpectrumExport(final File export) {
		final ScaffoldSpectraVersion version = new ScaffoldSpectraVersion();
		version.load(export, null/*Not sure which version*/, null/* No progress reporting */);
		// Currently if the version starts with 3, it is deemed ok
		return version.getScaffoldVersion().startsWith("Scaffold_3");
	}

	/**
	 * Execute Scaffold.
	 *
	 * @param progressReporter   Where to report progress.
	 * @param scafmlFile         Scafml file driving Scaffold
	 * @param scaffoldWorkFolder Where should Scaffold run (usually the Scaffold install folder)
	 * @param outputFolder       Where do the Scaffold outputs go.
	 */
	private void runScaffold(final ProgressReporter progressReporter, final File scafmlFile, final File scaffoldWorkFolder, final File outputFolder) {
		final ProcessBuilder processBuilder = new ProcessBuilder(getScaffoldBatchScript().getAbsolutePath(), scafmlFile.getAbsolutePath())
				.directory(scaffoldWorkFolder);

		final ProcessCaller caller = new ProcessCaller(processBuilder);
		caller.setOutputMonitor(new ScaffoldLogMonitor(progressReporter));

		caller.run();
		final int exitValue = caller.getExitValue();

		FileUtilities.restoreUmaskRights(outputFolder, true);

		LOGGER.debug("Scaffold finished with exit value " + exitValue);
		if (exitValue != 0) {
			throw new DaemonException("Non-zero exit value=" + exitValue + " for call " + caller.getCallDescription() + "\n\tStandard out:"
					+ caller.getOutputLog() + "\n\tStandard error:"
					+ caller.getErrorLog());
		}
	}

	/**
	 * Make a scafml file for running Scaffold search.
	 *
	 * @param workPacket   Description of work to do.
	 * @param outputFolder Where should the file be created.
	 * @return Created scafml file.
	 */
	private File createScafmlFile(final Scaffold3WorkPacket workPacket, final File outputFolder) {
		// Create the .scafml file
		final String scafmlDocument = workPacket.getScafmlFile().getDocument();
		final File scafmlFile = workPacket.getScafmlFileLocation();
		FileUtilities.writeStringToFile(scafmlFile, scafmlDocument, true);
		return scafmlFile;
	}

	/**
	 * Create a .scafml file instructing Scaffold to export spectra.
	 *
	 * @param work         Work to do.
	 * @param outputFolder Where to put the {@code .scafml} file.
	 * @return The created .scafml file
	 */
	private File createSpectrumExportScafmlFile(final Scaffold3SpectrumExportWorkPacket work, final File outputFolder) {
		final String experimentName = FileUtilities.stripGzippedExtension(work.getScaffoldFile().getName());
		final File scafmlFile = new File(outputFolder,
				experimentName + "_spectrum_export.scafml");

		final String contents;
		try {
			contents = getScafmlSpectrumExport(work);
		} catch (Exception e) {
			throw new MprcException("Could not export " + scafmlFile.getAbsolutePath(), e);
		}

		FileUtilities.writeStringToFile(scafmlFile, contents, true);
		return scafmlFile;
	}

	/**
	 * @param work Spectrume export to do.
	 * @return String to put into .scafml file that will produce the export.
	 */
	static String getScafmlSpectrumExport(final Scaffold3SpectrumExportWorkPacket work) {
		try {
			final String experimentName = FileUtilities.stripGzippedExtension(work.getScaffoldFile().getName());
			final XMLBuilder builder = XMLBuilder.create("Scaffold");
			builder.a("version", "1.5")
					.e("Experiment")
					.a("name", experimentName)
					.a("load", work.getScaffoldFile().getAbsolutePath())

					.e("DisplayThresholds")
					.a("name", "Some Thresholds")
					.a("id", "thresh")
					.a("proteinProbability", "0.8")
					.a("minimumPeptideCount", "1")
					.a("peptideProbability", "0.8")
					.a("minimumNTT", "1")
					.a("useCharge", "true,true,true")
					.a("useMergedPeptideProbability", "true")
					.t("")
					.up()

					.e("Export")
					.a("type", "spectrum")
					.a("thresholds", "thresh")
					.a("path", work.getSpectrumExportFile().getAbsolutePath())
					.up();

			final Properties outputProperties = new Properties();
			// Explicitly identify the output as an XML document
			outputProperties.setProperty(OutputKeys.METHOD, "xml");
			// Pretty-print the XML output (doesn't work in all cases)
			outputProperties.setProperty(OutputKeys.INDENT, "yes");
			outputProperties.setProperty("{http://xml.apache.org/xalan}indent-amount", "2");
			outputProperties.setProperty(OutputKeys.STANDALONE, "yes");

			return builder.asString(outputProperties);
		} catch (Exception e) {
			throw new MprcException("Could not create .scafml for spectrum export", e);
		}
	}


	private File getScaffoldBatchScript() {
		return scaffoldBatchScript;
	}

	private void setScaffoldBatchScript(final File scaffoldBatchScript) {
		this.scaffoldBatchScript = scaffoldBatchScript;
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {

		@Override
		public Worker create(final Config config, final DependencyResolver dependencies) {
			final Scaffold3Worker worker = new Scaffold3Worker();
			worker.setScaffoldBatchScript(new File(config.getScaffoldBatchScript()).getAbsoluteFile());

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

		public Config(final String scaffoldBatchScript) {
			this.scaffoldBatchScript = scaffoldBatchScript;
		}

		public String getScaffoldBatchScript() {
			return scaffoldBatchScript;
		}

		public void setScaffoldBatchScript(final String scaffoldBatchScript) {
			this.scaffoldBatchScript = scaffoldBatchScript;
		}

		public boolean isReportDecoyHits() {
			return reportDecoyHits;
		}

		public void setReportDecoyHits(final boolean reportDecoyHits) {
			this.reportDecoyHits = reportDecoyHits;
		}

		public Map<String, String> save(final DependencyResolver resolver) {
			final Map<String, String> map = new TreeMap<String, String>();
			map.put(SCAFFOLD_BATCH_SCRIPT, getScaffoldBatchScript());
			return map;
		}

		public void load(final Map<String, String> values, final DependencyResolver resolver) {
			setScaffoldBatchScript(values.get(SCAFFOLD_BATCH_SCRIPT));
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		public void createUI(final DaemonConfig daemon, final ResourceConfig resource, final UiBuilder builder) {
			builder
					.property(SCAFFOLD_BATCH_SCRIPT, "ScaffoldBatch3 path", "Path to the ScaffoldBatch3 script<p>Default for Linux: <code>/opt/Scaffold3/ScaffoldBatch3</code></p>")
					.defaultValue("/opt/Scaffold3/ScaffoldBatch3")
					.required()
					.executable(Arrays.asList("-v"));
		}
	}

}
