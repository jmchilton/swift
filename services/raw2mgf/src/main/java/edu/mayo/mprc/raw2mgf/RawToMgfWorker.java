package edu.mayo.mprc.raw2mgf;

import com.google.common.base.Splitter;
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
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static java.util.Arrays.sort;

/**
 * When running extract_msn, we must not extract more than about 10000 spectra at a time
 * (the Windows filesystem fails to have more than 65535 directory entries, long file names can break this
 * limit easily).
 * <p/>
 * We filter out the -F and -L parameters out in {@link #cleanupFromToParams} (they specify first and last spectrum) and we substitute our
 * own limits, running extract_msn in multiple passes. We combine the resulting spectra into a .mgf file.
 */
public final class RawToMgfWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(RawToMgfWorker.class);
	public static final String TYPE = "raw2mgf";
	public static final String NAME = ".RAW to .mgf Conversion";
	public static final String DESC = "<p>Converts Thermo's .RAW files to Mascot Generic Format (.mgf). Without this module, Swift cannot process <tt>.RAW</tt> files.</p><p>We are using <tt>extract_msn</tt> from XCalibur, which is a windows program. On Linux we execute extract_msn using <tt>wine</tt>, which has to be installed prior to using the convertor.</p>";
	private File tempFolder = new File(".");
	private File extractMsnExecutable;
	private String wrapperScript;
	private File xvfbWrapperScript;
	private int spectrumBatchSize = 8000;

	public static final String TEMP_FOLDER = "tempFolder";
	public static final String EXTRACT_MSN_EXECUTABLE = "extractMsnExecutable";
	private static final String WRAPPER_SCRIPT = "wrapperScript";
	private static final String XVFB_WRAPPER_SCRIPT = "xvfbWrapperScript";

	private static final int MAX_RAW_PATH_LENGTH = 100;

	private static final Pattern FIRST_LAST_SPECTRUM = Pattern.compile("^-[FfLl]\\d*$");

	public static File[] getDtaFiles(File dtaFolder) {
		if (!dtaFolder.isDirectory()) {
			throw new MprcException("Dat file location is not a directory: " + dtaFolder.getAbsolutePath());
		}

		File[] files = dtaFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".dta");
			}
		});
		// sort them to aid unit testing
		sort(files, new DtaComparator());
		return files;
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
		if (!(workPacket instanceof RawToMgfWorkPacket)) {
			throw new DaemonException("Unknown request type: " + workPacket.getClass().getName());
		}

		RawToMgfWorkPacket batchWorkPacket = (RawToMgfWorkPacket) workPacket;

		//	steps are
		//	call the extract_msn utility to convert from raw to dta
		//	call dta to mgf converter to make the mgf file

		File mgfFile = batchWorkPacket.getOutputFile();

		final String origParams = batchWorkPacket.getParams();
		final Long firstSpectrum = getParamValue("-F", origParams);
		final Long lastSpectrum = getParamValue("-L", origParams);
		final String params = cleanupFromToParams(origParams);

		LOGGER.debug("Raw2mgf: starting conversion " + batchWorkPacket.getInputFile() + " -> " + mgfFile + " (params: " + params + ")");

		File rawFile = getRawFile(batchWorkPacket);

		//  check if already exists (skip condition)
		if (isConversionDone(batchWorkPacket, rawFile)) {
			return;
		}

		// will use the temporary folder to get dta files then make mgf file in the
		// output_dir
		FileUtilities.ensureFolderExists(tempFolder);
		File fulltempfolder = getMirrorFolderonTemp(tempFolder);
		FileUtilities.ensureFolderExists(fulltempfolder);

		boolean temporaryLinkMade = false;
		File temporaryRawFile = null;
		if (FileUtilities.isLinuxPlatform() && rawFile.getAbsolutePath().length() > MAX_RAW_PATH_LENGTH) {
			// We are on Linux, therefore most likely running wine
			// Make a temporary link to our original raw file, to shorten the path
			try {
				temporaryRawFile = FileUtilities.shortenFilePath(rawFile);
				LOGGER.debug("Shortening .RAW path (over " + MAX_RAW_PATH_LENGTH + " characters) by linking: " + rawFile.getAbsolutePath() + "->" + rawFile.getAbsolutePath());
				rawFile = temporaryRawFile;
				temporaryLinkMade = true;
			} catch (Exception ignore) {
				// SWALLOWED: If we fail to make a link, we use the original file. But we must NOT delete it afterwards!
				temporaryLinkMade = false;
			}
		}

		long currentSpectrum = firstSpectrum == null ? 1 : firstSpectrum;
		long lastSpectrumInBatch = currentSpectrum + spectrumBatchSize - 1;

		long totalSpectraExtracted = 0;
		try {
			while (currentSpectrum <= lastSpectrumInBatch) {
				// Extract .dta files
				File[] dtaFiles = null;

				File ex_msn_exe = getExtractMsnExecutable();

				FileUtilities.ensureFolderExists(mgfFile.getParentFile());

				runExtractMsnJob(ex_msn_exe, fulltempfolder, params, rawFile, currentSpectrum, lastSpectrumInBatch, wrapperScript, xvfbWrapperScript.getAbsolutePath());

				// Count how many extracted
				dtaFiles = getDtaFiles(fulltempfolder);

				// Terminate if we could not find any .dta anymore
				if (dtaFiles.length == 0) {
					if (totalSpectraExtracted == 0) {
						throw new MprcException("There were no .dta files present in " + rawFile.getAbsolutePath());
					}
					break;
				}

				// Combine .dta to .mgf
				final boolean wine = wrapperScript != null && wrapperScript.length() > 0;
				dtaToMgf(fulltempfolder, dtaFiles, mgfFile, wine && !FileUtilities.isWindowsPlatform());

				// Determine next batch position
				totalSpectraExtracted += dtaFiles.length;
				currentSpectrum = lastSpectrumInBatch + 1;
				lastSpectrumInBatch = currentSpectrum + spectrumBatchSize - 1;
				if (lastSpectrum != null) {
					lastSpectrumInBatch = Math.min(lastSpectrumInBatch, lastSpectrum);
				}
			}
		} catch (Exception we) {
			throw new DaemonException("Error extracting dta files from " + batchWorkPacket.getInputFile(), we);
		} finally {
			if (temporaryLinkMade) {
				FileUtilities.cleanupShortenedPath(temporaryRawFile);
			}
		}
	}

	static Long getParamValue(String paramName, String params) {
		final String lcParamName = paramName.toLowerCase();
		for (String parameter : Splitter.on(" ").omitEmptyStrings().trimResults().split(params)) {
			if (parameter.toLowerCase().startsWith(lcParamName)) {
				String value = parameter.substring(paramName.length()).trim();
				if (value.length() != 0 && value.charAt(0) >= '0' && value.charAt(0) <= '9') {
					try {
						return Long.valueOf(value);
					} catch (NumberFormatException e) {
						// SWALLOWED: We keep going - maybe there is another instance on the command line where the parameter is defined
						LOGGER.warn("Could not parse parameter " + paramName + " - expected numeric value, got " + parameter + ". " + e.getMessage());
					}
				}
			}
		}
		return null;
	}

	/**
	 * Omit all parameters defining the first and last spectrum.
	 *
	 * @param params Parameter list to clean from -F and -L
	 * @return Cleaned up parameter list.
	 */
	static String cleanupFromToParams(String params) {
		StringBuilder result = new StringBuilder(params.length());
		for (String parameter : Splitter.on(" ").omitEmptyStrings().trimResults().split(params)) {
			if (!FIRST_LAST_SPECTRUM.matcher(parameter).matches()) {
				if (result.length() > 0) {
					result.append(' ');
				}
				result.append(parameter);
			}
		}
		return result.toString();
	}

	private boolean isConversionDone(RawToMgfWorkPacket batchWorkPacket, File rawFile) {
		//  check if already exists (skip condition)
		if (batchWorkPacket.isSkipIfExists()) {
			File mgf_file = batchWorkPacket.getOutputFile();
			if (mgf_file.exists()) {
				LOGGER.info(rawFile.getAbsolutePath() + " conversion already done.");
				return true;
			}
		}
		return false;
	}

	private File getRawFile(RawToMgfWorkPacket batchWorkPacket) {
		File rawFile = batchWorkPacket.getInputFile();

		// check that we got real raw file to work with
		checkRawFile(rawFile);

		return rawFile;
	}

	/**
	 * run the extract msn executable
	 *
	 * @param fileToExec      - the executable file
	 * @param thermoOutputDir - output dir where the dta files will be written
	 * @param params          - parameters to run extract_msn with
	 * @param rawfile         - the raw file
	 * @return Number of extracted spectra. If there are genuinely no spectra, returns 0. Throws an exception if things go wrong.
	 */
	synchronized void runExtractMsnJob(File fileToExec, File thermoOutputDir, String params, File rawfile, long firstSpectrum, long lastSpectrum, String wrapperScript, String xvfbWrapperScript) {
		final String spectrumRangeParams = (params.length() == 0 ? "" : params + " ") + "-F" + firstSpectrum + " -L" + lastSpectrum;
		ExtractMsnWrapper extractMsn = new ExtractMsnWrapper(fileToExec, spectrumRangeParams, rawfile, wrapperScript, xvfbWrapperScript);
		extractMsn.setOutputDir(thermoOutputDir);

		try {
			extractMsn.run();
		} catch (Exception we) {
			throw new DaemonException("Error extracting dta files from " + rawfile, we);
		}
	}

	/**
	 * convert dta files to an mgf file
	 *
	 * @param thermoOutputDir - Where .dta files were stored
	 * @param dtaFiles        - the list of dta files to process
	 * @param finalOutputFile - token for mgf output file
	 * @param wine            - if should use wine or not
	 * @throws edu.mayo.mprc.MprcException
	 */
	private void dtaToMgf(File thermoOutputDir, File[] dtaFiles, File finalOutputFile, boolean wine) {
		// now do mgf file creation...
		File finalOutputDir = finalOutputFile.getParentFile();

		DTAToMGFConverter pDTAtoMGF =
				new DTAToMGFConverter(
						dtaFiles,
						finalOutputFile);
		pDTAtoMGF.setWineCleanup(wine);
		try {
			// Run the conversion
			pDTAtoMGF.run();
			// delete the dta files
			deleteDTAFiles(thermoOutputDir);
			// copy remaining files in folder to pBatchInfo.Output_Dir
			copyRemainingFiles(thermoOutputDir, finalOutputDir);
		} catch (Exception t) {
			throw new MprcException("dta to MGF conversion failed", t);
		}
		File file = pDTAtoMGF.getResultFile();
		if (!file.exists() || !file.isFile()) {
			throw new MprcException("The MGF file does not exist " + file.getAbsolutePath());
		}
		if (file.length() == 0) {
			throw new MprcException(
					MessageFormat.format("Merging of DTAs resulted in mgf of zero length (merge {0} into {1})",
							thermoOutputDir, file.getAbsolutePath()));
		}
	}

	private static void copyRemainingFiles(File fromfolder, File tofolder) {
		String[] fromfiles = fromfolder.list();
		for (String fromfile : fromfiles) {
			File to = new File(tofolder, FileUtilities.getLastFolderName(fromfile));
			File localfile = new File(fromfolder, fromfile);
			try {
				FileUtilities.copyFile(localfile, to, true);
			} catch (Exception e) {
				throw new MprcException("Error moving remaining files in batch converter", e);
			}
			FileUtilities.quietDelete(localfile);
		}
		FileUtilities.quietDelete(fromfolder);
	}

	private static File getMirrorFolderonTemp(File tempfolder) {
		try {
			return FileUtilities.createTempFolder(tempfolder, "raw2mgf", false);
		} catch (Exception t) {
			throw new DaemonException("Cannot create temporary folder for raw->mgf conversion", t);
		}
	}

	private void deleteDTAFiles(File dir) {

		ExtensionFilter filter = new ExtensionFilter("dta");

		String[] list = dir.list(filter);
		File file;
		if (list.length == 0) {
			return;
		}

		for (String aList : list) {
			file = new File(dir, aList);
			boolean isdeleted = file.delete();
			if (!isdeleted) {
				LOGGER.warn("Deletion of dta file failed with name=" + dir.getAbsolutePath() + File.separator + aList);
			}
		}
	}

	private static void checkRawFile(File pFile) {
		if (pFile.exists()) {
			if (pFile.isDirectory()) {
				throw new DaemonException("Raw to MGF convertor cannot convert a directory");
			}
		} else {
			throw new DaemonException("The file " + pFile.getAbsolutePath() + " cannot be found.");
		}
	}

	public void setExtractMsnExecutable(File extractMsnExecutable) {
		this.extractMsnExecutable = extractMsnExecutable;
	}

	public File getExtractMsnExecutable() {
		return this.extractMsnExecutable;
	}

	public File getTempFolder() {
		return tempFolder;
	}

	public void setTempFolder(File tempFolder) {
		this.tempFolder = tempFolder;
	}

	public String getWrapperScript() {
		return wrapperScript;
	}

	public void setWrapperScript(String wrapperScript) {
		this.wrapperScript = wrapperScript;
	}

	public File getXvfbWrapperScript() {
		return xvfbWrapperScript;
	}

	public void setXvfbWrapperScript(File xvfbWrapperScript) {
		this.xvfbWrapperScript = xvfbWrapperScript;
	}

	public String toString() {
		return MessageFormat.format("Batch conversion:\n\ttemp={0}\n\textract_msn path={1}\n\twrapper={2}", tempFolder.getPath(), extractMsnExecutable, wrapperScript);
	}

	public void setSpectrumBatchSize(int spectrumBatchSize) {
		this.spectrumBatchSize = spectrumBatchSize;
	}

	public int getSpectrumBatchSize() {
		return spectrumBatchSize;
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		@Override
		public Worker create(Config config, DependencyResolver dependencies) {
			RawToMgfWorker worker = new RawToMgfWorker();
			worker.setTempFolder(new File(config.getTempFolder()));
			worker.setWrapperScript(config.getWrapperScript());
			worker.setXvfbWrapperScript(new File(config.getXvfbWrapperScript()));
			worker.setExtractMsnExecutable(new File(config.getExtractMsnExecutable()));
			return worker;
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {

		private String tempFolder;
		private String wrapperScript;
		private String xvfbWrapperScript;
		private String extractMsnExecutable;

		public Config() {
		}

		public Config(String tempFolder, String wrapperScript, String xvfbWrapperScript, String extractMsnExecutable) {
			this.tempFolder = tempFolder;
			this.wrapperScript = wrapperScript;
			this.xvfbWrapperScript = xvfbWrapperScript;
			this.extractMsnExecutable = extractMsnExecutable;
		}

		public String getTempFolder() {
			return tempFolder;
		}

		public void setTempFolder(String tempFolder) {
			this.tempFolder = tempFolder;
		}

		public String getWrapperScript() {
			return wrapperScript;
		}

		public void setWrapperScript(String wrapperScript) {
			this.wrapperScript = wrapperScript;
		}

		public String getExtractMsnExecutable() {
			return extractMsnExecutable;
		}

		public void setExtractMsnExecutable(String extractMsnExecutable) {
			this.extractMsnExecutable = extractMsnExecutable;
		}

		public String getXvfbWrapperScript() {
			return xvfbWrapperScript;
		}

		public void setXvfbWrapperScript(String xvfbWrapperScript) {
			this.xvfbWrapperScript = xvfbWrapperScript;
		}

		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put(TEMP_FOLDER, tempFolder);
			map.put(WRAPPER_SCRIPT, wrapperScript);
			map.put(XVFB_WRAPPER_SCRIPT, xvfbWrapperScript);
			map.put(EXTRACT_MSN_EXECUTABLE, extractMsnExecutable);
			return map;
		}

		public void load(Map<String, String> values, DependencyResolver resolver) {
			tempFolder = values.get(TEMP_FOLDER);
			wrapperScript = values.get(WRAPPER_SCRIPT);
			xvfbWrapperScript = values.get(XVFB_WRAPPER_SCRIPT);
			extractMsnExecutable = values.get(EXTRACT_MSN_EXECUTABLE);
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		private static final String PROVIDED_EXTRACT_MSN = "bin/extract_msn/extract_msn.exe";

		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder.property(EXTRACT_MSN_EXECUTABLE, "<tt>extract_msn.exe</tt> path", "Location of XCalibur's <tt>extract_msn.exe</tt>."
					+ "<p>Typically installed at <tt>C:\\XCalibur\\System\\Programs\\extract_msn.exe</tt></p>"
					+ "<p>For your convenience, a copy is in <tt>" + PROVIDED_EXTRACT_MSN + "</tt></p>")
					.required()
					.executable(Arrays.asList("-v"))
					.defaultValue(PROVIDED_EXTRACT_MSN)

					.property(TEMP_FOLDER, "Temp folder",
							"extract_msn takes a .RAW file on the input and turns it into " +
									"a lot of <tt>.dta</tt> files (one per spectrum), that are subsequently collected into an <tt>.mgf</tt>. " +
									"The <tt>.dta</tt> files need to be stored to a temporary place that is hopefully very fast. For highest performance," +
									"consider setting up a RAM disk (has to have enough space to contain all .dta files for your largest .RAW files). " +
									"A local temporary folder will work as well. Avoid putting this folder on slow, network drives.")
					.required()
					.existingDirectory()

					.property(WRAPPER_SCRIPT, "Wrapper Script",
							"<p>This is needed only for Linux. On Windows, leave this field blank.</p>" +
									"<p>A wrapper script takes the extract_msn command line as a parameter and executes extract_msn.</p>"
									+ "<p>On linux we suggest using <tt>" + DaemonConfig.WINECONSOLE_CMD + "</tt>. You need to have X Window System installed for <tt>" + DaemonConfig.WINECONSOLE_CMD + "</tt> to work, or use the X virtual frame buffer for headless operation (see below).</p>"
									+ "<p>Alternatively, use <tt>" + DaemonConfig.WINE_CMD + "</tt> without need to run X, but in our experience <tt>" + DaemonConfig.WINE_CMD + "</tt> is less stable.</p>")
					.executable(Arrays.asList("-v"))
					.defaultValue(daemon.getWrapperScript())

					.property(XVFB_WRAPPER_SCRIPT, "X Window Wrapper Script",
							"<p>This is needed only for Linux. On Windows, leave this field blank.</p>"
									+ "<p>This wrapper script makes sure there is X window system set up and ready to be used by <tt>wineconsole</tt> (see above).</p>"
									+ "<p>We provide a script <tt>" + DaemonConfig.XVFB_CMD + "</tt> that does just that - feel free to modify it to suit your needs. "
									+ " The script uses <tt>Xvfb</tt> - X virtual frame buffer, so <tt>Xvfb</tt>"
									+ " has to be functional on the host system.</p>"
									+ "<p>If you do not require this functionality, leave the field blank.</p>")
					.executable(Arrays.asList("-v"))
					.defaultValue(daemon.getXvfbWrapperScript())
					.addDaemonChangeListener(new WrapperScriptSwitcher(resource, daemon, WRAPPER_SCRIPT));
		}
	}

}
