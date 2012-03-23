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

	/**
	 * Swift exit codes. Support exiting with the particular code for convenience.
	 */
	public enum ExitCode {
		/**
		 * Successful execution.
		 */
		Ok(0),

		/**
		 * Swift failed.
		 */
		Error(1),

		/**
		 * Swift should restart (the configuration changed).
		 */
		Restart(2);

		private final int exitCode;

		ExitCode(final int exitCode) {
			this.exitCode = exitCode;
		}

		/**
		 * Call {@code System.exit} with this exit code
		 */
		void exit() {
			System.exit(exitCode);
		}
	}

	public static final String CONFIG_FILE_NAME = "conf/swift.xml";

	private Swift() {
	}

	public static void main(String[] args) {
		try {
			MainFactoryContext.initialize();
			runSwift(args);
			// Swift will exist with Ok code if everything is ok
		} catch (Exception t) {
			FileUtilities.err(MprcException.getDetailedMessage(t));
		} finally {
			ExitCode.Error.exit();
		}
	}

	private static void runSwift(String[] args) {
		LOGGER.info(ReleaseInfoCore.infoString());
		CommandLineParser parser = new CommandLineParser(args);
		SwiftCommandLine commandLine = parser.getCommandLine();
		if (commandLine.getError() != null) {
			displayHelpMessage(parser.getOptionParser());
			ExitCode.Error.exit();
		}

		if (SwiftCommandLine.COMMAND_HELP.equals(commandLine.getCommand())) {
			displayHelpMessage(parser.getOptionParser());
			ExitCode.Ok.exit();
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
				ExitCode.Error.exit();
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
			ExitCode.Error.exit();
		}
	}
}
