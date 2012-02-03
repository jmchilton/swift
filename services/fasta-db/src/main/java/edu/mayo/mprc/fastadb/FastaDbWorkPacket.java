package edu.mayo.mprc.fastadb;

import edu.mayo.mprc.daemon.WorkPacketBase;

/**
 * Request to load FASTA database for a curation of a given ID.
 *
 * @author Roman Zenka
 */
public class FastaDbWorkPacket extends WorkPacketBase {

    private int curationId;

    /**
     * @param taskId      Task identifier to be used for nested diagnostic context when logging.
     * @param fromScratch Do all the work from scratch, do not rely on cached values.
     */
    public FastaDbWorkPacket(String taskId, boolean fromScratch) {
        super(taskId, fromScratch);
    }

    public int getCurationId() {
        return curationId;
    }

    public void setCurationId(int curationId) {
        this.curationId = curationId;
    }
}
