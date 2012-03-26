package edu.mayo.mprc.scaffold;

import edu.mayo.mprc.daemon.WorkPacketBase;
import edu.mayo.mprc.scafml.ScafmlScaffold;

import java.io.File;

/**
 * Job for scaffold. Scaffold needs several inputs to work:
 * <ul>
 * <li>workFolder - the resulting .sfd is written here</li>
 * <li>scafmlFile - {@link ScafmlScaffold} representing the contents of the .scafml file.</li>
 * </ul>
 */
public final class ScaffoldWorkPacket extends WorkPacketBase {
	private static final long serialVersionUID = 20080804L;
	private File outputFolder;
	private String experimentName;
	private ScafmlScaffold scafmlFile;

	public ScaffoldWorkPacket(final String taskId, final boolean fromScratch) {
		super(taskId, fromScratch);
	}

	public ScaffoldWorkPacket(final File outputFolder, final ScafmlScaffold scafmlFile, final String experimentName, final String taskId, final boolean fromScratch) {
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
