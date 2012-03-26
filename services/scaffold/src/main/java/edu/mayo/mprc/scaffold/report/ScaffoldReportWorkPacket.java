package edu.mayo.mprc.scaffold.report;

import edu.mayo.mprc.daemon.WorkPacketBase;

import java.io.File;
import java.util.List;

public final class ScaffoldReportWorkPacket extends WorkPacketBase {

	private static final long serialVersionUID = 1L;

	private List<File> scaffoldOutputFiles;
	private File peptideReportFile;
	private File proteinReportFile;

	public ScaffoldReportWorkPacket(final String taskId, final boolean fromScratch) {
		super(taskId, fromScratch);
	}

	/**
	 * @param scaffoldOutputFiles
	 */
	public ScaffoldReportWorkPacket(final List<File> scaffoldOutputFiles, final File peptideReportFile, final File proteinReportFile, final String taskId, final boolean fromScratch) {
		super(taskId, fromScratch);

		this.scaffoldOutputFiles = scaffoldOutputFiles;
		this.peptideReportFile = peptideReportFile;
		this.proteinReportFile = proteinReportFile;
	}

	public List<File> getScaffoldOutputFiles() {
		return scaffoldOutputFiles;
	}

	public File getPeptideReportFile() {
		return peptideReportFile;
	}

	public File getProteinReportFile() {
		return proteinReportFile;
	}

	@Override
	public void synchronizeFileTokensOnReceiver() {
		uploadAndWait("peptideReportFile");
		uploadAndWait("proteinReportFile");
	}
}
