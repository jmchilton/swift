package edu.mayo.mprc.swift.ui.client.rpc;

public final class ClientExtractMsnSettings implements ClientValue {

	private static final long serialVersionUID = 20121221L;
	private String commandLineSwitches;

	public ClientExtractMsnSettings() {
	}

	public ClientExtractMsnSettings(String commandLineSwitches) {
		this.commandLineSwitches = commandLineSwitches;
	}

	public String getCommandLineSwitches() {
		return commandLineSwitches;
	}

	public void setCommandLineSwitches(String commandLineSwitches) {
		this.commandLineSwitches = commandLineSwitches;
	}
}
