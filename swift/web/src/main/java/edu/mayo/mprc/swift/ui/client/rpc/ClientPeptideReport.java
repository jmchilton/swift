package edu.mayo.mprc.swift.ui.client.rpc;

import java.io.Serializable;

/**
 * Class holds information about the different reports enable in a Swift search.
 */
public final class ClientPeptideReport implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private boolean scaffoldPeptideReportEnabled;

	/**
	 * Null Constructor
	 */
	public ClientPeptideReport() {
	}

	public ClientPeptideReport(boolean scaffoldReportEnabled) {
		this.scaffoldPeptideReportEnabled = scaffoldReportEnabled;
	}

	public boolean isScaffoldPeptideReportEnabled() {
		return scaffoldPeptideReportEnabled;
	}

	public void setScaffoldPeptideReportEnabled(boolean scaffoldPeptideReportEnabled) {
		this.scaffoldPeptideReportEnabled = scaffoldPeptideReportEnabled;
	}
}
