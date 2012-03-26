package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.mascot.MascotResultUrl;
import edu.mayo.mprc.mascot.MascotWorkPacket;
import edu.mayo.mprc.myrimatch.MyrimatchDeploymentResult;
import edu.mayo.mprc.myrimatch.MyrimatchWorkPacket;
import edu.mayo.mprc.omssa.OmssaWorkPacket;
import edu.mayo.mprc.peaks.PeaksWorkPacket;
import edu.mayo.mprc.searchengine.SearchEngineResult;
import edu.mayo.mprc.sequest.SequestMGFWorkPacket;
import edu.mayo.mprc.swift.db.SearchEngine;
import edu.mayo.mprc.utilities.progress.ProgressInfo;
import edu.mayo.mprc.xtandem.XTandemWorkPacket;

import java.io.File;

/**
 * A search on one of the saerch engines.
 */
final class EngineSearchTask extends AsyncTaskBase {
	private SearchEngine engine;
	private MgfOutput mgfOutput;
	private DatabaseDeploymentResult deploymentResult;
	private File outputFile;
	private File paramsFile;
	/**
	 * When true, the intermediate search files are provided for the user. In case of caching
	 * the intermediates, the file is also copied to the resulting directory. When the cache
	 * is not enabled, this parameter has no effect, as the files are already published.
	 */
	private boolean publicSearchFiles;

	public EngineSearchTask(
			final SearchEngine engine,
			final String searchId,
			final MgfOutput mgfOutput,
			final DatabaseDeploymentResult deploymentResult,
			final File outputFile,
			final File paramsFile,
			final boolean publicSearchFiles,
			final DaemonConnection searchEngineDaemon, final FileTokenFactory fileTokenFactory, final boolean fromScratch) {
		super(searchEngineDaemon, fileTokenFactory, fromScratch);
		this.engine = engine;
		this.outputFile = outputFile;
		this.paramsFile = paramsFile;
		this.mgfOutput = mgfOutput;
		this.deploymentResult = deploymentResult;
		this.publicSearchFiles = publicSearchFiles;
		setName(engine.getFriendlyName() + " search");
		setDescription(engine.getFriendlyName() + " search: " + searchId);
	}

	public File getOutputFile() {
		return outputFile;
	}

	public SearchEngine getSearchEngine() {
		return engine;
	}

	/**
	 * @return Work packet to be sent asynchronously. If it returns null, it means the work was done without a need
	 *         to send a work packet.
	 */
	public WorkPacket createWorkPacket() {
		updateDescription(null);

		WorkPacket workPacket = null;
		if ("MASCOT".equalsIgnoreCase(engine.getCode())) {
			workPacket = new MascotWorkPacket(
					outputFile,
					paramsFile,
					mgfOutput.getFilteredMgfFile(),
					deploymentResult.getShortDbName(),
					this.getFullId(),
					isFromScratch(),
					publicSearchFiles);
		} else if ("SEQUEST".equalsIgnoreCase(engine.getCode())) {
			workPacket = new SequestMGFWorkPacket(
					outputFile,
					paramsFile,
					mgfOutput.getFilteredMgfFile(),
					getSequestDatabase(),
					publicSearchFiles,
					this.getFullId(),
					isFromScratch());
		} else if ("TANDEM".equalsIgnoreCase(engine.getCode())) {
			workPacket = new XTandemWorkPacket(
					mgfOutput.getFilteredMgfFile(),
					paramsFile,
					outputFile,
					outputFile.getParentFile(),
					deploymentResult.getFastaFile(),
					publicSearchFiles,
					this.getFullId(),
					isFromScratch());
		} else if ("OMSSA".equalsIgnoreCase(engine.getCode())) {
			workPacket = new OmssaWorkPacket(
					outputFile,
					paramsFile,
					mgfOutput.getFilteredMgfFile(),
					deploymentResult.getFastaFile(),
					deploymentResult.getGeneratedFiles(),
					publicSearchFiles,
					this.getFullId(),
					isFromScratch());
		} else if ("PEAKS".equalsIgnoreCase(engine.getCode())) {
			workPacket = new PeaksWorkPacket(this.getFullId(), isFromScratch(), paramsFile, mgfOutput.getFilteredMgfFile());
		} else if ("MYRIMATCH".equalsIgnoreCase(engine.getCode())) {
			final MyrimatchDeploymentResult myrimatchDeploymentResult = (MyrimatchDeploymentResult) deploymentResult.getDeploymentResult();
			workPacket = new MyrimatchWorkPacket(
					mgfOutput.getFilteredMgfFile(),
					paramsFile,
					outputFile,
					outputFile.getParentFile(),
					deploymentResult.getFastaFile(),
					myrimatchDeploymentResult.getNumForwardEntries(),
					myrimatchDeploymentResult.getDecoySequencePrefix(),
					publicSearchFiles, this.getFullId(),
					isFromScratch()
			);
		} else {
			throw new MprcException("Unsupported engine: " + engine.getFriendlyName());
		}
		return workPacket;
	}

	/**
	 * @return Either Sequest .hdr file (when database got indexed) or the .fasta file (no deployment done).
	 */
	private File getSequestDatabase() {
		return deploymentResult.getSequestHdrFile() == null ? deploymentResult.getFastaFile() : deploymentResult.getSequestHdrFile();
	}

	private void updateDescription(final String mascotResultLink) {
		setDescription(engine.getFriendlyName() + " search: "
				+ fileTokenFactory.fileToTaggedDatabaseToken(mgfOutput.getFilteredMgfFile())
				+ " params: " + fileTokenFactory.fileToTaggedDatabaseToken(paramsFile)
				+ " db: " + getDeployedDatabaseFile(engine, deploymentResult)
				+ (mascotResultLink != null ? " <a href=\"" + mascotResultLink + "\">Open in Mascot</a>" : ""));
	}

	private String getDeployedDatabaseFile(final SearchEngine engine, final DatabaseDeploymentResult deploymentResult) {
		if ("MASCOT".equalsIgnoreCase(engine.getCode())) {
			return deploymentResult.getShortDbName();
		} else if ("SEQUEST".equalsIgnoreCase(engine.getCode())) {
			return getSequestDatabase().getAbsolutePath();
		} else if ("TANDEM".equalsIgnoreCase(engine.getCode())) {
			return deploymentResult.getShortDbName();
		} else if ("OMSSA".equalsIgnoreCase(engine.getCode())) {
			// TODO: OMSSA might be deploying other file types
			return deploymentResult.getShortDbName();
		} else if ("MYRIMATCH".equalsIgnoreCase(engine.getCode())) {
			return deploymentResult.getShortDbName();
		} else {
			throw new MprcException("Unsupported engine: " + engine.getFriendlyName());
		}
	}

	public void onSuccess() {
		completeWhenFilesAppear(outputFile);
	}

	public void onProgress(final ProgressInfo progressInfo) {
		// The search engine produced the output file at a different location than where we asked it to
		if (progressInfo instanceof SearchEngineResult) {
			final SearchEngineResult searchEngineResult = (SearchEngineResult) progressInfo;
			outputFile = searchEngineResult.getResultFile();
		} else if (progressInfo instanceof MascotResultUrl) {
			final MascotResultUrl mascotResultUrl = (MascotResultUrl) progressInfo;
			updateDescription(mascotResultUrl.getMascotUrl());
		}

	}
}
