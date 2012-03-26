package edu.mayo.mprc.peaks;

import edu.mayo.mprc.enginedeployment.DeploymentResult;

public final class PeaksDeploymentResult extends DeploymentResult {
	private static final long serialVersionUID = 20090324L;

	private String databaseId;

	/**
	 * Null Constructor
	 */
	public PeaksDeploymentResult() {
	}

	public String getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(final String databaseId) {
		this.databaseId = databaseId;
	}
}
