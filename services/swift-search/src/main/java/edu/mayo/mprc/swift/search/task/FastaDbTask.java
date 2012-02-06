package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.fastadb.FastaDbWorkPacket;

/**
 * Loads a FASTA file into the database.
 *
 * @author Roman Zenka
 */
public class FastaDbTask extends AsyncTaskBase {
    private int curationIdToLoad;

    /**
     * See {@link AsyncTaskBase#AsyncTaskBase(edu.mayo.mprc.daemon.DaemonConnection, edu.mayo.mprc.daemon.files.FileTokenFactory, boolean)}
     */
    public FastaDbTask(DaemonConnection daemon, FileTokenFactory fileTokenFactory, boolean fromScratch, int curationIdToLoad) {
        super(daemon, fileTokenFactory, fromScratch);
        this.curationIdToLoad = curationIdToLoad;
    }

    public int getCurationIdToLoad() {
        return curationIdToLoad;
    }

    public void setCurationIdToLoad(int curationIdToLoad) {
        this.curationIdToLoad = curationIdToLoad;
    }

    @Override
    public WorkPacket createWorkPacket() {
        return new FastaDbWorkPacket(getFullId(), curationIdToLoad);
    }

    @Override
    public void onSuccess() {
    }

    @Override
    public void onProgress(ProgressInfo progressInfo) {
    }
}
