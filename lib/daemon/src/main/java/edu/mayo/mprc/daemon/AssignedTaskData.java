package edu.mayo.mprc.daemon;

import edu.mayo.mprc.daemon.files.*;
import edu.mayo.mprc.utilities.progress.ProgressInfo;

import java.io.File;

/**
 * Progress report sent after task is submitted for execution.
 */
public final class AssignedTaskData implements ProgressInfo, FileTokenHolder {
	private static final long serialVersionUID = 20071220L;
	private FileToken errorLogFile;
	private FileToken outputLogFile;
	private final String jobId;

	private transient ReceiverTokenTranslator receiverTokenTranslator;

	public AssignedTaskData(File outputLogFile, File errorLogFile) {
		this(null, outputLogFile, errorLogFile);
	}

	public AssignedTaskData(String id, String outputLogFilePath, String errorLogFilePath) {
		this(id, new File(outputLogFilePath), new File(errorLogFilePath));
	}

	private AssignedTaskData(String id, File outputLogFile, File errorLogFile) {
		this.outputLogFile = FileTokenFactory.createAnonymousFileToken(outputLogFile);
		this.errorLogFile = FileTokenFactory.createAnonymousFileToken(errorLogFile);
		this.jobId = id;
	}

	public String getAssignedId() {
		return jobId;
	}

	public File getErrorLogFile() {
		return receiverTokenTranslator.getFile(errorLogFile);
	}

	public FileToken getErrorLogFileToken() {
		return errorLogFile;
	}

	public File getOutputLogFile() {
		return receiverTokenTranslator.getFile(outputLogFile);
	}

	public FileToken getOutputLogFileToken() {
		return outputLogFile;
	}

	public String toString() {
		return "SGE task id: " + (jobId != null ? jobId : "None") + " | Standard Out: " + outputLogFile.toString() + " | Error Out: " + errorLogFile.toString();
	}

	public void translateOnSender(SenderTokenTranslator translator) {
		errorLogFile = translator.translateBeforeTransfer(errorLogFile);
		outputLogFile = translator.translateBeforeTransfer(outputLogFile);
	}

	public void translateOnReceiver(ReceiverTokenTranslator translator, FileTokenSynchronizer synchronizer) {
		this.receiverTokenTranslator = translator;
	}

	@Override
	public void synchronizeFileTokensOnReceiver() {
		//Do nothing, no file needs synchronization.
	}
}
