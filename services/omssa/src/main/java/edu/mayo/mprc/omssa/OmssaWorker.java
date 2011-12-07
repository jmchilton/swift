package edu.mayo.mprc.omssa;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ExecutableSwitching;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.GZipUtilities;
import edu.mayo.mprc.utilities.ProcessCaller;
import edu.mayo.mprc.utilities.StreamRegExMatcher;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a worker that can run omssa.  It will execute on an injected OMSSA configuration.  Each OmssaWorkPacket will
 * contain a params file we should execute as well as the mgf, fasta, and outputfile paths which will sbe inserted into the
 * params file at ${MGF_PATH}, ${DB[_PATH]:...}, and ${OUT_PATH}.  These placeholder MUST be in place in the correct positions
 * for a successful search to complete.
 */
public final class OmssaWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(OmssaWorker.class);
	public static final String TYPE = "omssa";
	public static final String NAME = "Omssa";
	public static final String DESC = "OMSSA search engine support. <p>OMSSA is freely available at <a href=\"http://pubchem.ncbi.nlm.nih.gov/omssa/\">http://pubchem.ncbi.nlm.nih.gov/omssa/</a>, so we include the binaries directly in Swift install.</p>";

	private File omssaclPath;
	private OmssaUserModsWriter omssaUserModsWriter;

	public static final String OMSSACL_PATH = "omssaclPath";

	public OmssaWorker(OmssaUserModsWriter omssaUserModsWriter) {
		this.omssaUserModsWriter = omssaUserModsWriter;
	}

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
		OmssaWorkPacket omssaWorkPacket = (OmssaWorkPacket) workPacket;
		LOGGER.info("Starting OMSSA search: " + omssaWorkPacket.toString());

		omssaWorkPacket.waitForInputFiles();

		final File outputFile = omssaWorkPacket.getOutputFile();
		File modOutputFile = new File(outputFile.getParentFile(), outputFile.getName() + ".org");
		File tempFolder = null;

		try {
			tempFolder = FileUtilities.createTempFolder(new File(modOutputFile.getParent()), "temp", false);
		} catch (Exception t) {
			throw new MprcException(t);
		}
		//the params file to search against
		final File completeParamsFile = finishParamsFile(tempFolder.getAbsolutePath(), omssaWorkPacket, modOutputFile);
		final File userModsFile = finishUserModsFile(tempFolder, completeParamsFile);

		final List<String> commandLine = new ArrayList<String>();

		commandLine.add(this.omssaclPath.getAbsolutePath());
		commandLine.add("-pm");
		commandLine.add(completeParamsFile.getAbsolutePath());
		commandLine.add("-mux");
		commandLine.add(userModsFile.getAbsolutePath());

		final ProcessBuilder procBuilder = new ProcessBuilder();
		procBuilder.directory(omssaWorkPacket.getSearchParamsFile().getParentFile());
		procBuilder.command(commandLine);

		final ProcessCaller caller = new ProcessCaller(procBuilder);

		caller.run();

		LOGGER.debug("OMSSA finished with exit value " + String.valueOf(caller.getExitValue()));
		if (caller.getExitValue() != 0) {
			throw new DaemonException("OMSSA finished with nonzero exit value. Call was: " + caller.getFailedCallDescription());
		}

		// gzip the output file
		try {
			if (outputFile.exists()) {
				FileUtilities.quietDelete(outputFile);
			}

			GZipUtilities.compressFile(modOutputFile, outputFile);
			// and then removed the temporary uncompressed file
			FileUtilities.quietDelete(modOutputFile);

			FileUtilities.restoreUmaskRights(outputFile.getParentFile(), true);

			FileUtilities.quietDelete(userModsFile);
			FileUtilities.quietDelete(completeParamsFile);

		} catch (Exception t) {
			throw new MprcException(t);
		}
	}

	/**
	 * Takes the params file that is given is substitutes in machine dependant paths and other options into placeholders.
	 *
	 * @param workPacket
	 * @return the complete params file that should be used for searching.
	 */
	private File finishParamsFile(String folderPath, OmssaWorkPacket workPacket, File outputFile) {
		final File rawParamsFile = workPacket.getSearchParamsFile();
		final File mgfFile = workPacket.getInputFile();
		final File databaseFile = workPacket.getDatabaseFile();


		final Map<Pattern, String> replacements = new HashMap<Pattern, String>();

		// Do not forget to quote the replacements, they go into replaceAll method
		replacements.put(Pattern.compile("\\$\\{MGF_PATH\\}"), Matcher.quoteReplacement(mgfFile.getAbsolutePath()));
		replacements.put(Pattern.compile("\\$\\{(?:DB|DBPath):[^}]*\\}"), Matcher.quoteReplacement(databaseFile.getAbsolutePath()));
		replacements.put(Pattern.compile("\\$\\{OUTPUT_PATH\\}"), Matcher.quoteReplacement(outputFile.getAbsolutePath()));

		StreamRegExMatcher matcher = null;
		try {
			matcher = new StreamRegExMatcher(rawParamsFile);
			matcher.replaceAll(replacements);
			File outFile = new File(new File(folderPath), rawParamsFile.getName() + ".final." + System.currentTimeMillis());
			matcher.writeContentsToFile(outFile);
			return outFile;
		} catch (IOException e) {
			throw new MprcException("Could not read the raw params file or write it perhaps couldn't write it out with substitutions.", e);
		} finally {
			if (matcher != null) {
				matcher.close();
			}
		}
	}

	/**
	 * copy the user mods file to a temporary folder and then modify it
	 */
	private File finishUserModsFile(File folder, File omssaParamsFile) {
		File finalUserModsFile = null;
		try {
			finalUserModsFile = File.createTempFile("usermod", "xml", folder);
		} catch (IOException e) {
			throw new MprcException("Could not create temporary usermod.xml file for OMSSA in " + folder.getAbsolutePath(), e);
		}
		omssaUserModsWriter.generateRuntimeUserModsFile(finalUserModsFile, omssaParamsFile);
		return finalUserModsFile;
	}

	protected void validateConfiguration() {
		if (omssaclPath == null) {
			throw new DaemonException("Daemon not configured properly.  Missing omssacl property, the path to the omssacl command.");
		}
		if (!omssaclPath.exists()) {
			throw new DaemonException("Daemon not configured properly.  The specified path to the omssacl command does not exist at " + omssaclPath.getAbsolutePath());
		}
	}

	public void setOmssaclPath(File omssaclPath) {
		this.omssaclPath = omssaclPath;
	}

	public OmssaUserModsWriter getOmssaUserModsWriter() {
		return omssaUserModsWriter;
	}

	public void setOmssaUserModsWriter(OmssaUserModsWriter omssaUserModsWriter) {
		this.omssaUserModsWriter = omssaUserModsWriter;
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		private OmssaUserModsWriter omssaUserModsWriter;

		@Override
		public Worker create(Config config, DependencyResolver dependencies) {
			OmssaWorker worker = new OmssaWorker(getOmssaUserModsWriter());
			worker.setOmssaclPath(new File(config.getOmssaclPath()));
			worker.validateConfiguration();
			return worker;
		}

		public void setOmssaUserModsWriter(OmssaUserModsWriter omssaUserModsWriter) {
			this.omssaUserModsWriter = omssaUserModsWriter;
		}

		public OmssaUserModsWriter getOmssaUserModsWriter() {
			return omssaUserModsWriter;
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {
		private String omssaclPath;

		public Config() {
		}

		public Config(String omssaclPath) {
			this.omssaclPath = omssaclPath;
		}

		public String getOmssaclPath() {
			return omssaclPath;
		}

		public void setOmssaclPath(String omssaclPath) {
			this.omssaclPath = omssaclPath;
		}

		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put(OMSSACL_PATH, omssaclPath);
			return map;
		}

		public void load(Map<String, String> values, DependencyResolver resolver) {
			omssaclPath = values.get(OMSSACL_PATH);
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		private static final String WINDOWS = "bin/omssa/windows/omssacl.exe";
		private static final String LINUX = "bin/omssa/linux/omssacl";

		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder.property(OMSSACL_PATH, "CL", "Omssa command line executable." +
					"<p>Swift install contains following executables for your convenience:</p>"
					+ "<table>"
					+ "<tr><td><tt>" + WINDOWS + "</tt></td><td>Windows</td></tr>"
					+ "<tr><td><tt>" + LINUX + "</tt></td><td>Linux</td></tr>"
					+ "</table>"
					+ "<br/>Executable can be downloaded from <a href=\"http://pubchem.ncbi.nlm.nih.gov/omssa/download.htm\"/>http://pubchem.ncbi.nlm.nih.gov/omssa/download.htm</a>")
					.required()
					.executable(Arrays.asList("-version"))
					.addChangeListener(new ExecutableSwitching(resource, OMSSACL_PATH, WINDOWS, LINUX));
		}
	}
}
