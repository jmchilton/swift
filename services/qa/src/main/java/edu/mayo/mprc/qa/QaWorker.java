package edu.mayo.mprc.qa;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.daemon.progress.PercentDone;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.msmseval.MSMSEvalOutputReader;
import edu.mayo.mprc.myrimatch.MyrimatchPepXmlReader;
import edu.mayo.mprc.scaffoldparser.spectra.ScaffoldSpectraReader;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ProcessCaller;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Generates data files and image files representing QA data.
 */
public final class QaWorker implements Worker {

	private static final Logger LOGGER = Logger.getLogger(QaWorker.class);
	public static final String TYPE = "qa";
	public static final String NAME = "Quality Assurance";
	// How many output files we produce per .mgf
	private static final int INITIAL_OUTPUT_FILES = 10;
	// Generating input files for the R script is considered 50% of all work(R script takes another 50%)
	private static final float PERCENT_GENERATING_FILES = 50.0f;
	// 100% - work complete
	private static final float COMPLETE = 100.0f;
	public static final String DESC = "Generates statistical information for analysis of the data adquisition process and the data search process.";

	private String rExecutable;
	private File rScript;
	private File xvfbWrapperScript;

	private static final String XVFB_WRAPPER_SCRIPT = "xvfbWrapperScript";
	private static final String R_SCRIPT = "rScript";
	private static final String R_EXECUTABLE = "rExecutable";

	@Override
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

	private void process(WorkPacket workPacket, ProgressReporter progressReporter) {
		QaWorkPacket qaWorkPacket = (QaWorkPacket) workPacket;

		File reportFile = qaWorkPacket.getReportFile();
		File qaReportFolder = qaWorkPacket.getQaReportFolder();
		FileUtilities.ensureFolderExists(qaReportFolder);

		File rScriptInputFile = new File(qaReportFolder, "rInputData.tsv");

		FileWriter fileWriter = null;

		LinkedList<File> generatedFileList = new LinkedList<File>();

		boolean atLeastOneFileMissing = !reportFile.exists();
		boolean writerIsClosed = false;

		try {
			fileWriter = new FileWriter(rScriptInputFile);

			fileWriter.write("Data File\tId File\tMz File\tIdVsMz File\tSource Current File\tmsmsEval Discriminant File\tGenerate Files\tRaw File\tmsmsEval Output\tRaw Info File\tRaw Spectra File\tPeptide Tolerance File\tTIC File\tChromatogram File");
			fileWriter.write("\n");

			final List<ExperimentQa> experimentQas = qaWorkPacket.getExperimentQas();

			int numFilesDone = 0;
			int numFilesTotal = countTotalFiles(experimentQas);

			for (ExperimentQa experimentQa : experimentQas) {
				final List<MgfQaFiles> entries = experimentQa.getMgfQaFiles();
				for (MgfQaFiles me : entries) {
					atLeastOneFileMissing = addRScriptInputLine(fileWriter, qaReportFolder, experimentQa, generatedFileList, me, atLeastOneFileMissing);
					numFilesDone++;
					reportProgress(numFilesDone * PERCENT_GENERATING_FILES / numFilesTotal, progressReporter);
				}
			}

			FileUtilities.closeObjectQuietly(fileWriter);
			writerIsClosed = true;

			if (atLeastOneFileMissing) {
				LOGGER.info("Running R script [" + getRScript().getAbsolutePath() + "] for output file [" + reportFile + "]");
				runRScript(rScriptInputFile, reportFile);
			}

			for (File file : generatedFileList) {
				if (!file.exists()) {
					throw new MprcException("Some of the output files for the QA have not been created, example: [" + file.getAbsolutePath() + "]");
				}
			}

			reportProgress(COMPLETE, progressReporter);
		} catch (Exception e) {
			throw new MprcException("Processing of QA work packet failed.", e);
		} finally {
			if (!writerIsClosed) {
				FileUtilities.closeObjectQuietly(fileWriter);
			}
		}
	}

