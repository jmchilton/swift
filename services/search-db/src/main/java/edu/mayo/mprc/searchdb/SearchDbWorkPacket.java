package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.daemon.WorkPacketBase;

/**
 * Command to load search results for Swift search of a given ID.
 */
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
