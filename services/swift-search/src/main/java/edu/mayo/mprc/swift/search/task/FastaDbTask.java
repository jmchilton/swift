package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.dbcurator.model.Curation;
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
    public FastaDbTask(DaemonConnection daemon, FileTokenFactory fileTokenFactory, boolean fromScratch, Curation curationToLoad) {
        super(daemon, fileTokenFactory, fromScratch);
        this.curationIdToLoad = curationToLoad.getId();
        setName("Fasta DB load");
        setDescription("Load " + fileTokenFactory.fileToTaggedDatabaseToken(curationToLoad.getCurationFile()) + " to database.");
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
