package edu.mayo.mprc.swift;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.ReleaseInfoCore;
import edu.mayo.mprc.swift.commands.DisplayHelp;
import edu.mayo.mprc.swift.commands.SwiftCommandLine;
import edu.mayo.mprc.swift.commands.SwiftEnvironment;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;

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
		public void exit() {
			System.exit(exitCode);
		}
	}

	public static final String CONFIG_FILE_NAME = "conf/swift.xml";

	private Swift() {
	}

	public static void main(final String[] args) {
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

	private static void runSwift(final String[] args) {
		LOGGER.info(ReleaseInfoCore.infoString());
		final CommandLineParser parser = new CommandLineParser(args);
		final SwiftCommandLine commandLine = parser.getCommandLine();
		if (commandLine.getError() != null) {
			FileUtilities.err(commandLine.getError() + "\nUse --" + DisplayHelp.COMMAND + " for more information.");
			ExitCode.Error.exit();
		}

		final SwiftEnvironment swiftEnvironment = MainFactoryContext.getSwiftEnvironment();
		try {
			swiftEnvironment.runSwiftCommand(commandLine);
		} catch (Exception e) {
			LOGGER.error("Error running Swift", e);
			ExitCode.Error.exit();
		}
	}

}
