package edu.mayo.mprc.swift.commands;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ServiceConfig;
import edu.mayo.mprc.daemon.Daemon;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.swift.Swift;
import joptsimple.OptionParser;

import java.io.File;

/**
 * @author Roman Zenka
 */
public interface SwiftEnvironment {
	/**
	 * Runs a Swift command within the Swift environment. The environment changes based on the
	 * command line provided (e.g. current daemon config is set, {@link FileTokenFactory} is set up for it etc.
	 * Then the command is executed.
	 *
	 * @param cmdLine Parsed command line.
	 */
	void runSwiftCommand(final SwiftCommandLine cmdLine);

	/**
	 * @return Parameter (currently only one supported) for the Swift command running.
	 */
	String getParameter();

	/**
	 * User specifies which environment to run within using the --daemon command line switch.
	 * Daemons are configured in the main Swift configuration file, by default in {@link Swift#CONFIG_FILE_NAME}.
	 *
	 * @return Configuration of the current daemon. A daemon specifies a particular environment
	 *         and a list of services to run in that environment.
	 */
	DaemonConfig getDaemonConfig();

	/**
	 * Creates a daemon of given configuration.
	 */
	Daemon createDaemon(DaemonConfig config);

	/**
	 * Create a resource from a given config.
	 *
	 * @param resourceConfig Configuration of the resource to create.
	 * @return Singleton instance of the given resource.
	 */
	Object createResource(ResourceConfig resourceConfig);

	/**
	 * Provides a connection to a runner. The parameter you pass comes from your worker configuration.
	 *
	 * @param service Configuration of a service to connect to.
	 * @return Connection to the runner performing the particular service.
	 */
	DaemonConnection getConnection(ServiceConfig service);

	/**
	 * @return XML configuration file for entire Swift.
	 */
	File getConfigXmlFile();

	/**
	 * @return The parser that was used to parse the command line parameters.
	 */
	OptionParser getOptionParser();
}