	private boolean addRScriptInputLine(FileWriter fileWriter, File qaReportFolder, ExperimentQa experimentQa, LinkedList<File> generatedFiles, MgfQaFiles qaFiles, boolean atLeastOneFileMissing) throws IOException {
		String uniqueMgfAnalysisName;
		boolean generate;
		File msmsEvalDiscriminantFile;
		File ticFile;
		File mgfFile = qaFiles.getMgfFile();

		// The name of the analysis output file is the original .mgf name combined with scaffold version to make it unique
		uniqueMgfAnalysisName = FileUtilities.getFileNameWithoutExtension(mgfFile) + "." +
				(experimentQa.getScaffoldVersion().startsWith("2") ? "sfd" : "sf3");

		generate = false;
		List<File> rScriptOutputFilesSet = new ArrayList<File>(INITIAL_OUTPUT_FILES);

		File outputFile = new File(qaReportFolder, uniqueMgfAnalysisName + ".sfs");

		File massCalibrationRtFile = new File(qaReportFolder, uniqueMgfAnalysisName + ".calRt.png");
		File massCalibrationMzFile = new File(qaReportFolder, uniqueMgfAnalysisName + ".calMz.png");
		File mzRtFile = new File(qaReportFolder, uniqueMgfAnalysisName + ".mzRt.png");
		File sourceCurrentFile = new File(qaReportFolder, uniqueMgfAnalysisName + ".current.png");
		File pepTolFile = new File(qaReportFolder, uniqueMgfAnalysisName + ".pepTol.png");

		rScriptOutputFilesSet.add(massCalibrationRtFile);
		rScriptOutputFilesSet.add(massCalibrationMzFile);
		rScriptOutputFilesSet.add(mzRtFile);
		rScriptOutputFilesSet.add(sourceCurrentFile);
		rScriptOutputFilesSet.add(pepTolFile);

		//Do not add msmsEval file to list before checking if msmsEval is enabled.
		msmsEvalDiscriminantFile = new File(qaReportFolder, uniqueMgfAnalysisName + ".msmsEval.png");

		// Do not add anything that depends on RawDump unless we are sure we can provide the data
		ticFile = new File(qaReportFolder, uniqueMgfAnalysisName + ".tic.png");

		MyrimatchPepXmlReader myrimatchReader = getMyrimatchReader(qaFiles.getAdditionalSearchResults());

		if (!outputFile.exists() || outputFile.length() == 0) {

			LOGGER.info("Generating output file [" + outputFile.getAbsolutePath() + "]");

			ScaffoldSpectraReader scaffoldParser = new ScaffoldSpectraReader(experimentQa.getScaffoldSpectraFile(), experimentQa.getScaffoldVersion());
			RawDumpReader rawDumpReader = new RawDumpReader(qaFiles.getRawSpectraFile());
			MSMSEvalOutputReader msmsEvalReader = new MSMSEvalOutputReader(qaFiles.getMsmsEvalOutputFile());
			final String rawInputFile = qaFiles.getRawInputFile() != null ? qaFiles.getRawInputFile().getAbsolutePath() : null;
			generate = SpectrumInfoJoiner.joinSpectrumData(
					mgfFile,
					scaffoldParser,
					rawDumpReader,
					msmsEvalReader,
					myrimatchReader,
					outputFile,
					rawInputFile) > 0;

			if (generate) {
				atLeastOneFileMissing = true;
			}
		} else {
			LOGGER.info("Skipping creation of output file [" + outputFile.getAbsolutePath() + "] because already exists.");

			//Check msmsEval files.
			if (qaFiles.getMsmsEvalOutputFile() != null && (!msmsEvalDiscriminantFile.exists() || msmsEvalDiscriminantFile.length() == 0)) {
				atLeastOneFileMissing = true;
				generate = true;
			}

			if (!generate) {
				for (File file : rScriptOutputFilesSet) {
					if (file.exists() || file.length() == 0) {
						atLeastOneFileMissing = true;
						generate = true;
						break;
					}
				}
			}
		}

		if (generate) {
			//If msmsEval is enabled, add it to the output file list.
			if (qaFiles.getMsmsEvalOutputFile() != null) {
				rScriptOutputFilesSet.add(msmsEvalDiscriminantFile);
			}

			// TIC file and others need rawDump output
			if (qaFiles.getRawInfoFile() != null && qaFiles.getRawSpectraFile() != null) {
				rScriptOutputFilesSet.add(ticFile);
			}

			generatedFiles.addAll(rScriptOutputFilesSet);
		}

		final File chromatogramFile = qaFiles.getChromatogramFile();
		writeInputLine(fileWriter, outputFile, massCalibrationRtFile, massCalibrationMzFile, mzRtFile, sourceCurrentFile, msmsEvalDiscriminantFile, generate, qaFiles, pepTolFile, ticFile, chromatogramFile);
		return atLeastOneFileMissing;
	}

