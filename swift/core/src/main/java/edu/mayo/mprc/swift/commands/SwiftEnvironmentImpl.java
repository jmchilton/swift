package edu.mayo.mprc.swift.commands;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.*;
import edu.mayo.mprc.daemon.Daemon;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.swift.SwiftConfig;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.exceptions.ExceptionUtilities;
import joptsimple.OptionParser;

import java.io.File;
import java.util.List;

/**
 * Swift daemon entrypoint. Obtains a parsed command line option set and runs Swift as a command-line daemon.
 * Alternatively, if a user specifies a different command, Swift will execute that command and exit.
 */
public final class SwiftEnvironmentImpl implements SwiftEnvironment {
	private FileTokenFactory fileTokenFactory;
	private Daemon.Factory daemonFactory;
	private MultiFactory swiftFactory;
	private List<SwiftCommand> commands;

	private DaemonConfig daemonConfig;
	private DependencyResolver dependencyResolver;
	private File configXmlFile;
	private SwiftCommandLine commandLine;

	public SwiftEnvironmentImpl() {
	}

	/**
	 * Load Swift configuration from a given file.
	 *
	 * @param configXmlFile XML config file to load config from.
	 * @param swiftFactory  A factory of all objects supported by Swift.
	 * @return Loaded Swift configuration.
	 */
	private static ApplicationConfig loadSwiftConfig(final File configXmlFile, final MultiFactory swiftFactory) {
		final ApplicationConfig swiftConfig = ApplicationConfig.load(configXmlFile.getAbsoluteFile(), swiftFactory);
		checkConfig(swiftConfig);
		return swiftConfig;
	}

	/**
	 * Validate the config and output all the found errors on the command line.
	 *
	 * @param swiftConfig Config to check.
	 */
	private static void checkConfig(final ApplicationConfig swiftConfig) {
		final List<String> errorList = SwiftConfig.validateSwiftConfig(swiftConfig);
		if (errorList.size() > 0) {
			FileUtilities.err("WARNING: The configuration file has issues, Swift may not function correctly:");
			for (final String error : errorList) {
				FileUtilities.err("\t" + error);
			}
		}
	}

	@Override
	public void runSwiftCommand(final SwiftCommandLine cmdLine) {
		this.commandLine = cmdLine;
		final SwiftCommand command = getCommand(commandLine.getCommand());

		if (command == null) {
			throw new MprcException("Unknown command: " + commandLine.getCommand() + "\nSupported: " + listSupportedCommands());
		}

		configXmlFile = commandLine.getInstallFile();
		final ApplicationConfig swiftConfig = loadSwiftConfig(configXmlFile, getSwiftFactory());
		daemonConfig = SwiftConfig.getUserSpecifiedDaemonConfig(commandLine.getDaemonId(), swiftConfig);
		SwiftConfig.setupFileTokenFactory(swiftConfig, daemonConfig, getFileTokenFactory());

		command.run(this);
	}

	@Override
	public String getParameter() {
		return commandLine.getParameter();
	}

	/**
	 * @param commandName Name of a command to find.
	 * @return The command to be executed or null if no such command exists.
	 */
	private SwiftCommand getCommand(final String commandName) {
		for (final SwiftCommand command : commands) {
			if (command.getName().equalsIgnoreCase(commandName)) {
				return command;
			}
		}
		return null;
	}

	/**
	 * @return A comma-separated list of command names.
	 */
	private String listSupportedCommands() {
		final StringBuilder supportedCommands = new StringBuilder(commands.size() * 10);
		for (final SwiftCommand command : commands) {
			supportedCommands.append(command.getName());
			supportedCommands.append(", ");
		}
		return supportedCommands.substring(0, supportedCommands.length() - 2);
	}

	public FileTokenFactory getFileTokenFactory() {
		return fileTokenFactory;
	}

	public void setFileTokenFactory(final FileTokenFactory fileTokenFactory) {
		this.fileTokenFactory = fileTokenFactory;
	}

	public Daemon.Factory getDaemonFactory() {
		return daemonFactory;
	}

	public void setDaemonFactory(final Daemon.Factory factory) {
		this.daemonFactory = factory;
	}

	public MultiFactory getSwiftFactory() {
		return swiftFactory;
	}

	public void setSwiftFactory(final MultiFactory swiftFactory) {
		dependencyResolver = new DependencyResolver(swiftFactory);
		this.swiftFactory = swiftFactory;
	}

	public List<SwiftCommand> getCommands() {
		return commands;
	}

	public void setCommands(final List<SwiftCommand> commands) {
		this.commands = commands;
	}

	@Override
	public DaemonConfig getDaemonConfig() {
		return daemonConfig;
	}

	@Override
	public Daemon createDaemon(final DaemonConfig config) {
		return daemonFactory.createDaemon(config);
	}

	@Override
	public DaemonConnection getConnection(final ServiceConfig service) {
		final Object singleton = dependencyResolver.createSingleton(service);
		if (singleton instanceof DaemonConnection) {
			return (DaemonConnection) singleton;
		} else {
			ExceptionUtilities.throwCastException(singleton, DaemonConnection.class);
			return null;
		}
	}

	@Override
	public File getConfigXmlFile() {
		return configXmlFile;
	}

	@Override
	public OptionParser getOptionParser() {
		return commandLine.getParser();
	}
}
