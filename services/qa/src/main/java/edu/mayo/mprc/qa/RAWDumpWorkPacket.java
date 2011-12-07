package edu.mayo.mprc.qa;

import edu.mayo.mprc.daemon.CachableWorkPacket;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.WorkPacketBase;
import edu.mayo.mprc.daemon.progress.ProgressReporter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Class contains information for RAW file dump request.
 */
public final class RAWDumpWorkPacket extends WorkPacketBase implements CachableWorkPacket {

	private static final long serialVersionUID = 200220L;

	private File rawFile;
	private File rawInfoFile;
	private File rawSpectraFile;
	private File chromatogramFile;

	public RAWDumpWorkPacket(String taskId, boolean fromScratch) {
		super(taskId, fromScratch);
	}

	public RAWDumpWorkPacket(File rawFile, File rawInfoFile, File rawSpectraFile, File chromatogramFile, String taskId, boolean fromScratch) {
		super(taskId, fromScratch);

		assert rawFile != null : "Raw input file can not be null.";
		assert rawInfoFile != null : "Info output file must be defined.";
		assert rawSpectraFile != null : "Spectra output file must be defined.";

		this.rawFile = rawFile;
		this.rawInfoFile = rawInfoFile;
		this.rawSpectraFile = rawSpectraFile;
		this.chromatogramFile = chromatogramFile;
	}

	public File getRawFile() {
		return rawFile;
	}

	public File getRawInfoFile() {
		return rawInfoFile;
	}

	public File getRawSpectraFile() {
		return rawSpectraFile;
	}

	public File getChromatogramFile() {
		return chromatogramFile;
	}

	public void setChromatogramFile(File chromatogramFile) {
		this.chromatogramFile = chromatogramFile;
	}

	@Override
	public void synchronizeFileTokensOnReceiver() {
		uploadAndWait("rawInfoFile");
		uploadAndWait("rawSpectraFile");
		uploadAndWait("chromatogramFile");
	}

	@Override
	public boolean isPublishResultFiles() {
		// We never publish these intermediate files
		return false;
	}

	@Override
	public File getOutputFile() {
		return null;
	}

	@Override
	public String getStringDescriptionOfTask() {
		StringBuilder description = new StringBuilder();
		description
				.append("Input:")
				.append(getRawFile().getAbsolutePath())
				.append("\n")
				.append("Chromatogram:")
				.append("true")
				.append("\n");
		return description.toString();
	}

	@Override
	public WorkPacket translateToWorkInProgressPacket(File wipFolder) {
		return new RAWDumpWorkPacket(
				getRawFile(),
				new File(wipFolder, getRawInfoFile().getName()),
				new File(wipFolder, getRawSpectraFile().getName()),
				new File(wipFolder, getChromatogramFile().getName()),
				getTaskId(),
				isFromScratch()
		);
	}

	@Override
	public List<String> getOutputFiles() {
		return Arrays.asList(
				getRawInfoFile().getName(),
				getRawSpectraFile().getName(),
				getChromatogramFile().getName());
	}

	@Override
	public boolean cacheIsStale(File subFolder, List<String> outputFiles) {
		final long inputFileModified = getRawFile().lastModified();
		return inputFileModified > new File(subFolder, outputFiles.get(0)).lastModified() ||
				inputFileModified > new File(subFolder, outputFiles.get(1)).lastModified() ||
				inputFileModified > new File(subFolder, outputFiles.get(2)).lastModified();
	}

	@Override
	public void reportCachedResult(ProgressReporter reporter, File targetFolder, List<String> outputFiles) {
		final File rawInfoFile = new File(targetFolder, outputFiles.get(0));
		final File rawSpectraFile = new File(targetFolder, outputFiles.get(1));
		final File chromatogramFile = new File(targetFolder, outputFiles.get(2));
		reporter.reportProgress(new RAWDumpResult(rawInfoFile, rawSpectraFile, chromatogramFile));
	}

}
