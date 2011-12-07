package edu.mayo.mprc.swift.ui.client.widgets;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class AdditionalSettingsPanel extends HorizontalPanel {
	private final CheckBox publicMgfs;
	private final CheckBox publicSearchFiles;
	private final CheckBox fromScratch;

	/**
	 * Null Constructor
	 */
	public AdditionalSettingsPanel() {
		this(false, false);
	}

	public AdditionalSettingsPanel(boolean publicMgfs, boolean publicSearchFiles) {
		this.publicMgfs = new CheckBox("Provide .mgf files");
		this.publicMgfs.setChecked(publicMgfs);
		this.add(this.publicMgfs);

		this.publicSearchFiles = new CheckBox("Provide intermediate search results");
		this.publicSearchFiles.setChecked(publicSearchFiles);
		this.add(this.publicSearchFiles);

		this.fromScratch = new CheckBox("From scratch (no cache)");
		this.fromScratch.setChecked(false);
		this.add(this.fromScratch);
	}

	public void setPublicMgfs(boolean publicMgfs) {
		this.publicMgfs.setChecked(publicMgfs);
	}

	public boolean isPublicMgfs() {
		return publicMgfs.isChecked();
	}

	public void setPublicSearchFiles(boolean publicSearchFiles) {
		this.publicSearchFiles.setChecked(publicSearchFiles);
	}

	public boolean isPublicSearchFiles() {
		return publicSearchFiles.isChecked();
	}

	public void setFromScratch(boolean fromScratch) {
		this.fromScratch.setChecked(fromScratch);
	}

	public boolean isFromScratch() {
		return fromScratch.isChecked();
	}
}
