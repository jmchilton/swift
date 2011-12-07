package edu.mayo.mprc.dbundeploy;

import edu.mayo.mprc.daemon.progress.ProgressInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Class contains results of multiple database undeployment tasks.
 */
public final class DatabaseUndeployerResult implements ProgressInfo {
	private static final long serialVersionUID = 20101221L;

	private Map<String, UndeploymentTaskResult> undeploymentTaskResults;

	public DatabaseUndeployerResult() {
		this.undeploymentTaskResults = new HashMap<String, UndeploymentTaskResult>();
	}

	public Map<String, UndeploymentTaskResult> getDatabaseUndeployerResults() {
		return undeploymentTaskResults;
	}

	public void addUndeploymentTaskResult(String connectionName, UndeploymentTaskResult taskResult) {
		undeploymentTaskResults.put(connectionName, taskResult);
	}
}
