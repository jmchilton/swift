package edu.mayo.mprc.qa;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.config.ui.WrapperScriptSwitcher;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ProcessCaller;
import edu.mayo.mprc.utilities.progress.ProgressReporter;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Worker extracts data from given raw file.
 */
public final class RAWDumpWorker implements Worker {

	private static final Logger LOGGER = Logger.getLogger(RAWDumpWorker.class);

	public static final String RAW_FILE_CMDLINE_OPTION = "--raw";
	public static final String INFO_FILE_CMDLINE_OPTION = "--info";
	public static final String SPECTRA_FILE_CMDLINE_OPTION = "--spectra";
	public static final String CHROMATOGRAM_FILE_CMDLINE_OPTION = "--chromatogram";
	public static final String PARAM_FILE_CMDLINE_OPTION = "--params";
	public static final String TYPE = "rawdump";
	public static final String NAME = "RAW Dump";
	public static final String DESC = "Extracts information about experiment and spectra from RAW files.";

	private File wrapperScript;
	private String windowsExecWrapperScript;

	private File rawDumpExecutable;
	private String commandLineOptions;

	private File tempParamFile;
	// If the raw file path is longer than this, we will attempt to shorten it
	private static final int MAX_UNSHORTENED_PATH_LENGTH = 100;

	protected RAWDumpWorker(Config config) {
		setWrapperScript(config.getWrapperScript() != null && config.getWrapperScript().length() > 0 ? new File(config.getWrapperScript()) : null);
		setWindowsExecWrapperScript(config.getWindowsExecWrapperScript());
	}

	@Override
	public void processRequest(WorkPacket workPacket, ProgressReporter progressReporter) {
		try {
			progressReporter.reportStart();
			processLocal(workPacket);
			workPacket.synchronizeFileTokensOnReceiver();
			progressReporter.reportSuccess();
		} catch (Exception t) {
			progressReporter.reportFailure(t);
		} finally {
			FileUtilities.deleteNow(tempParamFile);
		}
	}

	protected void processLocal(WorkPacket workPacket) throws IOException {
		RAWDumpWorkPacket rawDumpWorkPacket = (RAWDumpWorkPacket) workPacket;

		File rawInfo = rawDumpWorkPacket.getRawInfoFile();
		File rawSpectra = rawDumpWorkPacket.getRawSpectraFile();
		File rawFile = rawDumpWorkPacket.getRawFile();
		File chromatogramFile = rawDumpWorkPacket.getChromatogramFile();

		FileUtilities.ensureFolderExists(rawInfo.getParentFile());
		FileUtilities.ensureFolderExists(rawSpectra.getParentFile());

		File shortenedRawFile = null;
		if (rawFile.getAbsolutePath().length() > MAX_UNSHORTENED_PATH_LENGTH) {
			try {
				shortenedRawFile = FileUtilities.shortenFilePath(rawFile);
			} catch (Exception ignore) {
				// SWALLOWED: Failed shortening does not necessarily mean a problem
				shortenedRawFile = null;
			}
		}

		final List<String> commandLine = getCommandLine(shortenedRawFile != null ? shortenedRawFile : rawFile, rawInfo, rawSpectra, chromatogramFile);
		process(commandLine, true/*windows executable*/, wrapperScript, windowsExecWrapperScript);

		if (shortenedRawFile != null) {
			FileUtilities.cleanupShortenedPath(shortenedRawFile);
		}

		if (!rawInfo.exists() || rawInfo.length() == 0 || !rawSpectra.exists() || rawSpectra.length() == 0) {
			throw new MprcException("Raw dump has failed to create output files, " + rawInfo.getAbsolutePath() + " and " + rawSpectra.getAbsolutePath() + ".");
		}
	}

	private List<String> getCommandLine(File rawFile, File rawInfo, File rawSpectra, File chromatogramFile) throws IOException {

		createParamFile(rawFile, rawInfo, rawSpectra, chromatogramFile);

		List<String> commandLineParams = new LinkedList<String>();
		commandLineParams.add(rawDumpExecutable.getAbsolutePath());
		commandLineParams.add(PARAM_FILE_CMDLINE_OPTION);
		commandLineParams.add(tempParamFile.getAbsolutePath());

		return commandLineParams;
	}

