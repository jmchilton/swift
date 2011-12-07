package edu.mayo.mprc.omssa;

import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.searchengine.EngineWorkPacket;

import java.io.File;
import java.util.List;

/**
 * This is a workpacket for requesting an OMSSA search from the {@link OmssaWorker}.
 * <p/>
 * This requires an output file, a mgf file, and a database file as well as a params file that will have these paths inserted into it
 * at placeholder positions.
 */
public final class OmssaWorkPacket extends EngineWorkPacket {
	private static final long serialVersionUID = 20101221L;
	private List<File> databaseRelatedFiles;

	public OmssaWorkPacket(String taskId, boolean fromScratch) {
		super(taskId, fromScratch);
	}

	public OmssaWorkPacket(File outputFile, File searchParamsFile, File mgfFile, File databaseFile, List<File> databaseRelatedFiles, boolean publishSearchFiles, String taskId, boolean fromScratch) {
		super(mgfFile, outputFile, searchParamsFile, databaseFile, publishSearchFiles, taskId, fromScratch);

		this.databaseRelatedFiles = databaseRelatedFiles;
	}

	/**
	 * Although this is never used, we drag the related files with the packet to ensure they get properly uploaded.
	 * Retain this!
	 */
	public List<File> getDatabaseRelatedFiles() {
		return databaseRelatedFiles;
	}

	@Override
	public WorkPacket translateToWorkInProgressPacket(File wipFolder) {
		return new OmssaWorkPacket(
				new File(wipFolder, getOutputFile().getName()),
				getSearchParamsFile(),
				getInputFile(),
				getDatabaseFile(),
				getDatabaseRelatedFiles(),
				isPublishResultFiles(),
				getTaskId(),
				isFromScratch()
		);
	}
}
