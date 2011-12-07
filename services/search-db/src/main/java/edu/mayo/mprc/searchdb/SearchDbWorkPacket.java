package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.daemon.WorkPacketBase;

public final class SearchDbWorkPacket extends WorkPacketBase {
	private final int swiftSearchId;

	public SearchDbWorkPacket(int swiftSearchId, String taskId, boolean fromScratch) {
		super(taskId, fromScratch);
		this.swiftSearchId = swiftSearchId;
	}

	public int getSwiftSearchId() {
		return swiftSearchId;
	}
}
