package edu.mayo.mprc.daemon;

import edu.mayo.mprc.utilities.progress.ProgressReporter;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class SimpleTestWorkPacket extends WorkPacketBase implements Serializable, CachableWorkPacket {
	private static final long serialVersionUID = -2096468611424782391L;
	private File resultFile;

	/**
	 * @param taskId      Task identifier to be used for nested diagnostic context when logging.
	 * @param fromScratch
	 */
	public SimpleTestWorkPacket(String taskId, boolean fromScratch) {
		super(taskId, fromScratch);
	}

	public File getResultFile() {
		return resultFile;
	}

	public void setResultFile(File resultFile) {
		this.resultFile = resultFile;
	}

	@Override
	public boolean isPublishResultFiles() {
		return false;
	}

	@Override
	public File getOutputFile() {
		return resultFile;
	}

	@Override
	public String getStringDescriptionOfTask() {
		return getTaskId();
	}

	@Override
	public WorkPacket translateToWorkInProgressPacket(File wipFolder) {
		final SimpleTestWorkPacket translatedPacket = new SimpleTestWorkPacket("WIP:" + getTaskId(), false);
		translatedPacket.setResultFile(new File(wipFolder, getResultFile().getName()));
		return translatedPacket;
	}

	@Override
	public List<String> getOutputFiles() {
		return Arrays.asList(getResultFile().getName());
	}

	@Override
	public boolean cacheIsStale(File subFolder, List<String> outputFiles) {
		return false;
	}

	@Override
	public void reportCachedResult(ProgressReporter reporter, File targetFolder, List<String> outputFiles) {
	}
}
