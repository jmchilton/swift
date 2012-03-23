package edu.mayo.mprc.swift.commands;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.daemon.Daemon;
import edu.mayo.mprc.swift.Swift;
import edu.mayo.mprc.swift.WebUi;
import edu.mayo.mprc.utilities.FileListener;
import edu.mayo.mprc.utilities.FileMonitor;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * Run the Swift as a daemon.
 *
 * @author Roman Zenka
 */
public class RunSwift implements FileListener, SwiftCommand {
	private static final Logger LOGGER = Logger.getLogger(RunSwift.class);
	public static final String RUN_SWIFT = "run-swift";

	private final CountDownLatch configFileChanged = new CountDownLatch(1);

	@Override
	public String getName() {
		return RUN_SWIFT;
	}

	@Override
	public String getDescription() {
		return "Runs all workers defined for this daemon. This is the default command.";
	}

	/**
	 * Run all workers configured for this daemon.
	 */
	public void run(SwiftEnvironment environment) {
		DaemonConfig config = environment.getDaemonConfig();
		File installXmlFile = environment.getConfigXmlFile();

		checkDoesNotContainWebModule(config);

		Daemon daemon = environment.createDaemon(config);
		LOGGER.debug(daemon.toString());

		startListeningToConfigFileChanges(installXmlFile);

		boolean terminateDaemon = true;

		if (daemon.getNumRunners() > 0) {
			daemon.start();

			try {
				configFileChanged.await();
				// Since the config file changed, we want to restart, not terminate
				terminateDaemon = false;
			} catch (InterruptedException ignore) {
				LOGGER.info("Execution interrupted");
			}
			if (terminateDaemon) {
				LOGGER.info("Stopping the daemon");
				daemon.stop();
			} else {
				LOGGER.info("Clean shutdown of daemon initiated");
				daemon.stop();
				LOGGER.info("Waiting for tasks to be completed");
				daemon.awaitTermination();
			}
			LOGGER.info("Daemon stopped");
		} else {
			throw new MprcException("No daemons are configured in " + installXmlFile.getAbsolutePath() + ". Exiting.");
		}

		if (terminateDaemon) {
			Swift.ExitCode.Ok.exit();
		} else {
			Swift.ExitCode.Restart.exit();
		}
	}

	/**
	 * Setup listener to config file, so when the config file changes, we restart.
	 *
	 * @param installXmlFile File to check.
	 */
	private void startListeningToConfigFileChanges(File installXmlFile) {
		FileMonitor monitor = new FileMonitor(10 * 1000);
		monitor.addFile(installXmlFile);
		monitor.addListener(this);
	}

	/**
	 * Makes sure we are not running a web module as a classical daemon. The web module has to be run using a wrapper.
	 *
	 * @param config Config of the deamon we are trying to run.
	 */
	private static void checkDoesNotContainWebModule(DaemonConfig config) {
		for (ResourceConfig resourceConfig : config.getResources()) {
			if (resourceConfig instanceof WebUi.Config) {
				throw new MprcException("The configuration of daemon '" + config.getName() + "' contains Swift website setup.\n"
						+ "This daemon has to run within a web server. Please use:\n\tswiftWeb --daemon " + config.getName()
						+ "\ninstead of\n\tswift --daemon " + config.getName());
			}
		}
	}

	@Override
	public void fileChanged(File file) {
		configFileChanged.countDown();
	}
}
