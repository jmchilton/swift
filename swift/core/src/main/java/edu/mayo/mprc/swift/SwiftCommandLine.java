package edu.mayo.mprc.swift;

import java.io.File;

/**
 * @author Roman Zenka
 */
public final class SwiftCommandLine {
	public static final String COMMAND_HELP = "help";
	public static final String COMMAND_SGE = "sge";
	public static final String COMMAND_RUN_SWIFT = "run-swift";

	private final String command;
	private final String parameter;
	private final File installFile;
	private final String daemonId;
	private final String error;

	public SwiftCommandLine(String command, String parameter, File installFile, String daemonId, String error) {
		this.command = command;
		this.parameter = parameter;
		this.installFile = installFile;
		this.daemonId = daemonId;
		this.error = error;
	}

	public String getCommand() {
		return command;
	}

	public String getParameter() {
		return parameter;
	}

	public File getInstallFile() {
		return installFile;
	}

	public String getDaemonId() {
		return daemonId;
	}

	public String getError() {
		return error;
	}
}
