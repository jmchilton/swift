package edu.mayo.mprc.sge;

import com.google.common.base.Joiner;
import edu.mayo.mprc.MprcException;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * this packet needs to provide grid engine task run information
 * including
 * - application name
 * - parameter string
 */
public class GridWorkPacket {

	private static final String GRIDENGINE_STD_ERR_FILE_PREFIX = "e";
	private static final String GRIDENGINE_STD_OUT_FILE_PREFIX = "o";
	private static final String LOG_FILE_EXTENTION = ".sge.log";

	private final String applicationName;
	private List<String> parameters;
	private boolean forceQueue;
	private String jobQueueName;
	private boolean hasMemoryRequirement;
	private String minMemoryRequirement;
	private boolean hasNativeSpecification;
	private String nativeSpecification;
	private String workingFolder;
	private String logFolder;
	private boolean hasWorkingFolder;
	private boolean success = false;
	private boolean failure = false;
	private String errorMessage;

	private static AtomicLong workPacketUniqueIdBase;

	//This id is used to composed the output and error log files of the SGE.
	private long workPacketUniqueId;

	private GridWorkPacketStateListener listener;

	private Long persistentRequestId;

	static {
		workPacketUniqueIdBase = new AtomicLong(System.currentTimeMillis());
	}

	public GridWorkPacket(
			final String applicationName,
			final List<String> parameters) {
		if (applicationName == null) {
			throw new MprcException("The application name for grid work packet was null");
		}

		this.applicationName = applicationName;
		setParameters(parameters);
		jobQueueName = "none";
		minMemoryRequirement = "0";
		nativeSpecification = "none";
		workingFolder = "none";
		logFolder = "none";

		workPacketUniqueId = workPacketUniqueIdBase.getAndIncrement();
	}

	public GridWorkPacket(final GridWorkPacket packet) {
		this.parameters = packet.parameters;
		this.applicationName = packet.applicationName;
		this.forceQueue = packet.forceQueue;
		this.jobQueueName = packet.jobQueueName;
		this.hasMemoryRequirement = packet.hasMemoryRequirement;
		this.minMemoryRequirement = packet.minMemoryRequirement;
		this.hasNativeSpecification = packet.hasNativeSpecification;
		this.nativeSpecification = packet.nativeSpecification;
		this.workingFolder = packet.workingFolder;
		this.logFolder = packet.logFolder;
		this.hasWorkingFolder = packet.hasWorkingFolder;
		this.persistentRequestId = packet.getPersistentRequestId();
		this.listener = packet.listener;

		workPacketUniqueId = packet.getWorkPacketUniqueId();
	}

	public long getWorkPacketUniqueId() {
		return workPacketUniqueId;
	}

	public void setParameters(final List<String> parameters) {
		this.parameters = parameters;
	}

	public void setListener(final GridWorkPacketStateListener listener) {
		this.listener = listener;
	}

	public boolean forcequeue() {
		return forceQueue;
	}

	public String getForcedJobQueue() {
		return jobQueueName;
	}

	public void setJobQueue(final String queueName) {
		forceQueue = true;
		jobQueueName = queueName;
	}

	public void setWorkingFolder(final String workingFolder) {
		this.workingFolder = workingFolder;
		this.hasWorkingFolder = true;
	}

	public String getWorkingFolder() {
		return this.workingFolder;
	}

	public String getLogFolder() {
		return logFolder;
	}

	public void setLogFolder(final String logFolder) {
		this.logFolder = logFolder;
	}

	public String getOutputLogFilePath() {
		return getOutputFileName(false);
	}

	public String getErrorLogFilePath() {
		return getOutputFileName(true);
	}

	public boolean hasWorkingFolder() {
		return this.hasWorkingFolder;
	}

	public boolean hasNativeSpecification() {
		return hasNativeSpecification;
	}

	public String getNativeSpecification() {
		return nativeSpecification;
	}

	public void setNativeSpecification(final String nativespecification) {
		hasNativeSpecification = true;
		nativeSpecification = nativespecification;
	}


	public boolean forceMemoryRequirement() {
		return this.hasMemoryRequirement;
	}

	public String getForcedMemoryRequirement() {
		return this.minMemoryRequirement;
	}

	public void setForcedMemoryRequirement(final String memoryrequirement) {
		this.hasMemoryRequirement = true;
		minMemoryRequirement = memoryrequirement;
	}

	public String getApplicationName() {
		return this.applicationName;
	}

	public List<String> getParameters() {
		return this.parameters;
	}

	public String getParametersAsCallString() {
		if (this.parameters == null || this.parameters.size() == 0) {
			return "";
		}
		return Joiner.on(" ").join(this.parameters);
	}

	public Long getPersistentRequestId() {
		return persistentRequestId;
	}

	public void setPersistentRequestId(final Long persistentRequestId) {
		this.persistentRequestId = persistentRequestId;
	}

	public boolean getPassed() {
		return success;
	}

	public boolean getFailed() {
		return failure;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void fireStateChanged() {
		this.listener.stateChanged(this);
	}

	public void jobUpdateSucceeded() {
		success = true;
		fireStateChanged();
	}

	public void jobUpdateFailed(final String message) {
		failure = true;
		errorMessage = message;
		fireStateChanged();
	}

	public String toString() {
		return "GridWorkPacket: " + applicationName + " " + getParametersAsCallString() + " (queue=" + jobQueueName + ")";
	}

	/**
	 * Generates output file name for given GridEngineWorkPacket object.
	 *
	 * @param isError If true, file name is error log. Otherwise, file name is standard log.
	 * @return
	 */
	private String getOutputFileName(final boolean isError) {
		String fileName = null;

		if (isError) {
			fileName = new File(getLogFolder(), GRIDENGINE_STD_ERR_FILE_PREFIX + getWorkPacketUniqueId() + LOG_FILE_EXTENTION).getAbsolutePath();
		} else {
			fileName = new File(getLogFolder(), GRIDENGINE_STD_OUT_FILE_PREFIX + getWorkPacketUniqueId() + LOG_FILE_EXTENTION).getAbsolutePath();
		}

		return fileName;
	}
}