	private void createParamFile(File rawFile, File rawInfo, File rawSpectra, File chromatogramFile) throws IOException {
		tempParamFile = File.createTempFile("inputParamFile", null);

		LOGGER.info("Creating parameter file: " + tempParamFile.getAbsolutePath() + ".");

		BufferedWriter bufferedWriter = null;

		try {
			bufferedWriter = new BufferedWriter(new FileWriter(tempParamFile));

			StringTokenizer stringTokenizer = new StringTokenizer(commandLineOptions, ",");

			while (stringTokenizer.hasMoreTokens()) {
				bufferedWriter.write(stringTokenizer.nextToken().trim());
				bufferedWriter.write("\n");
			}

			bufferedWriter.write(RAW_FILE_CMDLINE_OPTION);
			bufferedWriter.write("\n");
			bufferedWriter.write(rawFile.getAbsolutePath());
			bufferedWriter.write("\n");
			bufferedWriter.write(INFO_FILE_CMDLINE_OPTION);
			bufferedWriter.write("\n");
			bufferedWriter.write(rawInfo.getAbsolutePath());
			bufferedWriter.write("\n");
			bufferedWriter.write(SPECTRA_FILE_CMDLINE_OPTION);
			bufferedWriter.write("\n");
			bufferedWriter.write(rawSpectra.getAbsolutePath());
			bufferedWriter.write("\n");
			bufferedWriter.write(CHROMATOGRAM_FILE_CMDLINE_OPTION);
			bufferedWriter.write("\n");
			bufferedWriter.write(chromatogramFile.getAbsolutePath());
			bufferedWriter.write("\n");

		} catch (IOException e) {
			throw new MprcException("Failed to created param file: " + tempParamFile.getAbsolutePath() + ".", e);
		} finally {
			FileUtilities.closeObjectQuietly(bufferedWriter);
		}
	}

	public File getWrapperScript() {
		return wrapperScript;
	}

	public void setWrapperScript(File wrapperScript) {
		this.wrapperScript = wrapperScript;
	}

	public String getWindowsExecWrapperScript() {
		return windowsExecWrapperScript;
	}

	public void setWindowsExecWrapperScript(String windowsExecWrapperScript) {
		this.windowsExecWrapperScript = windowsExecWrapperScript;
	}

	public File getRawDumpExecutable() {
		return rawDumpExecutable;
	}

	public void setRawDumpExecutable(File rawDumpExecutable) {
		this.rawDumpExecutable = rawDumpExecutable;
	}

	public String getCommandLineOptions() {
		return commandLineOptions;
	}

	public void setCommandLineOptions(String commandLineOptions) {
		this.commandLineOptions = commandLineOptions;
	}

