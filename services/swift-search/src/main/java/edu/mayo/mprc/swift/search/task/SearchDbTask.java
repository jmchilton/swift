package edu.mayo.mprc.swift.search.task;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.searchdb.RawFileMetaData;
import edu.mayo.mprc.searchdb.SearchDbWorkPacket;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.progress.ProgressInfo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Take Scaffold's spectrum report and loads it into a relational database.
 *
 * @author Roman Zenka
 */
public class SearchDbTask extends AsyncTaskBase {

	private Scaffold3Task scaffold3Task;
	private Map<String, RAWDumpTask> rawDumpTaskMap = new HashMap<String, RAWDumpTask>(5);

	public SearchDbTask(DaemonConnection daemon, FileTokenFactory fileTokenFactory, boolean fromScratch, Scaffold3Task scaffold3Task) {
		super(daemon, fileTokenFactory, fromScratch);
		this.scaffold3Task = scaffold3Task;
		setName("SearchDb");
		setDescription("Load " + fileTokenFactory.fileToTaggedDatabaseToken(getScaffoldSpectraFile()) + " into database");
	}

	private void addRawDumpTask(final RAWDumpTask task) {
		final String fileName = FileUtilities.stripExtension(task.getRawFile().getName());
		if (rawDumpTaskMap.containsKey(fileName)) {
			throw new MprcException("Two files of identical name: " + task.getRawFile().getName() + " cannot be distinguished in resulting analysis.");
		}
		rawDumpTaskMap.put(fileName, task);
	}

	private File getScaffoldSpectraFile() {
		return scaffold3Task.getScaffoldSpectraFile();
	}

	@Override
	public WorkPacket createWorkPacket() {
		HashMap<String, RawFileMetaData> metaDataMap = new HashMap<String, RawFileMetaData>(rawDumpTaskMap.size());
		for (final Map.Entry<String, RAWDumpTask> entry : rawDumpTaskMap.entrySet()) {
			final RAWDumpTask task = entry.getValue();
			try {
				metaDataMap.put(entry.getKey(), new RawFileMetaData(
						Files.toString(task.getRawInfoFile(), Charsets.ISO_8859_1),
						Files.toString(task.getTuneMethodFile(), Charsets.ISO_8859_1),
						Files.toString(task.getInstrumentMethodFile(), Charsets.ISO_8859_1),
						Files.toString(task.getSampleInformationFile(), Charsets.ISO_8859_1),
						Files.toString(task.getErrorLogFile(), Charsets.ISO_8859_1)
				));
			} catch (IOException e) {
				throw new MprcException("Could not load .RAW file metadata", e);
			}
		}

		return new SearchDbWorkPacket(getFullId(), isFromScratch(), scaffold3Task.getReportData().getId(), getScaffoldSpectraFile(), metaDataMap);
	}

	@Override
	public void onSuccess() {
	}

	@Override
	public void onProgress(ProgressInfo progressInfo) {
	}
}