	/**
	 * Find a myrimatch search engine results in the list and create a reader from them.
	 */
	private MyrimatchPepXmlReader getMyrimatchReader(HashMap<String, File> additionalSearchResults) {
		for (Map.Entry<String, File> entry : additionalSearchResults.entrySet()) {
			final String searchEngineCode = entry.getKey();
			if ("MYRIMATCH".equals(searchEngineCode)) {
				final MyrimatchPepXmlReader reader = new MyrimatchPepXmlReader();
				final File searchResult = entry.getValue();
				reader.load(FileUtilities.getInputStream(searchResult));
				return reader;
			}
		}
		return null;
	}

	private void writeInputLine(FileWriter fileWriter, File outputFile, File idVsPpmFile, File mzVsPpmFile, File idVsMzFile, File sourceCurrentFile, File msmsEvalDiscriminantFile, boolean generate, MgfQaFiles qaFiles, File pepTolFile, File ticFile, File chromatogramFile) throws IOException {
		fileWriter.write(outputFile.getAbsolutePath());
		fileWriter.write("\t");
		fileWriter.write(idVsPpmFile.getAbsolutePath());
		fileWriter.write("\t");
		fileWriter.write(mzVsPpmFile.getAbsolutePath());
		fileWriter.write("\t");
		fileWriter.write(idVsMzFile.getAbsolutePath());
		fileWriter.write("\t");
		fileWriter.write(sourceCurrentFile.getAbsolutePath());
		fileWriter.write("\t");
		fileWriter.write(msmsEvalDiscriminantFile.getAbsolutePath());
		fileWriter.write("\t");
		fileWriter.write(Boolean.toString(generate));
		fileWriter.write("\t");
		fileWriter.write(isDataFileValid(qaFiles.getRawInputFile()) ? qaFiles.getRawInputFile().getAbsolutePath() : "");
		fileWriter.write("\t");
		fileWriter.write(isDataFileValid(qaFiles.getMsmsEvalOutputFile()) ? qaFiles.getMsmsEvalOutputFile().getAbsolutePath() : "");
		fileWriter.write("\t");
		fileWriter.write(isDataFileValid(qaFiles.getRawInfoFile()) ? qaFiles.getRawInfoFile().getAbsolutePath() : "");
		fileWriter.write("\t");
		fileWriter.write(isDataFileValid(qaFiles.getRawSpectraFile()) ? qaFiles.getRawSpectraFile().getAbsolutePath() : "");
		fileWriter.write("\t");
		fileWriter.write(pepTolFile.getAbsolutePath());
		fileWriter.write("\t");
		fileWriter.write((isDataFileValid(qaFiles.getRawInfoFile()) && isDataFileValid(qaFiles.getRawSpectraFile())) ? ticFile.getAbsolutePath() : "");
		fileWriter.write("\t");
		fileWriter.write(chromatogramFile != null ? chromatogramFile.getAbsolutePath() : "");
		fileWriter.write("\n");
	}

	private int countTotalFiles(List<ExperimentQa> experimentQas) {
		int numFilesTotal = 0;
		for (ExperimentQa experimentQa : experimentQas) {
			numFilesTotal += experimentQa.getMgfQaFiles().size();
		}
		return numFilesTotal;
	}

	private void reportProgress(float percentDone, ProgressReporter progressReporter) {
		progressReporter.reportProgress(new PercentDone(percentDone));
	}

	/**
	 * @param file
	 * @return true if file exists and is not empty.
	 */
	private boolean isDataFileValid(File file) {
		return file != null && file.exists() && file.length() > 0;
	}

	private void runRScript(File inputFile, File reportFile) {

		List<String> result = new ArrayList<String>();

		if (getXvfbWrapperScript() != null && FileUtilities.isLinuxPlatform()) {
			result.add(getXvfbWrapperScript().getAbsolutePath());
		}

		result.add(getRExecutable());
		result.add(getRScript().getAbsolutePath());
		result.add(inputFile.getAbsolutePath());
		result.add(reportFile.getAbsolutePath());

		ProcessBuilder builder = new ProcessBuilder(result.toArray(new String[result.size()]));

		ProcessCaller caller = new ProcessCaller(builder);

		try {
			caller.run();
		} catch (Exception t) {
			throw new MprcException("Qa R script call failed: " + caller.getFailedCallDescription(), t);
		}

		LOGGER.debug("R script call returned " + caller.getExitValue());

		if (caller.getExitValue() != 0) {
			throw new MprcException("Qa R script call failed: " + caller.getFailedCallDescription());
		}
	}

