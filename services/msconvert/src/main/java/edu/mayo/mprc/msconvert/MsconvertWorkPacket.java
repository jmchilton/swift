package edu.mayo.mprc.msconvert;

import edu.mayo.mprc.daemon.CachableWorkPacket;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.WorkPacketBase;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.progress.ProgressReporter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * One task for batch converter.
 * Defines who searches, using what params, what file and where to put outputs.
 * The packet is now in the format used by messaging. Eventually there will be an unified system for both grid engine
 * and daemon messaging in place.
 */
public final class MsconvertWorkPacket extends WorkPacketBase implements CachableWorkPacket {
	private static final long serialVersionUID = 20071220L;
	private File outputFile;
	private boolean skipIfExists;
	private File inputFile;
	private boolean publicAccess;

	public MsconvertWorkPacket(final String taskId, final boolean fromScratch) {
		super(taskId, fromScratch);
	}

	/**
	 * Request to convert a .RAW file to .mgf file.
	 *
	 * @param outputFile   This is the desired target of the output. The cache can overwrite this to anything it sees fit.
	 *                     If that happens, a {@link MsconvertResult} class is sent back
	 *                     as progress report.
	 * @param publicAccess If the .mgf caching is enabled, the files will never be visible to the end user.
	 *                     This parameter ensures the file will be provided.
	 */
	public MsconvertWorkPacket(final File outputFile,
	                           final boolean bSkipIfExists,
	                           final File inputFile,
	                           final String taskId,
	                           final boolean fromScratch,
	                           final boolean publicAccess) {
		super(taskId, fromScratch);

		assert outputFile != null : "msconvert request cannot be created: output file is null";
		assert inputFile != null : "msconvert request cannot be created: input file is null";

		this.outputFile = outputFile;
		this.skipIfExists = bSkipIfExists;
		this.inputFile = inputFile;
		this.publicAccess = publicAccess;
	}

	@Override
	public boolean isPublishResultFiles() {
		return isPublicAccess();
	}

	public File getOutputFile() {
		return outputFile;
	}

	public boolean isSkipIfExists() {
		return skipIfExists;
	}

	public File getInputFile() {
		return inputFile;
	}

	public boolean isPublicAccess() {
		return publicAccess;
	}

	@Override
	public void synchronizeFileTokensOnReceiver() {
		uploadAndWait("outputFile");
	}

	@Override
	public String getStringDescriptionOfTask() {
		final File sourceFile = getInputFile();
		return "Input:" + sourceFile.getAbsolutePath();
	}

	@Override
	public WorkPacket translateToWorkInProgressPacket(final File wipFolder) {
		final WorkPacket modifiedWorkPacket;
		modifiedWorkPacket = new MsconvertWorkPacket(
				new File(wipFolder, getOutputFile().getName()),
				isSkipIfExists(),
				getInputFile(),
				getTaskId(),
				isFromScratch(),
				/*public access*/false);
		return modifiedWorkPacket;
	}

	@Override
	public List<String> getOutputFiles() {
		return Arrays.asList(FileUtilities.getFileNameWithoutExtension(getInputFile()) + ".mgf");
	}

	@Override
	public boolean cacheIsStale(final File subFolder, final List<String> outputFiles) {
		return getInputFile().lastModified() > new File(subFolder, outputFiles.get(0)).lastModified();
	}

	@Override
	public void reportCachedResult(final ProgressReporter reporter, final File targetFolder, final List<String> outputFiles) {
		final File cachedMgf = new File(targetFolder, outputFiles.get(0));
		reporter.reportProgress(new MsconvertResult(cachedMgf));
	}
}
