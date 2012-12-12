package edu.mayo.mprc.idpicker;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import edu.mayo.mprc.daemon.CachableWorkPacket;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.WorkPacketBase;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.progress.ProgressReporter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * A task for idpQonvert to convert .pepXML into .idp files.
 */
public final class IdpickerWorkPacket extends WorkPacketBase implements CachableWorkPacket {
	private static final long serialVersionUID = 20121109;

	/**
	 * Input .pepXML files
	 */
	private List<File> inputFiles;
	/**
	 * Output .idp file
	 */
	private File outputFile;
	/**
	 * Settings for idpQonvert.
	 */
	private IdpQonvertSettings settings;

	public IdpickerWorkPacket(final String taskId, final boolean fromScratch) {
		super(taskId, fromScratch);
	}

	/**
	 * Request to convert a .pepXML file into
	 *
	 * @param outputFile This is the desired target of the output. The cache can overwrite this to anything it sees fit.
	 *                   If that happens, a {@link IdpickerResult} class is sent back
	 *                   as progress report.
	 */
	public IdpickerWorkPacket(final File outputFile,
	                          final List<File> inputFiles,
	                          final IdpQonvertSettings settings,
	                          final String taskId,
	                          final boolean fromScratch) {
		super(taskId, fromScratch);

		this.inputFiles = inputFiles;
		this.outputFile = outputFile;
		this.settings = settings;
	}

	@Override
	public boolean isPublishResultFiles() {
		return true;
	}

	public List<File> getInputFiles() {
		return inputFiles;
	}

	@Override
	public File getOutputFile() {
		return outputFile;
	}

	public IdpQonvertSettings getSettings() {
		return settings;
	}

	@Override
	public void synchronizeFileTokensOnReceiver() {
		super.synchronizeFileTokensOnReceiver();
		uploadAndWait("outputFile");
	}

	@Override
	public String getStringDescriptionOfTask() {
		return "Inputs:\n" + getInputPathsAsString("\n") + "\n\nOutput:\n" + outputFile.getAbsolutePath()
				+ "\n\nParameters:\n'" + Joiner.on("', '").join(getSettings().toCommandLine()) + "'";
	}

	public String getInputPathsAsString(String delimiter) {
		return Joiner.on(delimiter).join(getInputFilePaths());
	}

	public List<String> getInputFilePaths() {
		return Ordering.natural().sortedCopy(
					Iterables.transform(
							Iterables.filter(inputFiles, Predicates.<Object>notNull()),
							new FileUtilities.AbsolutePath()));
	}

	@Override
	public WorkPacket translateToWorkInProgressPacket(final File wipFolder) {
		return new IdpickerWorkPacket(new File(wipFolder, getOutputFile().getName()),
				inputFiles, settings, getTaskId(), isFromScratch());
	}

	@Override
	public List<String> getOutputFiles() {
		return Arrays.asList(getOutputFile().getAbsolutePath());
	}

	@Override
	public boolean cacheIsStale(final File subFolder, final List<String> outputFiles) {
		final long outputModifiedTime = new File(subFolder, outputFiles.get(0)).lastModified();
		for (final File inputFile : inputFiles) {
			if (inputFile.lastModified() > outputModifiedTime) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void reportCachedResult(final ProgressReporter reporter, final File targetFolder, final List<String> outputFiles) {
		final File cachedFile = new File(targetFolder, outputFiles.get(0));
		reporter.reportProgress(new IdpickerResult(cachedFile));
	}
}
