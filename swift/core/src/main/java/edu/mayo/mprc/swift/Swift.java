package edu.mayo.mprc.swift;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.ReleaseInfoCore;
import edu.mayo.mprc.utilities.CommandLine;
import edu.mayo.mprc.utilities.FileUtilities;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;

/**
 * Central Swift entry point. Based on the command line options, instantiates either normal
 * or SGE Swift instance and runs it.
 */
public final class Swift {
	private static final Logger LOGGER = Logger.getLogger(Swift.class);
	public static final String CONFIG_FILE_NAME = "conf/swift.xml";
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
		OptionParser parser = new OptionParser();
		parser.accepts("daemon", "Run the daemon of given name - as it was set up during the configuration. When no name is given, the configuration has to contain exactly one daemon, otherwise an error is produced.")
				.withOptionalArg().ofType(String.class).describedAs("name");
		parser.accepts("sge", "Run a single daemon on a single work packet and exit. Used for Sun Grid Engine (SGE).")
				.withRequiredArg().describedAs("XML describing SGE job").ofType(String.class);
		parser.accepts("install", "Installation config file. Default is " + CONFIG_FILE_NAME + ". Please run the Swift configuration to obtain this file.")
				.withRequiredArg().ofType(File.class);
		parser.acceptsAll(Arrays.asList("help", "?"), "Show this help screen");

		OptionSet options = parser.parse(args);
		if (options.has("?")) {
			displayHelpMessage(parser);
			System.exit(EXIT_CODE_OK);
		}
		if (!options.has("daemon") && !options.has("sge")) {
			LOGGER.error("You must specify either the --daemon or --sge option.");
			displayHelpMessage(parser);
			System.exit(EXIT_CODE_ERROR);
		}

		if (options.has("daemon")) {
			SwiftDaemon swiftDaemon = MainFactoryContext.getSwiftDaemon();
			final File installXmlFile = CommandLine.findPropertyFile(options, "install", "installation config file", CONFIG_FILE_NAME, null);
			final String daemonId = (String) options.valueOf("daemon");
			swiftDaemon.runSwiftDaemon(installXmlFile, daemonId);
		} else if (options.has("sge")) {
			SgeJobRunner swiftSge = MainFactoryContext.getSwiftSge();
			final String xmlConfigFilePath = (String) options.valueOf("sge");
			swiftSge.run(new File(xmlConfigFilePath));
		} else {
			displayHelpMessage(parser);
			System.exit(EXIT_CODE_ERROR);
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
