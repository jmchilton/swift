package edu.mayo.mprc.scaffold;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ProcessCaller;
import edu.mayo.mprc.utilities.progress.ProgressReporter;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * Runs scaffold search directly (without grid engine).
 */
public final class ScaffoldWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(ScaffoldWorker.class);
	private static final String SCAFFOLD_DIR = "scaffoldDir";
	private static final String SCAFFOLD_JAVA_VM_PATH = "scaffoldJavaVmPath";
	private static final String MEMORY_LIMIT = "memoryLimit";
	public static final String TYPE = "scaffold";
	public static final String NAME = "Scaffold";
	public static final String DESC = "Scaffold 2 integrates results from multiple search engines into a single file. You need Scaffold 2 Batch license from <a href=\"http://www.proteomesoftware.com/\">http://www.proteomesoftware.com/</a>";

	private List<String> args = null;
	private File scaffoldDir;
	private String scaffoldJavaVmPath = "java";
	private String memoryLimit = "256M";

	public String toString() {
		StringBuilder arguments = new StringBuilder();
		if (args != null) {
			for (String arg : args) {
				arguments.append(arg);
				arguments.append(" ");
			}
		}
		return "Scaffold worker that executes Scaffold directly\n\tArgs: " + arguments;

	}

	public File getScaffoldDir() {
		return scaffoldDir;
	}

	public void setScaffoldDir(File scaffoldDir) {
		this.scaffoldDir = scaffoldDir;
	}

	public String getScaffoldJavaVmPath() {
		return scaffoldJavaVmPath;
	}

	public void setScaffoldJavaVmPath(String scaffoldJavaVmPath) {
		this.scaffoldJavaVmPath = scaffoldJavaVmPath;
	}

	public String getMemoryLimit() {
		return memoryLimit;
	}

	public void setMemoryLimit(String memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

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

	private void process(final WorkPacket workPacket, final ProgressReporter progressReporter) {
		if (args == null) {
			initialize();
		}
		if (!(workPacket instanceof ScaffoldWorkPacket)) {
			throw new DaemonException("Unexpected packet type " + workPacket.getClass().getName() + ", expected " + ScaffoldWorkPacket.class.getName());
		}

		ScaffoldWorkPacket scaffoldWorkPacket = (ScaffoldWorkPacket) workPacket;
		LOGGER.debug("Scaffold search processing request");

		File outputFolder = scaffoldWorkPacket.getOutputFolder();
		// Make sure the output folder is there
		FileUtilities.ensureFolderExists(outputFolder);

		ScaffoldArgsBuilder scaffoldArgsBuilder = new ScaffoldArgsBuilder(scaffoldDir);
		// Returns work folder for scaffold. Depending on the version, it is either the folder where the output is produced,
		// or the Scaffold install folder itself.
		File scaffoldWorkFolder = scaffoldArgsBuilder.getWorkFolder(outputFolder);
		// Make sure the work folder is there.
		FileUtilities.ensureFolderExists(scaffoldWorkFolder);
		File scafmlFile = createScafmlFile(scaffoldWorkPacket, outputFolder);

		List<String> thisargs = new ArrayList<String>(args.size() + 2);
		thisargs.add(scaffoldJavaVmPath);
		for (String arg : args) {
			thisargs.add(arg);
		}
		thisargs.add(scafmlFile.getAbsolutePath());

		ProcessBuilder processBuilder = new ProcessBuilder(thisargs)
				.directory(scaffoldWorkFolder);

		ProcessCaller caller = new ProcessCaller(processBuilder);
		caller.setOutputMonitor(new ScaffoldLogMonitor(progressReporter));

		caller.run();
		int exitValue = caller.getExitValue();

		FileUtilities.restoreUmaskRights(outputFolder, true);

		LOGGER.debug("Scaffold finished with exit value " + String.valueOf(exitValue));
		if (exitValue != 0) {
			throw new DaemonException("Non-zero exit value=" + exitValue + " for call " + caller.getCallDescription() + "\n\tStandard out:"
					+ caller.getOutputLog() + "\n\tStandard error:"
					+ caller.getErrorLog());
		}
	}

	public static File createScafmlFile(ScaffoldWorkPacket workPacket, File outputFolder) {
		// Create the .scafml file
		String scafmlDocument = workPacket.getScafmlFile().getDocument();
		File scafmlFile = new File(outputFolder, workPacket.getExperimentName() + ".scafml");
		FileUtilities.writeStringToFile(scafmlFile, scafmlDocument, true);
		return scafmlFile;
	}

	public void initialize() {
		ScaffoldArgsBuilder execution = new ScaffoldArgsBuilder(scaffoldDir);
		args = execution.buildScaffoldArgs(memoryLimit, execution.getScaffoldBatchClassName());
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {

		@Override
		public Worker create(Config config, DependencyResolver dependencies) {
			ScaffoldWorker worker = new ScaffoldWorker();
			worker.setScaffoldDir(new File(config.getScaffoldDir()).getAbsoluteFile());
			worker.setScaffoldJavaVmPath(config.getScaffoldJavaVmPath());
			worker.setMemoryLimit(config.getMemoryLimit());

			return worker;
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {

		private String scaffoldDir;
		private String scaffoldJavaVmPath;
		private String memoryLimit;

		public Config() {
		}

		public Config(String scaffoldDir, String scaffoldJavaVmPath, String memoryLimit) {
			this.scaffoldDir = scaffoldDir;
			this.scaffoldJavaVmPath = scaffoldJavaVmPath;
			this.memoryLimit = memoryLimit;
		}

		public String getMemoryLimit() {
			return memoryLimit;
		}

		public void setMemoryLimit(String memoryLimit) {
			this.memoryLimit = memoryLimit;
		}

		public String getScaffoldDir() {
			return scaffoldDir;
		}

		public void setScaffoldDir(String scaffoldDir) {
			this.scaffoldDir = scaffoldDir;
		}

		public String getScaffoldJavaVmPath() {
			return scaffoldJavaVmPath;
		}

		public void setScaffoldJavaVmPath(String scaffoldJavaVmPath) {
			this.scaffoldJavaVmPath = scaffoldJavaVmPath;
		}

		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put(SCAFFOLD_DIR, scaffoldDir);
			map.put(SCAFFOLD_JAVA_VM_PATH, scaffoldJavaVmPath);
			map.put(MEMORY_LIMIT, memoryLimit);
			return map;
		}

		public void load(Map<String, String> values, DependencyResolver resolver) {
			scaffoldDir = values.get(SCAFFOLD_DIR);
			scaffoldJavaVmPath = values.get(SCAFFOLD_JAVA_VM_PATH);
			memoryLimit = values.get(MEMORY_LIMIT);
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder
					.property(SCAFFOLD_DIR, "Installation Folder", "Scaffold installation folder")
					.required()
					.existingDirectory()

					.property(SCAFFOLD_JAVA_VM_PATH, "Java VM Path", "<tt>java</tt> executable to run Scaffold with")
					.required()
					.executable(Arrays.asList("-version"))
					.defaultValue("java")

					.property(MEMORY_LIMIT, "Memory", "Memory requirement to execute Scaffold. Example, 256m is 256 megabytes")
					.required()
					.defaultValue("256m");
		}
	}

}
