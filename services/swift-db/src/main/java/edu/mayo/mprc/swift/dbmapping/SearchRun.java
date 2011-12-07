package edu.mayo.mprc.swift.dbmapping;

import edu.mayo.mprc.database.PersistableBase;
import edu.mayo.mprc.workspace.User;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A single run of a given search.
 */
public class SearchRun extends PersistableBase implements Serializable {
	private static final long serialVersionUID = 20080115L;

	// Duplicate of {@link SwiftSearchDefinition}.title to prevent loading of a complex table
	private String title;
	// Duplicate of {@link SwiftSearchDefinition}.user to prevent loading of a complex table
	private User submittingUser;
	/**
	 * @deprecated Replaced by {@link #swiftSearch}
	 */
	private File xmlDefFile;
	private Integer swiftSearch;
	private Date startTimestamp;
	private Date endTimestamp;
	private int errorCode;
	private String errorMessage;
	private int numTasks;
	private int tasksWithWarning;
	private int tasksFailed;
	private int tasksCompleted;
	private Set<ReportData> reports = new HashSet<ReportData>();

	private int hidden;

	public SearchRun() {
	}

	public SearchRun(String title, User submittingUser, SwiftSearchDefinition swiftSearch, Date startTimestamp, Date endTimestamp,
	                 int errorCode, String errorMessage,
	                 int numTasks, int tasksWithWarning, int taskFailed, int tasksCompleted, boolean hidden) {
		this.title = title;
		this.submittingUser = submittingUser;
		this.swiftSearch = swiftSearch == null ? 0 : swiftSearch.getId();
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.numTasks = numTasks;
		this.tasksWithWarning = tasksWithWarning;
		this.tasksFailed = taskFailed;
		this.tasksCompleted = tasksCompleted;
		this.hidden = hidden ? 1 : 0;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setSubmittingUser(User submittingUser) {
		this.submittingUser = submittingUser;
	}

	public User getSubmittingUser() {
		return submittingUser;
	}

	public Integer getSwiftSearch() {
		return swiftSearch;
	}

	public void setSwiftSearch(Integer swiftSearch) {
		this.swiftSearch = swiftSearch;
	}

	public void setXmlDefFile(File xmlDefFile) {
		this.xmlDefFile = xmlDefFile;
	}

	public File getXmlDefFile() {
		return xmlDefFile;
	}

	public void setStartTimestamp(Date startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public Date getStartTimestamp() {
		return startTimestamp;
	}

	public void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	public Date getEndTimestamp() {
		return endTimestamp;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setNumTasks(int numTasks) {
		this.numTasks = numTasks;
	}

	public int getNumTasks() {
		return numTasks;
	}

	public void setTasksFailed(int tasksFailed) {
		this.tasksFailed = tasksFailed;
	}

	public int getTasksFailed() {
		return tasksFailed;
	}

	public void setTasksCompleted(int tasksCompleted) {
		this.tasksCompleted = tasksCompleted;
	}

	public int getTasksCompleted() {
		return tasksCompleted;
	}

	public void setTasksWithWarning(int tasksWithWarning) {
		this.tasksWithWarning = tasksWithWarning;
	}

	public int getTasksWithWarning() {
		return tasksWithWarning;
	}

	public Set<ReportData> getReports() {
		return reports;
	}

	public void setReports(Set<ReportData> reports) {
		this.reports = reports;
	}

	public int getHidden() {
		return hidden;
	}

	public void setHidden(int hidden) {
		this.hidden = hidden;
	}

	/**
	 * Determine whether the search run completed (does not mean successfully completed, just that it will not run anymore).
	 * <p/>
	 * This is done by checkeing whether the total
	 * amount of tasks that are ok/with warnings/failed equals the total amount of tasks.
	 *
	 * @return True if the search will not run anymore.
	 */
	public boolean isCompleted() {
		return (getTasksCompleted() + getTasksWithWarning() + getTasksFailed()) >= getNumTasks();
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof SearchRun)) {
			return false;
		}

		SearchRun that = (SearchRun) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = (getId() != null ? getId().hashCode() : 0);
		return result;
	}

	public String toString() {
		return getId() + ": " + getTitle() + " (" + getSubmittingUser() + ")";
	}
}
