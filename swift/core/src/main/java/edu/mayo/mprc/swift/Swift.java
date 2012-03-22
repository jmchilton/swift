package edu.mayo.mprc.swift;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.ReleaseInfoCore;
import edu.mayo.mprc.utilities.FileUtilities;
import joptsimple.OptionParser;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Central Swift entry point. Based on the command line options, instantiates either normal
 * or SGE Swift instance and runs it.
 */
public final class Swift {
	private static final Logger LOGGER = Logger.getLogger(Swift.class);
	public static final int EXIT_CODE_OK = 0;
	public static final int EXIT_CODE_ERROR = 1;
	public static final int EXIT_CODE_RESTART = 2;

	private Swift() {
	}

	public static void main(String[] args) {
		try {
			MainFactoryContext.initialize();
			runSwift(args);
		} catch (Exception t) {
			FileUtilities.err(MprcException.getDetailedMessage(t));
			System.exit(EXIT_CODE_ERROR);
		} finally {
			System.exit(EXIT_CODE_ERROR);
		}
	}

	private static void runSwift(String[] args) {
		LOGGER.info(ReleaseInfoCore.infoString());
		CommandLineParser parser = new CommandLineParser(args);
		SwiftCommandLine commandLine = parser.getCommandLine();
		if (commandLine.getError() != null) {
			displayHelpMessage(parser.getOptionParser());
			System.exit(EXIT_CODE_ERROR);
		}

		if (SwiftCommandLine.COMMAND_HELP.equals(commandLine.getCommand())) {
			displayHelpMessage(parser.getOptionParser());
			System.exit(EXIT_CODE_OK);
		}

		if (SwiftCommandLine.COMMAND_SGE.equals(commandLine.getCommand())) {
			final SgeJobRunner swiftSge = MainFactoryContext.getSwiftSge();
			final String xmlConfigFilePath = commandLine.getParameter();
			swiftSge.run(new File(xmlConfigFilePath));
		} else {
			final SwiftDaemon swiftDaemon = MainFactoryContext.getSwiftDaemon();
			try {
				swiftDaemon.runSwiftCommand(commandLine);
			} catch (Exception e) {
				LOGGER.error("Error running Swift", e);
				System.exit(EXIT_CODE_ERROR);
			}
		}
	}

	private static void displayHelpMessage(OptionParser parser) {
		try {
			FileUtilities.out(ReleaseInfoCore.infoString());
			FileUtilities.out("");
			FileUtilities.out("This command starts Swift with the provided configuration parameters.");
			FileUtilities.out("If you do not have a configuration file yet, please run Swift's web configuration first.");
			FileUtilities.out("");
			FileUtilities.out("Usage:");
			parser.printHelpOn(System.out);
		} catch (Exception t) {
			LOGGER.fatal("Could not display help message.", t);
			System.exit(1);
		}
	}
}
