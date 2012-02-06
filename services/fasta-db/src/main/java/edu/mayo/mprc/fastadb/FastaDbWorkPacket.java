package edu.mayo.mprc.fastadb;

import edu.mayo.mprc.daemon.WorkPacketBase;

/**
 * Request to load FASTA database for a curation of a given ID.
 *
 * @author Roman Zenka
 */
public class FastaDbWorkPacket extends WorkPacketBase {

    private int curationId;

    public FastaDbWorkPacket(String taskId, boolean fromScratch) {
        super(taskId, fromScratch);
    }

    /**
     * @param taskId     ID of this task.
     * @param curationId ID of the curation to deploy.
     */
    public FastaDbWorkPacket(String taskId, int curationId) {
        super(taskId, false);
        this.curationId = curationId;
    }

    public int getCurationId() {
        return curationId;
    }

    public void setCurationId(int curationId) {
        this.curationId = curationId;
    }
}
