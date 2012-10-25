package edu.mayo.mprc.myrimatch;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
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
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ProcessCaller;
import edu.mayo.mprc.utilities.progress.ProgressReporter;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

public final class MyrimatchWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(MyrimatchWorker.class);
	public static final String TYPE = "myrimatch";
	public static final String NAME = "Myrimatch";
	public static final String DESC = "Myrimatch search engine support. <p>Myrimatch is freely available at <a href=\"http://fenchurch.mc.vanderbilt.edu/software.php#MyriMatch\">http://fenchurch.mc.vanderbilt.edu/software.php#MyriMatch</a>.</p>";

	private File executable;

	public static final String EXECUTABLE = "executable";

	public MyrimatchWorker(final File executable) {
		this.executable = executable;
	}

	public File getExecutable() {
		return executable;
	}

	public void setExecutable(final File executable) {
		this.executable = executable;
	}

	public void processRequest(final WorkPacket workPacket, final ProgressReporter progressReporter) {
		progressReporter.reportStart();

		if (!(workPacket instanceof MyrimatchWorkPacket)) {
			throw new DaemonException("Unexpected packet type " + workPacket.getClass().getName() + ", expected " + MyrimatchWorkPacket.class.getName());
		}

		final MyrimatchWorkPacket packet = (MyrimatchWorkPacket) workPacket;

		try {
			checkPacketCorrectness(packet);

			FileUtilities.ensureFolderExists(packet.getWorkFolder());

			final File fastaFile = packet.getDatabaseFile();

			final File inputFile = packet.getInputFile();
			final File paramsFile = packet.getSearchParamsFile();
			final File resultFile = packet.getOutputFile();

			if (resultFile.exists() && inputFile.exists() && resultFile.lastModified() >= inputFile.lastModified()) {
				progressReporter.reportSuccess();
				return;
			}

			LOGGER.info("Fasta file " + fastaFile.getAbsolutePath() + " does" + (fastaFile.exists() && fastaFile.length() > 0 ? " " : " not ") + "exist.");
			LOGGER.info("Input file " + inputFile.getAbsolutePath() + " does" + (inputFile.exists() && inputFile.length() > 0 ? " " : " not ") + "exist.");
			LOGGER.info("Parameter file " + paramsFile.getAbsolutePath() + " does" + (paramsFile.exists() && paramsFile.length() > 0 ? " " : " not ") + "exist.");

			Files.append("\n# Decoy sequence prefix is appended to all decoy matches" +
					"\nDecoyPrefix = " + packet.getDecoySequencePrefix(),
					paramsFile, Charsets.US_ASCII);

			final List<String> parameters = new LinkedList<String>();
			parameters.add(executable.getPath());
			parameters.add("-cfg");
			parameters.add(paramsFile.getAbsolutePath());
			parameters.add("-ProteinDatabase");
			parameters.add(fastaFile.getAbsolutePath());
			parameters.add("-DEndProteinIndex=" + packet.getNumForwardEntries());
			parameters.add(inputFile.getAbsolutePath());

			final ProcessBuilder processBuilder = new ProcessBuilder(parameters);
			processBuilder.directory(packet.getWorkFolder());

			final ProcessCaller processCaller = new ProcessCaller(processBuilder);

			LOGGER.info("Myrimatch search, " + packet.toString() + ", has been submitted.");
			processCaller.setOutputMonitor(new MyrimatchLogMonitor(progressReporter));
			processCaller.run();

			if (processCaller.getExitValue() != 0) {
				progressReporter.reportFailure(new MprcException("Execution of Myrimatch search engine failed. Error: " + processCaller.getFailedCallDescription()));
			} else {
				final File createdResultFile = new File(packet.getWorkFolder(), FileUtilities.stripExtension(packet.getInputFile().getName()) + ".pepXML");
				if (!createdResultFile.equals(resultFile)) {
					FileUtilities.rename(createdResultFile, resultFile);
				}

				workPacket.synchronizeFileTokensOnReceiver();
				progressReporter.reportSuccess();
				LOGGER.info("Myrimatch search, " + packet.toString() + ", has been successfully completed.");
			}
		} catch (Exception t) {
			progressReporter.reportFailure(t);
		}
	}

	private void checkPacketCorrectness(final MyrimatchWorkPacket packet) {
		if (packet.getSearchParamsFile() == null) {
			throw new MprcException("Params file must not be null");
		}
		if (packet.getWorkFolder() == null) {
			throw new MprcException("Work folder must not be null");
		}
		if (packet.getOutputFile() == null) {
			throw new MprcException("Result file must not be null");
		}
		if (packet.getInputFile() == null) {
			throw new MprcException("Input file must not be null");
		}
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		@Override
		public Worker create(final Config config, final DependencyResolver dependencies) {
			MyrimatchWorker worker = null;
			try {
				worker = new MyrimatchWorker(FileUtilities.getAbsoluteFileForExecutables(new File(config.getExecutable())));
			} catch (Exception e) {
				throw new MprcException("Tandem worker could not be created.", e);
			}
			return worker;
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {
		private String executable;

		public Config() {
		}

		public Config(final String executable) {
			this.executable = executable;
		}

		public String getExecutable() {
			return executable;
		}

		public void setExecutable(final String executable) {
			this.executable = executable;
		}

		public Map<String, String> save(final DependencyResolver resolver) {
			final Map<String, String> map = new TreeMap<String, String>();
			map.put(EXECUTABLE, executable);
			return map;
		}

		public void load(final Map<String, String> values, final DependencyResolver resolver) {
			executable = values.get(EXECUTABLE);
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {

		public void createUI(final DaemonConfig daemon, final ResourceConfig resource, final UiBuilder builder) {
			builder.property(EXECUTABLE, "Executable Path", "Myrimatch executable path. Myrimatch executables can be " +
					"<br/>found at <a href=\"http://fenchurch.mc.vanderbilt.edu/software.php#MyriMatch/\"/>http://fenchurch.mc.vanderbilt.edu/software.php#MyriMatch</a>")
					.required()
					.executable(Arrays.asList("-v"))
					.defaultValue("myrimatch");
		}
	}
}
