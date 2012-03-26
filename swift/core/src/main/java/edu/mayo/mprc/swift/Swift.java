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
			LOGGER.error(MprcException.getDetailedMessage(e));
			ExitCode.Error.exit();
		}
	}

}
