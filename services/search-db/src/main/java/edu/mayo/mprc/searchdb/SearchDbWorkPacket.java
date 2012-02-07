package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.daemon.WorkPacketBase;
import edu.mayo.mprc.swift.dbmapping.ReportData;

import java.io.File;

/**
 * Load search results for Swift search report (read - Scaffold file) of given ID.
 * <p/>
 * The packet sends just an ID because it makes no sense to send Hibernate object - it will be disconnected from the session
 * anyway.
 */
public final class SearchDbWorkPacket extends WorkPacketBase {
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

    public SearchDbWorkPacket(String taskId, boolean fromScratch, long reportDataId, File scaffoldSpectrumReport) {
        super(taskId, fromScratch);
        this.reportDataId = reportDataId;
        this.scaffoldSpectrumReport = scaffoldSpectrumReport;
    }

    public long getReportDataId() {
        return reportDataId;
    }

    public File getScaffoldSpectrumReport() {
        return scaffoldSpectrumReport;
    }
}
