package edu.mayo.mprc.swift.ui.client.rpc;

public final class ClientExtractMsnSettings implements ClientValue {

	private static final long serialVersionUID = 20121221L;
	private String commandLineSwitches;
	private String command;

	public ClientExtractMsnSettings() {
	}

	public ClientExtractMsnSettings(final String commandLineSwitches, final String command) {
		this.commandLineSwitches = commandLineSwitches;
		this.command = command;
	}

	public String getCommandLineSwitches() {
		return commandLineSwitches;
	}

	public void setCommandLineSwitches(final String commandLineSwitches) {
		this.commandLineSwitches = commandLineSwitches;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
}
