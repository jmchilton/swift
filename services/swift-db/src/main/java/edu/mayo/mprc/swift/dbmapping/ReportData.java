package edu.mayo.mprc.swift.dbmapping;


import java.io.File;
import java.text.MessageFormat;
import java.util.Date;

public class ReportData {

	private Long id;
	private File reportFileId;
	private Date dateCreated;
	private SearchRun searchRun;


	public ReportData() {
	}

	public ReportData(File file, Date dateCreated, SearchRun searchRun) {
		this.reportFileId = file;
		this.dateCreated = dateCreated;
		this.searchRun = searchRun;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public File getReportFileId() {
		return reportFileId;
	}

	public void setReportFileId(File reportFileId) {
		this.reportFileId = reportFileId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public SearchRun getSearchRun() {
		return searchRun;
	}

	public void setSearchRun(SearchRun searchRun) {
		this.searchRun = searchRun;
	}

	public String toString() {
		return MessageFormat.format("{0}: {1} {2}",
				getId(),
				getSearchRun() == null ? "no search run" : (getSearchRun().getTitle() + "(" + getSearchRun().getId() + ")"),
				getReportFileId().toString());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ReportData)) {
			return false;
		}

		ReportData that = (ReportData) o;

		if (getDateCreated() != null ? !getDateCreated().equals(that.getDateCreated()) : that.getDateCreated() != null) {
			return false;
		}
		if (getReportFileId() != null ? !getReportFileId().equals(that.getReportFileId()) : that.getReportFileId() != null) {
			return false;
		}
		if (getSearchRun() != null ? !getSearchRun().equals(that.getSearchRun()) : that.getSearchRun() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = getReportFileId() != null ? getReportFileId().hashCode() : 0;
		result = 31 * result + (getDateCreated() != null ? getDateCreated().hashCode() : 0);
		result = 31 * result + (getSearchRun() != null ? getSearchRun().hashCode() : 0);
		return result;
	}
}
