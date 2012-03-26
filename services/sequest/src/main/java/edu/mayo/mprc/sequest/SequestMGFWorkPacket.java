package edu.mayo.mprc.sequest;

import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.searchengine.EngineWorkPacket;

import java.io.File;

/**
 * All information Sequest needs to run a search.
 */
public final class SequestMGFWorkPacket extends EngineWorkPacket {
	private static final long serialVersionUID = 20101221L;

	public SequestMGFWorkPacket(final File outputFile, final File searchParamsFile, final File inputFile, final File databaseFile, final boolean publishSearchFiles, final String taskId, final boolean fromScratch) {
		super(inputFile, outputFile, searchParamsFile, databaseFile, publishSearchFiles, taskId, fromScratch);
	}

	@Override
	public WorkPacket translateToWorkInProgressPacket(final File wipFolder) {
		return new SequestMGFWorkPacket(
				new File(wipFolder, getOutputFile().getName()),
				getSearchParamsFile(),
				getInputFile(),
				getDatabaseFile(),
				isPublishResultFiles(),
				getTaskId(),
				isFromScratch()
		);
	}
}
