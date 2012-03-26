package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.daemon.WorkPacketBase;
import edu.mayo.mprc.swift.dbmapping.ReportData;

import java.io.File;
import java.util.Map;

/**
 * Load search results for Swift search report (read - Scaffold file) of given ID.
 * <p/>
 * The packet sends just an ID because it makes no sense to send Hibernate object - it will be disconnected from the session
 * anyway.
 */
public final class SearchDbWorkPacket extends WorkPacketBase {
	private static final long serialVersionUID = 100830535859947146L;
	/**
	 * ID of the particular Swift report that ties to the Scaffold file.
	 * Links to {@link ReportData} object.
	 * This way
	 */
	private long reportDataId;

	/**
	 * Scaffold-produced spectrum report file.
	 */
	private File scaffoldSpectrumReport;

	/**
	 * Map from raw file name
	 */
	private Map<String, RawFileMetaData> fileMetaDataMap;

	public SearchDbWorkPacket(final String taskId, final boolean fromScratch, final long reportDataId, final File scaffoldSpectrumReport, final Map<String, RawFileMetaData> fileMetaDataMap) {
		super(taskId, fromScratch);
		this.reportDataId = reportDataId;
		this.scaffoldSpectrumReport = scaffoldSpectrumReport;
		this.fileMetaDataMap = fileMetaDataMap;
	}

	public long getReportDataId() {
		return reportDataId;
	}

	public File getScaffoldSpectrumReport() {
		return scaffoldSpectrumReport;
	}

	public Map<String, RawFileMetaData> getFileMetaDataMap() {
		return fileMetaDataMap;
	}
}
