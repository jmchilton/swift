package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.fastadb.FastaDbWorkPacket;
import edu.mayo.mprc.utilities.progress.ProgressInfo;

/**
 * Loads a FASTA file into the database.
 *
 * @author Roman Zenka
 */
public class FastaDbTask extends AsyncTaskBase {
	private int curationIdToLoad;

	/**
	 * Does not require the curation to be loaded at the expense of having uglier description.
	 */
	public FastaDbTask(final DaemonConnection daemon, final FileTokenFactory fileTokenFactory, final boolean fromScratch, final int curationIdToLoad) {
		super(daemon, fileTokenFactory, fromScratch);
		this.curationIdToLoad = curationIdToLoad;
		setName("Fasta DB load");
		setDescription("Load curation #" + curationIdToLoad + " to database.");
	}

	/**
	 * See {@link AsyncTaskBase#AsyncTaskBase(edu.mayo.mprc.daemon.DaemonConnection, edu.mayo.mprc.daemon.files.FileTokenFactory, boolean)}
	 */
	public FastaDbTask(final DaemonConnection daemon, final FileTokenFactory fileTokenFactory, final boolean fromScratch, final Curation curationToLoad) {
		super(daemon, fileTokenFactory, fromScratch);
		this.curationIdToLoad = curationToLoad.getId();
		setName("Fasta DB load");
		setDescription("Load " + fileTokenFactory.fileToTaggedDatabaseToken(curationToLoad.getCurationFile()) + " to database.");
	}

	public int getCurationIdToLoad() {
		return curationIdToLoad;
	}

	public void setCurationIdToLoad(final int curationIdToLoad) {
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
	public void onProgress(final ProgressInfo progressInfo) {
	}
}
