package edu.mayo.mprc.swift;

import edu.mayo.mprc.swift.commands.DisplayHelp;
import edu.mayo.mprc.swift.commands.RunSge;
import edu.mayo.mprc.swift.commands.SwiftCommandLine;
import edu.mayo.mprc.utilities.CommandLine;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;
import java.util.Arrays;

/**
 * A parsed Swift command - what did the user ask Swift to do.
 * <p/>
 * Includes environment setup.
 *
 * @author Roman Zenka
 */
public final class CommandLineParser {
	private OptionParser parser;
	private final SwiftCommandLine commandLine;

	/**
	 * @return Option parser for Swift's command line.
	 */
	OptionParser setupParser() {
		parser = new OptionParser();
		parser.accepts("install", "Installation config file. Default is " + Swift.CONFIG_FILE_NAME + ". Please run the Swift configuration to obtain this file.")
				.withRequiredArg().ofType(File.class);
		parser.accepts("daemon", "Specify the daemon (this describes the environment of the current run) as it was set up during the configuration. When no name is given, the configuration has to contain exactly one daemon, otherwise an error is produced.")
				.withOptionalArg().ofType(String.class).describedAs("name");
		parser.accepts("run", "A Swift command to run. When missing, Swift will run all workers configured for the specified daemon.")
				.withRequiredArg().describedAs("Swift command").ofType(String.class);
		parser.accepts("sge", "Process a single work packet and exit. Used for jobs submitted through the Sun Grid Engine (SGE).")
				.withRequiredArg().describedAs("XML file describing SGE job").ofType(String.class);
		parser.acceptsAll(Arrays.asList("help", "?"), "Show this help screen");
		return parser;
	}

	public CommandLineParser(final String[] args) {
		setupParser();
		final OptionSet options = parser.parse(args);

		String command = null;
		String parameter = null;
		String error = null;

		if (options.has("?")) {
			command = DisplayHelp.COMMAND;
		} else if (!options.has("daemon") && !options.has("sge") && !options.has("run")) {
			error = "You must specify either the --daemon, --sge or --run options.";
			command = DisplayHelp.COMMAND;
		} else if (options.has("sge") && options.has("run")) {
			error = "--sge and --run options are mutually exclusive.";
			command = DisplayHelp.COMMAND;
		} else if (options.has("sge")) {
			command = RunSge.COMMAND;
			parameter = (String) options.valueOf("sge");
		} else {
			if (options.has("run")) {
				final String[] parsedRun = parseRun((String) options.valueOf("run"));
				command = parsedRun[0];
				parameter = parsedRun[1];
			} else {
				command = SwiftCommandLine.DEFAULT_RUN_COMMAND;
				parameter = "";
			}
		}
		File installFile = null;
		if (!DisplayHelp.COMMAND.equals(command)) {
			installFile = CommandLine.findPropertyFile(options, "install", "installation config file", Swift.CONFIG_FILE_NAME, null);
		}
		final String daemonId = (String) options.valueOf("daemon");
		commandLine = new SwiftCommandLine(command, parameter, installFile, daemonId, error, parser);
	}

	/**
	 * Parses given single run options to its parts.
	 *
	 * @param toParse String to parse.
	 */
	public String[] parseRun(final String toParse) {
		final int firstSpace = toParse.indexOf(' ');
		final String[] result = new String[2];
		if (firstSpace >= 0) {
			result[0] = toParse.substring(0, firstSpace).trim();
			result[1] = toParse.substring(firstSpace + 1).trim();
		} else {
			result[0] = toParse.trim();
			result[1] = null;
		}
		return result;
	}

	public SwiftCommandLine getCommandLine() {
		return commandLine;
	}

	public OptionParser getOptionParser() {
		return parser;
	}
}
