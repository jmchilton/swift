package edu.mayo.mprc.xtandem;

import com.google.common.collect.ImmutableMap;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.PropertyChangeListener;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.config.ui.UiResponse;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ProcessCaller;
import edu.mayo.mprc.utilities.StreamRegExMatcher;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XTandemWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(XTandemWorker.class);
	public static final String TYPE = "tandem";
	public static final String NAME = "X!Tandem";
	public static final String DESC = "X!Tandem search engine support. <p>X!Tandem is freely available at <a href=\"http://www.thegpm.org/TANDEM/\">http://www.thegpm.org/TANDEM/</a>. We include the binaries directly in Swift install for your convenience.</p>";

	private File tandemExecutable;

	public static final String TANDEM_EXECUTABLE = "tandemExecutable";

	public XTandemWorker(File tandemExecutable) {
		this.tandemExecutable = tandemExecutable;
	}

	public File getTandemExecutable() {
		return tandemExecutable;
	}

	public void setTandemExecutable(File tandemExecutable) {
		this.tandemExecutable = tandemExecutable;
	}

	public void processRequest(WorkPacket workPacket, ProgressReporter progressReporter) {
		progressReporter.reportStart();

		if (!(workPacket instanceof XTandemWorkPacket)) {
			throw new DaemonException("Unexpected packet type " + workPacket.getClass().getName() + ", expected " + XTandemWorkPacket.class.getName());
		}

		XTandemWorkPacket packet = (XTandemWorkPacket) workPacket;

		try {
			checkPacketCorrectness(packet);
			packet.waitForInputFiles();

			FileUtilities.ensureFolderExists(packet.getWorkFolder());

			File taxonomyXmlFile = createTaxonomyXmlFile(packet);

			createDefaultInputXml(packet);

			final int initialThreads = getNumThreads();
			ProcessCaller processCaller = runTandemSearch(packet, taxonomyXmlFile, initialThreads);
			if (processCaller.getExitValue() != 0 && initialThreads > 1) {
				// Failure, try running with fewer threads
				LOGGER.warn("X!Tandem failed, rerunning with fewer threads");
				processCaller = runTandemSearch(packet, taxonomyXmlFile, 1);
			}

			if (processCaller.getExitValue() != 0) {
				progressReporter.reportFailure(new MprcException("Execution of tandem search engine failed. Error: " + processCaller.getFailedCallDescription()));
			} else {
				workPacket.synchronizeFileTokensOnReceiver();
				progressReporter.reportSuccess();
				LOGGER.info("Tandem search, " + packet.toString() + ", has been successfully completed.");
			}
		} catch (Exception t) {
			progressReporter.reportFailure(t);
		} finally {
			cleanUp(packet);
		}
	}

	private ProcessCaller runTandemSearch(XTandemWorkPacket packet, File taxonomyXmlFile, int threads) {
		File fastaFile = packet.getDatabaseFile();
		File inputFile = packet.getInputFile();
		File paramsFile = packet.getSearchParamsFile();

		LOGGER.info("Running tandem search using " + threads + " threads: " + packet.toString());
		LOGGER.info("\tFasta file " + fastaFile.getAbsolutePath() + " does" + (fastaFile.exists() && fastaFile.length() > 0 ? " " : " not ") + "exist.");
		LOGGER.info("\tInput file " + inputFile.getAbsolutePath() + " does" + (inputFile.exists() && inputFile.length() > 0 ? " " : " not ") + "exist.");
		LOGGER.info("\tParameter file " + paramsFile.getAbsolutePath() + " does" + (paramsFile.exists() && paramsFile.length() > 0 ? " " : " not ") + "exist.");

		File paramFile = createTransformedTemplate(
				paramsFile,
				packet.getWorkFolder(),
				packet.getOutputFile(),
				inputFile,
				taxonomyXmlFile,
				XTandemMappings.DATABASE_TAXON,
				threads
		);

		List<String> parameters = new LinkedList<String>();
		parameters.add(tandemExecutable.getPath());
		parameters.add(paramFile.getAbsolutePath());

		ProcessBuilder processBuilder = new ProcessBuilder(parameters);
		processBuilder.directory(packet.getWorkFolder());

		ProcessCaller processCaller = new ProcessCaller(processBuilder);

		processCaller.run();
		return processCaller;
	}

	private File createTaxonomyXmlFile(XTandemWorkPacket packet) {
		final File fastaFile = packet.getDatabaseFile();
		final String resultFileName = packet.getOutputFile().getName();
		final String resultFileNameWithoutExtension = resultFileName.substring(0, resultFileName.length() - ".xml".length());
		File taxonomyXmlFile = new File(packet.getOutputFile().getParentFile(), resultFileNameWithoutExtension + ".taxonomy.xml");
		String taxonomyContents = "<?xml version=\"1.0\"?>\n" +
				"<bioml label=\"x! taxon-to-file matching list\">\n" +
				"\t<taxon label=\"" + XTandemMappings.DATABASE_TAXON + "\">\n" +
				"\t\t<file format=\"peptide\" URL=\"" + fastaFile.getAbsolutePath() + "\" />\n" +
				"\t</taxon>\n" +
				"</bioml>";

		FileUtilities.writeStringToFile(taxonomyXmlFile, taxonomyContents, true);
		return taxonomyXmlFile;
	}

	/**
	 * Create default_input.xml required for new version of XTandem.
	 */
	private void createDefaultInputXml(XTandemWorkPacket packet) {
		String defaultInputContent = "<?xml version=\"1.0\"?>\n" +
				"<?xml-stylesheet type=\"text/xsl\" href=\"tandem-input-style.xsl\"?>\n" +
				"<bioml></bioml>";
		FileUtilities.writeStringToFile(new File(packet.getOutputFile().getParentFile(), "default_input.xml"), defaultInputContent, true);
	}

	private void checkPacketCorrectness(XTandemWorkPacket packet) {
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

	private File createTransformedTemplate(File templateFile, File outFolder, File resultFile, File inputFile, File taxonXmlFilePath, String databaseName, int threads) {
		// The XTandem templates retardedly append .xml to the resulting file name
		// We have to chop it off.

		assert (databaseName != null);

		String resultFileName = resultFile.getAbsolutePath();
		String truncatedResultFileName = resultFileName;
		if (resultFileName.endsWith(XML_EXTENSION)) {
			truncatedResultFileName = resultFileName.substring(0, resultFileName.length() - XML_EXTENSION.length());
		}

		if (!taxonXmlFilePath.exists()) {
			throw new MprcException("Could not find the taxonomy.xml file that was specified: " + taxonXmlFilePath);
		}

		Map<Pattern, String> replacements = new ImmutableMap.Builder<Pattern, String>()
				.put(Pattern.compile("__OUTPATH__"), Matcher.quoteReplacement(truncatedResultFileName))
				.put(Pattern.compile("__PATH__"), Matcher.quoteReplacement(inputFile.getAbsolutePath()))
				.put(Pattern.compile("\\$\\{OUTPATH\\}"), Matcher.quoteReplacement(resultFile.getAbsolutePath()))
				.put(Pattern.compile("\\$\\{PATH\\}"), Matcher.quoteReplacement(inputFile.getAbsolutePath()))
				.put(Pattern.compile("\\$\\{TAXONXML\\}"), Matcher.quoteReplacement(taxonXmlFilePath.getAbsolutePath()))
				.put(Pattern.compile("\\$\\{(?:DB|DBPath):[^}]*\\}"), Matcher.quoteReplacement(databaseName))
				.put(Pattern.compile("\\$\\{THREADS\\}"), Matcher.quoteReplacement(String.valueOf(threads)))
				.build();

		StreamRegExMatcher matcher = null;

		try {
			matcher = new StreamRegExMatcher(templateFile);
			matcher.replaceAll(replacements);
			File outFile = new File(outFolder, templateFile.getName() + '.' + System.currentTimeMillis());
			matcher.writeContentsToFile(outFile);
			return outFile;
		} catch (IOException e) {
			throw new MprcException("Could not read the tandem template or write it perhaps couldn't write it out with substitutions.", e);
		} finally {
			if (matcher != null) {
				matcher.close();
			}
		}
	}

	private void cleanUp(WorkPacket workPacket) {
		XTandemWorkPacket packet = (XTandemWorkPacket) workPacket;
		File outputFolder = packet.getWorkFolder();
		FileUtilities.restoreUmaskRights(outputFolder, true);
	}

	private static final String XML_EXTENSION = ".xml";

	/**
	 * @return How many threads can X!Tandem utilize on this computer.
	 */
	public int getNumThreads() {
		return Math.max(1, Runtime.getRuntime().availableProcessors());
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		@Override
		public Worker create(Config config, DependencyResolver dependencies) {
			XTandemWorker worker = null;
			try {
				worker = new XTandemWorker(FileUtilities.getAbsoluteFileForExecutables(new File(config.getTandemExecutable())));
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
		private String tandemExecutable;

		public Config() {
		}

		public Config(String tandemExecutable) {
			this.tandemExecutable = tandemExecutable;
		}

		public String getTandemExecutable() {
			return tandemExecutable;
		}

		public void setTandemExecutable(String tandemExecutable) {
			this.tandemExecutable = tandemExecutable;
		}

		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put(TANDEM_EXECUTABLE, tandemExecutable);
			return map;
		}

		public void load(Map<String, String> values, DependencyResolver resolver) {
			tandemExecutable = values.get(TANDEM_EXECUTABLE);
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		private static final String WIN32 = "bin/tandem/win32_tandem/tandem.exe";
		private static final String WIN64 = "bin/tandem/win64_tandem/tandem.exe";
		private static final String LINUX_32 = "bin/tandem/linux_redhat_tandem/tandem.exe";
		private static final String LINUX_64 = "bin/tandem/ubuntu_64bit_tandem/tandem.exe";
		private static final String MAC_OSX = "bin/tandem/osx_intel_tandem/tandem.exe";

		public void createUI(final DaemonConfig daemon, final ResourceConfig resource, UiBuilder builder) {
			builder.property(TANDEM_EXECUTABLE, "Executable Path", "Tandem executable path. Tandem executables can be " +
					"<br/>found at <a href=\"ftp://ftp.thegpm.org/projects/tandem/binaries/\"/>ftp://ftp.thegpm.org/projects/tandem/binaries</a>"
					+ "<p>Swift install contains following executables for your convenience:</p>"
					+ "<table>"
					+ "<tr><td><tt>" + WIN32 + "</tt></td><td>Windows 32 bit</td></tr>"
					+ "<tr><td><tt>bin/tandem/win32_core2_tandem/tandem.exe</tt></td><td>Windows 32 bit, specialized for Core2 processors</td></tr>"
					+ "<tr><td><tt>" + WIN64 + "</tt></td><td>Windows 64 bit</td></tr>"
					+ "<tr><td><tt>bin/tandem/win64_core2_tandem/tandem.exe</tt></td><td>Windows 64 bit, specialized for Core2 processors</td></tr>"
					+ "<tr><td><tt>" + LINUX_32 + "</tt></td><td>RedHat Linux</td></tr>"
					+ "<tr><td><tt>" + MAC_OSX + "</tt></td><td>Mac OS 10 on intel processor</td></tr>"
					+ "<tr><td><tt>" + LINUX_64 + "</tt></td><td>Ubuntu Linux, 64 bit</td></tr>"
					+ "</table>")
					.required()
					.executable(Arrays.asList("-v"))
					.defaultValue(getDefaultExecutable(daemon))
					.addDaemonChangeListener(new ExecutableChanger(resource, daemon));
		}

		private static final class ExecutableChanger implements PropertyChangeListener {
			private final ResourceConfig resource;
			private final DaemonConfig daemon;

			public ExecutableChanger(ResourceConfig resource, DaemonConfig daemon) {
				this.resource = resource;
				this.daemon = daemon;
			}

			@Override
			public void propertyChanged(ResourceConfig config, String propertyName, String newValue, UiResponse response, boolean validationRequested) {
				response.setProperty(resource, TANDEM_EXECUTABLE, getDefaultExecutable(daemon));
			}

			@Override
			public void fixError(ResourceConfig config, String propertyName, String action) {
				// We never report an error, nothing to fix.
			}
		}

		private static String getDefaultExecutable(DaemonConfig daemon) {
			final String osArch = daemon.getOsArch() == null ? "" : daemon.getOsArch().toLowerCase(Locale.ENGLISH);
			if (daemon.isWindows()) {
				if (osArch.contains("64")) {
					return WIN64;
				} else {
					return WIN32;
				}
			} else if (daemon.isLinux()) {
				if (osArch.contains("64")) {
					return LINUX_64;
				} else {
					return LINUX_32;
				}
			} else if (daemon.isMac()) {
				return MAC_OSX;
			}
			return "";
		}
	}
}
