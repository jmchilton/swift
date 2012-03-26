package edu.mayo.mprc.workflow.engine;

/**
 * Notifies the monitor that the task got assigned information about the host it is running on.
 */
public final class AssignedExecutedOnHost {

	private String hostInfo;

	public AssignedExecutedOnHost(final String hostInfo) {
		this.hostInfo = hostInfo;
	}

	public String getHostInfo() {
		return hostInfo;
	}
}