	/**
	 * Generic method that can execute a given command line, wrapping it properly on windows etc.
	 * TODO: This is coupled to how we process packets on Windows - simplify, clean up.
	 *
	 * @param wrapperScript        The outer script to wrap the command line call into.
	 * @param windowsWrapperScript In case our executable is a windows executable and we are not on a windows
	 *                             platform, this wrapper will turn the executable into something that would run.
	 *                             Typically this wrapper is a script that executes <c>wine</c> or <c>wineconsole</c>.
	 */
	static void process(List<String> commandLine, boolean isWindowsExecutable, final File wrapperScript, final String windowsWrapperScript) {
		List<String> parameters = new ArrayList<String>();

		if (wrapperScript != null) {
			parameters.add(wrapperScript.getAbsolutePath());
		}

		if (isWindowsExecutable && windowsWrapperScript != null && !FileUtilities.isWindowsPlatform() && windowsWrapperScript.length() > 0) {
			parameters.add(windowsWrapperScript);
		}

		parameters.addAll(commandLine);

		LOGGER.info("Running command from the following parameters " + parameters.toString());

		ProcessBuilder builder = new ProcessBuilder(parameters.toArray(new String[parameters.size()]));

		ProcessCaller caller = new ProcessCaller(builder);

		try {
			caller.run();
		} catch (Exception t) {
			throw new MprcException("External process call failed: " + caller.getFailedCallDescription(), t);
		}

		LOGGER.debug("External process call returned " + caller.getExitValue());

		if (caller.getExitValue() != 0) {
			throw new MprcException("External process call failed: " + caller.getFailedCallDescription());
		}
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		public Worker create(Config config, DependencyResolver dependencies) {
			RAWDumpWorker worker = new RAWDumpWorker(config);

			//Raw dump values
			worker.setRawDumpExecutable(FileUtilities.getAbsoluteFileForExecutables(new File(config.getRawDumpExecutable())));
			worker.setCommandLineOptions(config.getCommandLineOptions());

			return worker;
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {
		private String wrapperScript;
		private String windowsExecWrapperScript;

		private String rawDumpExecutable;
		private String commandLineOptions;

		public Config() {
			super();
		}

		public Config(String rawDumpExecutable, String commandLineOptions) {
			super();
			this.rawDumpExecutable = rawDumpExecutable;
			this.commandLineOptions = commandLineOptions;
		}

		public String getWrapperScript() {
			return wrapperScript;
		}

		public String getWindowsExecWrapperScript() {
			return windowsExecWrapperScript;
		}

		public String getRawDumpExecutable() {
			return rawDumpExecutable;
		}

		public String getCommandLineOptions() {
			return commandLineOptions;
		}

		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put("wrapperScript", wrapperScript);
			map.put("windowsExecWrapperScript", windowsExecWrapperScript);
			map.put("rawDumpExecutable", rawDumpExecutable);
			map.put("commandLineOptions", commandLineOptions);
			return map;
		}

		public void load(Map<String, String> values, DependencyResolver resolver) {
			wrapperScript = values.get("wrapperScript");
			windowsExecWrapperScript = values.get("windowsExecWrapperScript");
			rawDumpExecutable = values.get("rawDumpExecutable");
			commandLineOptions = values.get("commandLineOptions");
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		private static final String WINDOWS_EXEC_WRAPPER_SCRIPT = "windowsExecWrapperScript";
		private static final String WRAPPER_SCRIPT = "wrapperScript";

		private static final String DEFAULT_RAWDUMP_EXEC = "bin/rawExtract/MprcExtractRaw.exe";
		private static final String DEFAULT_CMDLINE_OPTIONS = "--data";

		public void createUI(final DaemonConfig daemon, final ResourceConfig resource, UiBuilder builder) {
			builder.property("rawDumpExecutable", "Executable Path", "RAW Dump executable path."
					+ "<br/>The RAW Dump executable has been inplemented in house and is included with the Swift installation. "
					+ "<br/>Executable can be found in the Swift installation directory: "
					+ "<br/><tt>" + DEFAULT_RAWDUMP_EXEC + "</tt>").executable(Arrays.asList("-v"))
					.required()
					.defaultValue(DEFAULT_RAWDUMP_EXEC)

					.property("commandLineOptions", "Command Line Options",
							"<br/>Command line option --data is required for this application to generate RAW file information related files. Multiple command line options must be separated by commas.")
					.required()
					.defaultValue(DEFAULT_CMDLINE_OPTIONS);

			builder.property(WINDOWS_EXEC_WRAPPER_SCRIPT, "Windows Program Wrapper Script",
					"<p>This is needed only for Linux when running Windows executables. On Windows, leave this field blank.</p>" +
							"<p>A wrapper script takes the Windows command as a parameter and executes on the Linux Platform.</p>"
							+ "<p>On Linux we suggest using <tt>" + DaemonConfig.WINECONSOLE_CMD + "</tt>. You need to have X Window System installed for <tt>" + DaemonConfig.WINECONSOLE_CMD
							+ "</tt> to work, or use the X virtual frame buffer for headless operation (see below).</p>"
							+ "<p>Alternatively, use <tt>" + DaemonConfig.WINE_CMD + "</tt> without need to run X, but in our experience <tt>" + DaemonConfig.WINE_CMD + "</tt> is less stable.</p>")
					.executable(Arrays.asList("-v"))
					.defaultValue(daemon.getWrapperScript())

					.property(WRAPPER_SCRIPT, "Wrapper Script",
							"<p>This an optional wrapper script in case some pre-processing and set up is needed before running command, for example, this is needed for Linux if the command"
									+ " to run is a Windows executable.</p><p>Default values are set up to allowed Windows executables to run in Linux.</p>"
									+ "<p>The default wrapper script makes sure there is X window system set up and ready to be used by <tt>wineconsole</tt> (see above).</p>"
									+ "<p>We provide a script <tt>" + DaemonConfig.XVFB_CMD + "</tt> that does just that - feel free to modify it to suit your needs. "
									+ " The script uses <tt>Xvfb</tt> - X virtual frame buffer, so <tt>Xvfb</tt>"
									+ " has to be functional on the host system.</p>"
									+ "<p>If you do not require this functionality, leave the field blank.</p>")
					.executable(Arrays.asList("-v"))
					.defaultValue(daemon.getXvfbWrapperScript())
					.addDaemonChangeListener(new WrapperScriptSwitcher(resource, daemon, WINDOWS_EXEC_WRAPPER_SCRIPT));
		}

	}

}
