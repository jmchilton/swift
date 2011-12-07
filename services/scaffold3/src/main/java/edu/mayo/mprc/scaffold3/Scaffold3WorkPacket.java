package edu.mayo.mprc.scaffold3;

import edu.mayo.mprc.daemon.WorkPacketBase;
import edu.mayo.mprc.scafml.ScafmlScaffold;

import java.io.File;

public final class Scaffold3WorkPacket extends WorkPacketBase {
	private static final long serialVersionUID = 20110407L;
	private File outputFolder;
	private String experimentName;
	private ScafmlScaffold scafmlFile;

	public Scaffold3WorkPacket(String taskId, boolean fromScratch) {
		super(taskId, fromScratch);
	}

	public Scaffold3WorkPacket(File outputFolder, ScafmlScaffold scafmlFile, String experimentName, String taskId, boolean fromScratch) {
		super(taskId, fromScratch);

		assert outputFolder != null : "Scaffold request cannot be created: Work folder was null";
		assert scafmlFile != null : "Scaffold request cannot be created: .scafml file was null";

		this.outputFolder = outputFolder;
		this.scafmlFile = scafmlFile;
		this.experimentName = experimentName;
	}

	public File getOutputFolder() {
		return outputFolder;
	}

	public ScafmlScaffold getScafmlFile() {
		return scafmlFile;
	}

	public String getExperimentName() {
		return experimentName;
	}

	@Override
	public void synchronizeFileTokensOnReceiver() {
		uploadAndWait("outputFolder");
	}
}
