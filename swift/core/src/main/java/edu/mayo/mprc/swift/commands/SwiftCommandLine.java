package edu.mayo.mprc.swift.commands;

import joptsimple.OptionParser;

import java.io.File;

/**
 * @author Roman Zenka
 */
public final class SwiftCommandLine {
	public static final String COMMAND_SGE = "sge";
	public static final String DEFAULT_RUN_COMMAND = RunSwift.RUN_SWIFT;

	private final String command;
	private final String parameter;
	private final File installFile;
	private final String daemonId;
	private final String error;
	private final OptionParser parser;

	public SwiftCommandLine(final String command, final String parameter, final File installFile, final String daemonId, final String error, final OptionParser parser) {
		this.command = command;
		this.parameter = parameter;
		this.installFile = installFile;
		this.daemonId = daemonId;
		this.error = error;
		this.parser = parser;
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

	public OptionParser getParser() {
		return parser;
	}
}
