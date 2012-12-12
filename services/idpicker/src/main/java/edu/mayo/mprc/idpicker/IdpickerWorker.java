package edu.mayo.mprc.idpicker;

import com.google.common.collect.Lists;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ProcessCaller;
import edu.mayo.mprc.utilities.exceptions.ExceptionUtilities;
import edu.mayo.mprc.utilities.progress.ProgressReporter;
import org.apache.log4j.Logger;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Calls <tt>msaccess.exe</tt> to determine whether peak picking should be enabled.
 * Then calls <tt>msconvert.exe</tt>.
 */
public final class IdpickerWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(IdpickerWorker.class);
	public static final String TYPE = "idpicker";
	public static final String NAME = "IDPicker";
	public static final String DESC = "<p>IDPicker uses machine learning algorithms to separate correct and incorrect peptide spectrum matches.</p>" +
			"<p>Inputs are results from the search engines (in .pepXML format), output is an .idp file (sqlite3) with search engine scores " +
			"recalculated to match a particular target FDR.</p>";

	private static final String IDPQONVERT_EXECUTABLE = "idpqonvert";

	private File idpQonvertExecutable;

	public void processRequest(final WorkPacket workPacket, final ProgressReporter progressReporter) {
		try {
			progressReporter.reportStart();
			process(workPacket);
			workPacket.synchronizeFileTokensOnReceiver();
			progressReporter.reportSuccess();
		} catch (Exception t) {
			progressReporter.reportFailure(t);
		}
	}

	private void process(final WorkPacket workPacket) {
		if (!(workPacket instanceof IdpickerWorkPacket)) {
			ExceptionUtilities.throwCastException(workPacket, IdpickerWorkPacket.class);
			return;
		}

		final IdpickerWorkPacket batchWorkPacket = (IdpickerWorkPacket) workPacket;

		LOGGER.debug("Running IDPicker: [" + batchWorkPacket.getInputPathsAsString("], [") + "] -> " + batchWorkPacket.getOutputFile());

		//  check if already exists (skip condition)
		if (isConversionDone(batchWorkPacket)) {
			return;
		}

		List<String> commandLine = new ArrayList<String>();
		commandLine.add(FileUtilities.getAbsoluteFileForExecutables(getIdpQonvertExecutable()).getPath());
		commandLine.addAll(batchWorkPacket.getSettings().toCommandLine());
		commandLine.addAll(batchWorkPacket.getInputFilePaths());

		ProcessBuilder builder = new ProcessBuilder(commandLine);
		builder.directory(idpQonvertExecutable.getParentFile());
		ProcessCaller caller = new ProcessCaller(builder);
		caller.runAndCheck("idpQonvert");
		if (!batchWorkPacket.getOutputFile().exists() || !batchWorkPacket.getOutputFile().canRead() || !batchWorkPacket.getOutputFile().isFile()) {
			throw new MprcException("idpicker failed to create file: " + batchWorkPacket.getOutputFile().getAbsolutePath());
		}
	}

	private static boolean isConversionDone(final IdpickerWorkPacket batchWorkPacket) {
		final File resultFile = batchWorkPacket.getOutputFile();
		if (resultFile.exists()) {
			final long resultModified = resultFile.lastModified();
			for (File inputFile : batchWorkPacket.getInputFiles()) {
				if (inputFile.lastModified() > resultModified) {
					LOGGER.info("The input file [" + inputFile.getAbsolutePath() + "] is newer than [" + resultFile.getAbsolutePath() + "]");
					return false;
				}
			}
			LOGGER.info(resultFile.getAbsolutePath() + " already exists and sufficiently recent.");
			return true;
		}
		return false;
	}

	public String toString() {
		return MessageFormat.format("IDPicker:\n\tidpQonvert={0}\n", getIdpQonvertExecutable().getPath());
	}

	public File getIdpQonvertExecutable() {
		return idpQonvertExecutable;
	}

	public void setIdpQonvertExecutable(File idpQonvertExecutable) {
		this.idpQonvertExecutable = idpQonvertExecutable;
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {

		@Override
		public Worker create(final Config config, final DependencyResolver dependencies) {
			final IdpickerWorker worker = new IdpickerWorker();
			worker.setIdpQonvertExecutable(new File(config.getIdpQonvertExecutable()));
			return worker;
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {

		private String idpQonvertExecutable;

		public Config() {
		}

		public Config(final String idpQonvertExecutable) {
			setIdpQonvertExecutable(idpQonvertExecutable);
		}

		public String getIdpQonvertExecutable() {
			return idpQonvertExecutable;
		}

		public void setIdpQonvertExecutable(final String idpQonvertExecutable) {
			this.idpQonvertExecutable = idpQonvertExecutable;
		}

		public Map<String, String> save(final DependencyResolver resolver) {
			final Map<String, String> map = new TreeMap<String, String>();
			map.put(IDPQONVERT_EXECUTABLE, getIdpQonvertExecutable());
			return map;
		}

		public void load(final Map<String, String> values, final DependencyResolver resolver) {
			setIdpQonvertExecutable(values.get(IDPQONVERT_EXECUTABLE));
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		public void createUI(final DaemonConfig daemon, final ResourceConfig resource, final UiBuilder builder) {
			builder

					.property(IDPQONVERT_EXECUTABLE, "<tt>idpQonvert</tt> path", "Location of IDPicker ver. 3 <tt>idpQonvert</tt>." +
							"<p><a href=\"http://teamcity.fenchurch.mc.vanderbilt.edu/project.html?projectId=project9&tab=projectOverview\">TeamCity download from Vanderbilt</a></p>")
					.required()
					.executable(Lists.<String>newArrayList())
					.defaultValue("idpQonvert");
		}
	}
}
