package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.searchdb.SearchDbWorkPacket;
import edu.mayo.mprc.utilities.progress.ProgressInfo;

import java.io.File;

/**
 * Take Scaffold's spectrum report and loads it into a relational database.
 *
 * @author Roman Zenka
 */
public class SearchDbTask extends AsyncTaskBase {

	private Scaffold3Task scaffold3Task;

	public SearchDbTask(DaemonConnection daemon, FileTokenFactory fileTokenFactory, boolean fromScratch, Scaffold3Task scaffold3Task) {
		super(daemon, fileTokenFactory, fromScratch);
		this.scaffold3Task = scaffold3Task;
		setName("SearchDb");
		setDescription("Load " + fileTokenFactory.fileToTaggedDatabaseToken(getScaffoldSpectraFile()) + " into database");
	}

	private File getScaffoldSpectraFile() {
		return scaffold3Task.getScaffoldSpectraFile();
	}

	@Override
	public WorkPacket createWorkPacket() {
		return new SearchDbWorkPacket(getFullId(), isFromScratch(), scaffold3Task.getReportData().getId(), getScaffoldSpectraFile());
	}

	@Override
	public void onSuccess() {
	}

	@Override
	public void onProgress(ProgressInfo progressInfo) {
	}
}
