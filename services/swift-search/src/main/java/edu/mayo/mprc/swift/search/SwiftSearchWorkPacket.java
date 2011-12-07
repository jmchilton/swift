package edu.mayo.mprc.swift.search;

import edu.mayo.mprc.daemon.WorkPacketBase;

public final class SwiftSearchWorkPacket extends WorkPacketBase {
	private static final long serialVersionUID = 20110901;
	private int swiftSearchId;
	private int previousSearchRunId; // For reruns

	public SwiftSearchWorkPacket(int swiftSearchId,
	                             String taskId,
	                             boolean fromScratch,
	                             int previousSearchRunId) {
		super(taskId, fromScratch);
		this.swiftSearchId = swiftSearchId;
		this.previousSearchRunId = previousSearchRunId;
	}

	public int getSwiftSearchId() {
		return swiftSearchId;
	}

	public int getPreviousSearchRunId() {
		return previousSearchRunId;
	}
}