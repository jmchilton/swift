package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.qa.RAWDumpResult;
import edu.mayo.mprc.qa.RAWDumpWorkPacket;
import edu.mayo.mprc.utilities.progress.ProgressInfo;
import org.apache.log4j.Logger;

import java.io.File;

final class RAWDumpTask extends AsyncTaskBase {
	private static final Logger LOGGER = Logger.getLogger(RAWDumpTask.class);

	private File rawFile;
	private File rawInfoFile;
	private File rawSpectraFile;
	private File chromatogramFile;
	private File outputFolder;

	public static final String RAW_INFO_FILE_SUFFIX = ".info.tsv";
	public static final String RAW_SPECTRA_FILE_SUFFIX = ".spectra.tsv";
	public static final String CHROMATOGRAM_FILE_SUFFIX = ".chroma.gif";

	public RAWDumpTask(File rawFile, File outputFolder, DaemonConnection daemonConnection, FileTokenFactory fileTokenFactory, boolean fromScratch) {
		super(daemonConnection, fileTokenFactory, fromScratch);

		this.rawFile = rawFile;
		this.outputFolder = outputFolder;
		this.rawInfoFile = getExpectedRawInfoFile();
		this.rawSpectraFile = getExpectedRawSpectraFile();
		this.chromatogramFile = getExpectedChromatogramFile();

		setName("RAW Dump");
		updateDescription();
	}

	private void updateDescription() {
		setDescription("RAW Dump info file, " + getFileTokenFactory().fileToTaggedDatabaseToken(rawInfoFile)
				+ ", spectra file, " + getFileTokenFactory().fileToTaggedDatabaseToken(rawSpectraFile) + ".");
	}

	@Override
	public WorkPacket createWorkPacket() {
		return new RAWDumpWorkPacket(rawFile, rawInfoFile, rawSpectraFile, chromatogramFile, getFullId(), isFromScratch());
	}

	public File getOutputFolder() {
		return outputFolder;
	}

	public File getExpectedRawInfoFile() {
		return new File(outputFolder, rawFile.getName() + RAW_INFO_FILE_SUFFIX);
	}

	public File getExpectedRawSpectraFile() {
		return new File(outputFolder, rawFile.getName() + RAW_SPECTRA_FILE_SUFFIX);
	}

	public File getExpectedChromatogramFile() {
		return new File(outputFolder, rawFile.getName() + CHROMATOGRAM_FILE_SUFFIX);
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

	@Override
	public void onSuccess() {
		//Do nothing
	}

	@Override
	public void onProgress(ProgressInfo progressInfo) {
		if (progressInfo instanceof RAWDumpResult) {
			final RAWDumpResult dumpResult = (RAWDumpResult) progressInfo;
			rawInfoFile = dumpResult.getRawInfoFile();
			rawSpectraFile = dumpResult.getRawSpectraFile();
			updateDescription();
			chromatogramFile = dumpResult.getChromatogramFile();
		}
	}
}
