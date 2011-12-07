package edu.mayo.mprc.swift.ui.client.rpc;

/**
 * Textual representation of parameter file for previewing to user.
 */
public final class ClientParamFile implements ClientValue {
	private static final long serialVersionUID = 20101221L;
	private String name;
	private String text;

	public ClientParamFile() {
	}

	public ClientParamFile(String name, String text) {
		this.name = name;
		this.text = text;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
