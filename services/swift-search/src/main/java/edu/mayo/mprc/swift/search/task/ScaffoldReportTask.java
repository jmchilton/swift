package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.scaffold.report.ScaffoldReportWorkPacket;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

final class ScaffoldReportTask extends AsyncTaskBase {

	private static final Logger LOGGER = Logger.getLogger(ScaffoldReportTask.class);

	private List<File> scaffoldOutputFiles;
	private File peptideReportFile;
	private File proteinReportFile;

	public static final String TASK_NAME = "ScaffoldReport";

	public ScaffoldReportTask(DaemonConnection daemon, List<File> scaffoldOutputFiles, File peptideReportFile, File proteinReportFile, FileTokenFactory fileTokenFactory, boolean fromScratch) {
		super(daemon, fileTokenFactory, fromScratch);
		this.scaffoldOutputFiles = scaffoldOutputFiles;
		this.peptideReportFile = peptideReportFile;
		this.proteinReportFile = proteinReportFile;

		setName(TASK_NAME);
		setDescription("Scaffold reports: " + fileTokenFactory.fileToTaggedDatabaseToken(peptideReportFile) + ", " + fileTokenFactory.fileToTaggedDatabaseToken(proteinReportFile));
	}

	/**
	 * @return Work packet to be sent asynchronously. If it returns null, it means the work was done without a need
	 *         to send a work packet.
	 */
	public WorkPacket createWorkPacket() {
		if (!isFromScratch() && peptideReportFile.exists() && peptideReportFile.length() > 0 &&
				proteinReportFile.exists() && proteinReportFile.length() > 0) {
			LOGGER.info("Skipping scaffold report task because report output files, " + peptideReportFile.getAbsolutePath() + " and " + proteinReportFile.getAbsolutePath() + ", already exist.");
			return null;
		}

		return new ScaffoldReportWorkPacket(scaffoldOutputFiles, peptideReportFile, proteinReportFile, getFullId(), isFromScratch());
	}

	public void onSuccess() {
		completeWhenFileAppears(proteinReportFile);
	}

	public void onProgress(ProgressInfo progressInfo) {
		//Do nothing
	}
}