	public String getRExecutable() {
		return rExecutable;
	}

	public void setRExecutable(String rExecutable) {
		this.rExecutable = rExecutable;
	}

	public File getRScript() {
		return rScript;
	}

	public void setRScript(File rScript) {
		this.rScript = rScript;
	}

	public File getXvfbWrapperScript() {
		return xvfbWrapperScript;
	}

	public void setXvfbWrapperScript(File xvfbWrapperScript) {
		this.xvfbWrapperScript = xvfbWrapperScript;
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		@Override
		public Worker create(Config config, DependencyResolver dependencies) {
			QaWorker qaWorker = new QaWorker();
			qaWorker.setRExecutable(config.getRExecutable());
			qaWorker.setRScript(new File(config.getRScript()));
			qaWorker.setXvfbWrapperScript(config.getXvfbWrapperScript() != null && config.getXvfbWrapperScript().length() > 0 ? new File(config.getXvfbWrapperScript()) : null);
			return qaWorker;
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {

		private String xvfbWrapperScript;
		private String rScript;
		private String rExecutable;

		public Config() {
		}

		public Config(String xvfbWrapperScript, String rScript) {
			this.xvfbWrapperScript = xvfbWrapperScript;
			this.rScript = rScript;
		}

		public String getXvfbWrapperScript() {
			return xvfbWrapperScript;
		}

		public void setXvfbWrapperScript(String xvfbWrapperScript) {
			this.xvfbWrapperScript = xvfbWrapperScript;
		}

		public String getRScript() {
			return rScript;
		}

		public void setRScript(String rScript) {
			this.rScript = rScript;
		}

		public String getRExecutable() {
			return rExecutable;
		}

		public void setRExecutable(String rExecutable) {
			this.rExecutable = rExecutable;
		}

		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put(XVFB_WRAPPER_SCRIPT, xvfbWrapperScript);
			map.put(R_SCRIPT, rScript);
			map.put(R_EXECUTABLE, rExecutable);
			return map;
		}

		public void load(Map<String, String> values, DependencyResolver resolver) {
			xvfbWrapperScript = values.get(XVFB_WRAPPER_SCRIPT);
			rScript = values.get(R_SCRIPT);
			rExecutable = values.get(R_EXECUTABLE);
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		private static final String PROVIDED_R_SCRIPT = "bin/util/rPpmPlot.r";
		private static final String R_EXECUTABLE_DEFAULT = "Rscript";

		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder
					.property(R_EXECUTABLE, "<tt>R executable</tt>", "R script executable or interpreter that runs the given R script below. R must be installed in the system. " +
							"R installation packages can be found at <a href=\"http://www.r-project.org\"/>http://www.r-project.org</a>")
					.required()
					.defaultValue(R_EXECUTABLE_DEFAULT)

					.property(R_SCRIPT, "<tt>R script</tt> path", "R script that generates ppm analysis plots. " +
							"<p>For your convenience, a copy is in <tt>" + PROVIDED_R_SCRIPT + "</tt></p>")
					.required()
					.defaultValue(PROVIDED_R_SCRIPT)

					.property(XVFB_WRAPPER_SCRIPT, "X Window Wrapper Script",
							"<p>This is needed only for Linux. On Windows, leave this field blank.</p>"
									+ "<p>This wrapper script makes sure there is X window system set up and ready to be used by the <tt>R script</tt> (see above).</p>"
									+ "<p>We provide a script <tt>" + DaemonConfig.XVFB_CMD + "</tt> that does just that - feel free to modify it to suit your needs. "
									+ " The script uses <tt>Xvfb</tt> - X virtual frame buffer, so <tt>Xvfb</tt>"
									+ " has to be functional on the host system.</p>"
									+ "<p>If you do not require this functionality, leave the field blank.</p>")
					.executable(Arrays.asList("-v"))
					.defaultValue(daemon.getXvfbWrapperScript());
		}
	}
}
