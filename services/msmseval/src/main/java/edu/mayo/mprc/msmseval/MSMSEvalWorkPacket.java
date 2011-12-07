package edu.mayo.mprc.msmseval;

import edu.mayo.mprc.daemon.CachableWorkPacket;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.WorkPacketBase;
import edu.mayo.mprc.daemon.progress.ProgressReporter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class MSMSEvalWorkPacket extends WorkPacketBase implements CachableWorkPacket {
	private static final long serialVersionUID = 20090402L;

	private File sourceMGFFile;
	private File msmsEvalParamFile;
	private File outputDirectory;

	/**
	 * The implemntation of this constructor calls constructor:
	 * MSMSEvalWorkPacket(File sourceMGFFile, File msmsEvalParamFile, File outputDirectory, String taskId, boolean skipIfAlreadyExecuted)
	 * and sets the skipIfAlreadyExecuted flag to a default of true.
	 *
	 * @param sourceMGFFile
	 * @param msmsEvalParamFile
	 * @param outputDirectory
	 * @param taskId
	 */
	public MSMSEvalWorkPacket(File sourceMGFFile, File msmsEvalParamFile, File outputDirectory, String taskId) {
		this(sourceMGFFile, msmsEvalParamFile, outputDirectory, taskId, false);
	}

	public MSMSEvalWorkPacket(File sourceMGFFile, File msmsEvalParamFile, File outputDirectory, String taskId, boolean fromScratch) {
		super(taskId, fromScratch);

		this.sourceMGFFile = sourceMGFFile;
		this.outputDirectory = outputDirectory;
		this.msmsEvalParamFile = msmsEvalParamFile;
	}

	public File getSourceMGFFile() {
		return sourceMGFFile;
	}

	public File getMsmsEvalParamFile() {
		return msmsEvalParamFile;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	@Override
	public void synchronizeFileTokensOnReceiver() {
		uploadAndWait("outputDirectory");
	}

	@Override
	public boolean isPublishResultFiles() {
		// We never publish msmsEval results to the end user - they are fine in the cache folder
		return false;
	}

	@Override
	public File getOutputFile() {
		// We do not need to provide output file as we never publish the result
		return null;
	}

	@Override
	public String getStringDescriptionOfTask() {
		StringBuilder description = new StringBuilder();
		description
				.append("Input:")
				.append(getSourceMGFFile().getAbsolutePath())
				.append('\n')
				.append("ParamFile:")
				.append(getMsmsEvalParamFile().getAbsolutePath())
				.append('\n');
		return description.toString();
	}

	@Override
	public WorkPacket translateToWorkInProgressPacket(File wipFolder) {
		return new MSMSEvalWorkPacket(
				getSourceMGFFile(),
				getMsmsEvalParamFile(),
				wipFolder,
				getTaskId()
		);
	}

	@Override
	public List<String> getOutputFiles() {
		return Arrays.asList(
				MSMSEvalWorker.getExpectedResultFileName(getSourceMGFFile(), new File(".")).getName(),
				MSMSEvalWorker.getExpectedEmOutputFileName(getSourceMGFFile(), new File(".")).getName()
		);
	}

	@Override
	public boolean cacheIsStale(File subFolder, List<String> outputFiles) {
		final long inputFileModified = getSourceMGFFile().lastModified();
		return inputFileModified > new File(subFolder, outputFiles.get(0)).lastModified() ||
				inputFileModified > new File(subFolder, outputFiles.get(1)).lastModified();
	}

	@Override
	public void reportCachedResult(ProgressReporter reporter, File targetFolder, List<String> outputFiles) {
		final File outputFile = new File(targetFolder, outputFiles.get(0));
		final File emFile = new File(targetFolder, outputFiles.get(1));
		reporter.reportProgress(
				new MsmsEvalResult(
						outputFile,
						emFile));
	}

}
