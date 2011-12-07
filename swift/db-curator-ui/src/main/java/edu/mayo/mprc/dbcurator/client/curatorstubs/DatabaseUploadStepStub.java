package edu.mayo.mprc.dbcurator.client.curatorstubs;

import edu.mayo.mprc.dbcurator.client.steppanels.AbstractStepPanel;
import edu.mayo.mprc.dbcurator.client.steppanels.DatabaseUploadPanel;

/**
 * @author Eric Winter
 */
public final class DatabaseUploadStepStub extends CurationStepStub {
	private static final long serialVersionUID = 20101221L;

	public String clientFilePath;
	public String serverFilePath;

	public AbstractStepPanel getStepPanel() {
		DatabaseUploadPanel panel = new DatabaseUploadPanel();
		panel.setContainedStep(this);
		return panel;
	}
}
