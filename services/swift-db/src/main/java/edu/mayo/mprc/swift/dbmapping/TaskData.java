package edu.mayo.mprc.swift.dbmapping;

import com.google.common.base.Objects;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.workflow.persistence.TaskState;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class TaskData {
	private Integer id;
	private String taskName;
	private Date queueTimestamp;
	private Date startTimestamp;
	private Date endTimestamp;
	private int errorCode;
	private String outputLogDatabaseToken;
	private String errorLogDatabaseToken;

	private String errorMessage;
	private SearchRun searchRun;
	private TaskStateData taskState;
	private String descriptionLong;
	private String gridJobId;
	private String host;
	private String exceptionString;
	private Float percentDone;

	public TaskData() {
		this.setTaskState(new TaskStateData(TaskState.UNINITIALIZED.getText()));
	}

	public TaskData(String name,
	                Date queueTimeStamp,
	                Date startTimeStamp,
	                Date endTimeStamp,
	                SearchRun searchRun, TaskStateData taskState, String descriptionLong) {
		setId(null);
		this.taskName = name;
		this.queueTimestamp = queueTimeStamp;
		this.startTimestamp = startTimeStamp;
		this.endTimestamp = endTimeStamp;
		this.errorCode = 0;
		this.errorMessage = null;
		this.searchRun = searchRun;
		this.setTaskState(taskState);
		this.descriptionLong = descriptionLong;
		this.exceptionString = null;
		this.percentDone = null;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskName() {
		return taskName;
	}

	public Date getQueueTimestamp() {
		return queueTimestamp;
	}

	public void setQueueTimestamp(Date queueTimestamp) {
		this.queueTimestamp = queueTimestamp;
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

	public SearchRun getSearchRun() {
		return searchRun;
	}

	public void setSearchRun(SearchRun searchRun) {
		this.searchRun = searchRun;
	}

	public void setTaskState(TaskStateData taskState) {
		assert (taskState != null);
		this.taskState = taskState;
	}

	public TaskStateData getTaskState() {
		return taskState;
	}

	public void setDescriptionLong(String descriptionLong) {
		this.descriptionLong = descriptionLong;
	}

	public String getDescriptionLong() {
		return descriptionLong;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setOutputLogDatabaseToken(String outputLogDatabaseToken) {
		this.outputLogDatabaseToken = outputLogDatabaseToken;
	}

	public String getOutputLogDatabaseToken() {
		return outputLogDatabaseToken;
	}

	public String getErrorLogDatabaseToken() {
		return errorLogDatabaseToken;
	}

	public void setErrorLogDatabaseToken(String errorLogDatabaseToken) {
		this.errorLogDatabaseToken = errorLogDatabaseToken;
	}

	public String getGridJobId() {
		return gridJobId;
	}

	public void setGridJobId(String gridJobId) {
		if (gridJobId == null && this.gridJobId != null) {
			// TODO: Hack. This should never happen in practice.
			return;
		}
		this.gridJobId = gridJobId;
	}

	public void setHostString(String hostString) {
		this.host = hostString;
	}

	public String getHostString() {
		return host;
	}

	public void setExceptionString(String exceptionString) {
		this.exceptionString = exceptionString;
	}

	public String getExceptionString() {
		return exceptionString;
	}

	private String getHost() {
		return host;
	}

	public Float getPercentDone() {
		return percentDone;
	}

	public void setPercentDone(Float percentDone) {
		this.percentDone = percentDone;
	}

	public void setException(Throwable t) {
		setErrorMessage(MprcException.getDetailedMessage(t));
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		try {
			t.printStackTrace(writer);
			String stackTrace = sw.toString();
			setExceptionString(stackTrace);
		} finally {
			writer.close();
		}
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof TaskData)) {
			return false;
		}

		TaskData taskData = (TaskData) obj;

		return Objects.equal(getEndTimestamp(), taskData.getEndTimestamp()) &&
				Objects.equal(getHost(), taskData.getHost()) &&
				Objects.equal(getQueueTimestamp(), taskData.getQueueTimestamp()) &&
				Objects.equal(getStartTimestamp(), taskData.getStartTimestamp()) &&
				Objects.equal(getTaskName(), taskData.getTaskName()) &&
				Objects.equal(getDescriptionLong(), taskData.getDescriptionLong()) &&
				Objects.equal(getPercentDone(), taskData.getPercentDone());
	}

	public int hashCode() {
		return Objects.hashCode(getTaskName(),
				getQueueTimestamp(),
				getStartTimestamp(),
				getEndTimestamp(),
				getErrorCode(),
				getDescriptionLong(),
				getHost(),
				getPercentDone());
	}

	public String toString() {
		return "TaskData{" +
				"id=" + getId() +
				", taskName='" + getTaskName() + '\'' +
				", queueTimestamp=" + getQueueTimestamp() +
				", startTimestamp=" + getStartTimestamp() +
				", endTimestamp=" + getEndTimestamp() +
				", errorCode=" + getErrorCode() +
				", outputLogDatabaseToken=" + getOutputLogDatabaseToken() +
				", errorLogDatabaseToken=" + getErrorLogDatabaseToken() +
				", errorMessage='" + getErrorMessage() + '\'' +
				", searchRun=" + getSearchRun() +
				", taskState=" + getTaskState() +
				", descriptionLong='" + getDescriptionLong() + '\'' +
				", gridId=" + getGridJobId() +
				", host='" + getHost() + '\'' +
				", exceptionString='" + getExceptionString() + '\'' +
				", percentDone='" + getPercentDone() + '\'' +
				'}';
	}
}

