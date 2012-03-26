package edu.mayo.mprc.swift.commands;

import edu.mayo.mprc.swift.SgeJobRunner;

import java.io.File;

/**
 * @author Roman Zenka
 */
public class RunSge implements SwiftCommand {
	public static final String COMMAND = "sge";
	private SgeJobRunner swiftSge;

	@Override
	public String getName() {
		return COMMAND;
	}

	@Override
	public String getDescription() {
		return "Internal command to execute a piece of work within SGE";
	}

	@Override
	public void run(final SwiftEnvironment environment) {
		final String xmlConfigFilePath = environment.getParameter();
		swiftSge.run(new File(xmlConfigFilePath));
	}

	public SgeJobRunner getSwiftSge() {
		return swiftSge;
	}

	public void setSwiftSge(final SgeJobRunner swiftSge) {
		this.swiftSge = swiftSge;
	}
}
