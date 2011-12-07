package edu.mayo.mprc.swift.ui.client.rpc;

import java.io.Serializable;

/**
 * Equivalent of {@link edu.mayo.mprc.msmseval.MSMSEvalParamFile} which is serializable by GWT.
 */
public final class SpectrumQaParamFileInfo implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private String path;
	private String description;

	/**
	 * Null Constructor for serialization.
	 */
	public SpectrumQaParamFileInfo() {
	}

	public SpectrumQaParamFileInfo(String path, String description) {
		this.path = path;
		this.description = description;
	}

	public String getPath() {
		return path;
	}

	public String getDescription() {
		return description;
	}
}
