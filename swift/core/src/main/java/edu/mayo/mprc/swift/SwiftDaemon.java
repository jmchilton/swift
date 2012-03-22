package edu.mayo.mprc.swift;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.*;
import edu.mayo.mprc.daemon.Daemon;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.MessageBroker;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.database.DatabaseFactory;
import edu.mayo.mprc.database.FileType;
import edu.mayo.mprc.filesharing.jms.JmsFileTransferHandlerFactory;
import edu.mayo.mprc.mascot.MascotDeploymentService;
import edu.mayo.mprc.mascot.MascotWorker;
import edu.mayo.mprc.omssa.OmssaDeploymentService;
import edu.mayo.mprc.omssa.OmssaWorker;
import edu.mayo.mprc.peaks.PeaksDeploymentService;
import edu.mayo.mprc.peaks.PeaksWorker;
import edu.mayo.mprc.qa.RAWDumpWorker;
import edu.mayo.mprc.scaffold.ScaffoldWorker;
import edu.mayo.mprc.sequest.SequestDeploymentService;
import edu.mayo.mprc.sequest.SequestWorker;
import edu.mayo.mprc.swift.db.FileTokenFactoryWrapper;
import edu.mayo.mprc.swift.search.SwiftSearcher;
import edu.mayo.mprc.utilities.FileListener;
import edu.mayo.mprc.utilities.FileMonitor;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.xtandem.XTandemDeploymentService;
import edu.mayo.mprc.xtandem.XTandemWorker;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Swift daemon entrypoint. Obtains a parsed command line option set and runs Swift as a command-line daemon.
 */
public final class SwiftDaemon implements FileListener {
	private static final Logger LOGGER = Logger.getLogger(SwiftDaemon.class);
	public static final String LOAD_TO_SEARCH_DB = "load-to-search-db";

	private final CountDownLatch configFileChanged = new CountDownLatch(1);
	private FileTokenFactory fileTokenFactory;
	private Daemon.Factory daemonFactory;
	private MultiFactory swiftFactory;

	/**
	 * Runs the given daemon from the given install file. If the daemonId is null,
	 * and there is only one daemon specified, it gets run, otherwise exception is thrown.
	 *
	 * @param commandLine Parsed command line.
	 */
	public void runSwiftCommand(SwiftCommandLine commandLine) {
		final ApplicationConfig swiftConfig = loadSwiftConfig(commandLine.getInstallFile());

		DaemonConfig config = getUserSpecifiedDaemonConfig(commandLine.getDaemonId(), swiftConfig);

		setupFileTokenFactory(swiftConfig, config, getFileTokenFactory());

		if (SwiftCommandLine.COMMAND_RUN_SWIFT.equalsIgnoreCase(commandLine.getCommand())) {
			startSwiftDaemonCommand(config, commandLine.getInstallFile());
		} else if (LOAD_TO_SEARCH_DB.equalsIgnoreCase(commandLine.getCommand())) {
			loadToSearchDbCommand(config, commandLine.getParameter());
		} else {
			throw new MprcException("Unknown command: " + commandLine.getCommand() + "\nSupported: " + SwiftCommandLine.COMMAND_RUN_SWIFT + ", " + LOAD_TO_SEARCH_DB);
		}
	}

	/**
	 * Run all workers configured for this daemon.
	 *
	 * @param config         Daemon configuration.
	 * @param installXmlFile The config file (we watch this for changes to enable restarts)
	 */
	private void startSwiftDaemonCommand(DaemonConfig config, File installXmlFile) {
		checkDoesNotContainWebModule(config);

		Daemon daemon = getDaemonFactory().createDaemon(config);
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

		System.exit(terminateDaemon ? Swift.EXIT_CODE_OK : Swift.EXIT_CODE_RESTART);
	}

	/**
	 * Load given search results into the database.
	 * This is equivalent to a "shortened" Swift search that:
	 * 1) dumps .RAW metadata
	 * 2) dumps Scaffold spectrum report (if missing) using Scaffold 3
	 * 3) loads the FASTA database
	 * 4) loads the Scaffold dump
	 *
	 * @param params The command parameters.
	 */
	private void loadToSearchDbCommand(DaemonConfig config, String params) {
		try {
			final List<ResourceConfig> searchers = config.getApplicationConfig().getModulesOfConfigType(SwiftSearcher.Config.class);
			if (searchers.size() != 1) {
				throw new MprcException("More than one Swift Searcher defined in this Swift install");
			}
			final SwiftSearcher.Config searcherConfig = (SwiftSearcher.Config) searchers.get(0);
			DependencyResolver dependencyResolver = new DependencyResolver(swiftFactory);

			if (searcherConfig.getRawdump() == null) {
				throw new MprcException("The swift searcher does not define a " + RAWDumpWorker.NAME + " module");
			}

			DaemonConnection rawDump = (DaemonConnection) dependencyResolver.createSingleton(searcherConfig.getRawdump());

		} catch (Exception e) {
			throw new MprcException("Could not load into Swift search database", e);
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

	/**
	 * Returns the daemon the user asked us to run. If the user did not specify anything, and there is just one
	 * daemon defined, we return that one. Otherwise we log an error and exit.
	 *
	 * @param daemonId    ID of the daemon to run.
	 * @param swiftConfig Current swift config.
	 * @return Configuration of the daemon the user wants to run.
	 */
	private static DaemonConfig getUserSpecifiedDaemonConfig(String daemonId, ApplicationConfig swiftConfig) {
		if (daemonId == null) {
			// The user did not specify daemon name. If there is only one daemon defined, that is fine - we
			// will run that one. Otherwise complain.
			if (swiftConfig.getDaemons().size() > 1) {
				StringBuilder builder = new StringBuilder();
				for (DaemonConfig daemonConfig : swiftConfig.getDaemons()) {
					builder.append("'");
					builder.append(daemonConfig.getName());
					builder.append("', ");
				}
				builder.setLength(builder.length() - 2);
				throw new MprcException("There is more than one daemon specified in this configuration.\n"
						+ "Run Swift with --daemon set to one of: " + builder.toString());
			}
			daemonId = swiftConfig.getDaemons().get(0).getName();
		}
		return swiftConfig.getDaemonConfig(daemonId);
	}

	private ApplicationConfig loadSwiftConfig(File installXmlFile) {
		final ApplicationConfig swiftConfig = ApplicationConfig.load(installXmlFile.getAbsoluteFile(), getSwiftFactory());
		checkConfig(swiftConfig);
		return swiftConfig;
	}

	/**
	 * Sets up the file token factory. File token factory needs to know which daemon we are running in,
	 * and where is the database module. The database module is located within the config.
	 *
	 * @param swiftConfig  Complete Swift config.
	 * @param daemonConfig Config for the active daemon.
	 */
	public static void setupFileTokenFactory(ApplicationConfig swiftConfig, DaemonConfig daemonConfig, final FileTokenFactory fileTokenFactory) {
		// Setup the actual daemon
		fileTokenFactory.setDaemonConfigInfo(daemonConfig.createDaemonConfigInfo());
		if (daemonConfig.getTempFolderPath() == null) {
			throw new MprcException("The temporary folder is not configured for this daemon. Swift cannot run.");
		}
		fileTokenFactory.setTempFolderRepository(new File(daemonConfig.getTempFolderPath()));

		DaemonConfig databaseDaemonConfig = getDatabaseDaemonConfig(swiftConfig);

		fileTokenFactory.setDatabaseDaemonConfigInfo(databaseDaemonConfig.createDaemonConfigInfo());

		List<ResourceConfig> brokerConfigs = swiftConfig.getModulesOfConfigType(MessageBroker.Config.class);
		if (brokerConfigs.size() > 0) {
			try {
				fileTokenFactory.setFileSharingFactory(new JmsFileTransferHandlerFactory(new URI(brokerConfigs.get(0).save(null).get(MessageBroker.BROKER_URL))));
			} catch (URISyntaxException e) {
				throw new MprcException("Failed to set FileTransferHandlerFactory in FileTokenFactory object.", e);
			}
		} else {
			throw new MprcException("Swift cannot run without a message broker.");
		}

		FileType.initialize(new FileTokenFactoryWrapper(fileTokenFactory));
	}

	/**
	 * Returns a config for a daemon that contains the database. There must be exactly one such daemon.
	 *
	 * @param swiftConfig Swift configuration.
	 * @return Daemon that contains the database module.
	 */
	private static DaemonConfig getDatabaseDaemonConfig(ApplicationConfig swiftConfig) {
		final ResourceConfig databaseResource = getDatabaseResource(swiftConfig);
		return swiftConfig.getDaemonForResource(databaseResource);
	}

	private static ResourceConfig getDatabaseResource(ApplicationConfig swiftConfig) {
		List<ResourceConfig> configs = swiftConfig.getModulesOfConfigType(DatabaseFactory.Config.class);
		if (configs.size() > 1) {
			throw new MprcException("Swift has more than one database defined.");
		}
		if (configs.size() == 0) {
			throw new MprcException("Swift does not define a database.");
		}
		return configs.get(0);
	}

	private static void checkConfig(ApplicationConfig swiftConfig) {
		final List<String> errorList = validateSwiftConfig(swiftConfig);
		if (errorList.size() > 0) {
			FileUtilities.err("WARNING: The configuration file has issues, Swift may not function correctly:");
			for (String error : errorList) {
				FileUtilities.err("\t" + error);
			}
		}
	}

	private static boolean isDifferentDaemon(ApplicationConfig swift, ResourceConfig config1, ResourceConfig config2) {
		if (config1 == null || config2 == null) {
			return false;
		}
		DaemonConfig daemon1 = swift.getDaemonForResource(config1);
		if (daemon1 == null) {
			return false;
		}
		DaemonConfig daemon2 = swift.getDaemonForResource(config2);
		return !daemon1.equals(daemon2);
	}

	/**
	 * Validates the config, returns a list of discovered errors.
	 */
	public static List<String> validateSwiftConfig(ApplicationConfig swift) {
		List<String> errors = new ArrayList<String>();
		// Make sure we have the essential modules
		if (swift.getModulesOfConfigType(SwiftSearcher.Config.class).size() == 0) {
			errors.add("Without " + SwiftSearcher.NAME + " module you will not be able to run any Swift searches.");
		}
		if (swift.getModulesOfConfigType(WebUi.Config.class).size() == 0) {
			errors.add("Without " + WebUi.NAME + " modules you will not be able to interact with Swift.");
		}
		final int numDbs = swift.getModulesOfConfigType(DatabaseFactory.Config.class).size();
		if (numDbs == 0) {
			errors.add("Without " + DatabaseFactory.NAME + " module, Swift will not be able to function.");
		}
		if (numDbs > 1) {
			errors.add("Swift cannot currently be configured with more than one " + DatabaseFactory.NAME + " module.");
		}
		if (swift.getModulesOfConfigType(MessageBroker.Config.class).size() == 0) {
			errors.add("Without " + MessageBroker.NAME + " module, other Swift modules will not be able to communicate with each other.");
		}

		// Make sure that modules that have to be within one daemon are within one daemon
		// Currently the web ui has to be at the same place as searcher it links to
		// A searcher has to be at the same place as the database it links to
		for (ResourceConfig config : swift.getModulesOfConfigType(SwiftSearcher.Config.class)) {
			final SwiftSearcher.Config searcher = (SwiftSearcher.Config) config;
			final ResourceConfig database = searcher.getDatabase();
			if (database == null) {
				errors.add("Each " + SwiftSearcher.NAME + " has to reference a " + DatabaseFactory.NAME + " module that is within the same daemon.");
			} else {
				if (isDifferentDaemon(swift, database, searcher)) {
					errors.add("Each " + SwiftSearcher.NAME + " must be located in the same daemon as the " + DatabaseFactory.NAME + " it refers to.");
					break;
				}
			}
		}

		for (ResourceConfig config : swift.getModulesOfConfigType(WebUi.Config.class)) {
			final WebUi.Config ui = (WebUi.Config) config;
			final ServiceConfig searcher = ui.getSearcher();
			if (searcher == null) {
				errors.add("Each " + WebUi.NAME + " has to reference a " + SwiftSearcher.NAME + " module that is within the same daemon.");
			} else {
				if (isDifferentDaemon(swift, searcher, ui)) {
					errors.add("Each " + WebUi.NAME + " must be located in the same daemon as the " + SwiftSearcher.NAME + " it refers to.");
					break;
				}
			}
		}

		// Make sure that coupled modules (deployer + search engine) referenced by a single searcher are either both defined or both are not
		for (ResourceConfig config : swift.getModulesOfConfigType(SwiftSearcher.Config.class)) {
			final SwiftSearcher.Config searcher = (SwiftSearcher.Config) config;
			checkEngineDeployer(errors, searcher.getMascot(), searcher.getMascotDeployer(), MascotWorker.NAME, MascotDeploymentService.NAME);
			checkEngineDeployer(errors, searcher.getSequest(), searcher.getSequestDeployer(), SequestWorker.NAME, SequestDeploymentService.NAME);
			checkEngineDeployer(errors, searcher.getTandem(), searcher.getTandemDeployer(), XTandemWorker.NAME, XTandemDeploymentService.NAME);
			checkEngineDeployer(errors, searcher.getPeaks(), searcher.getPeaksDeployer(), OmssaWorker.NAME, OmssaDeploymentService.NAME);
			checkEngineDeployer(errors, searcher.getOmssa(), searcher.getOmssaDeployer(), PeaksWorker.NAME, PeaksDeploymentService.NAME);
			checkEngineDeployer(errors, searcher.getScaffold(), searcher.getScaffoldDeployer(), ScaffoldWorker.NAME, SequestDeploymentService.NAME);
		}

		return errors;
	}

	private static void checkEngineDeployer(List<String> errors, ServiceConfig searchEngine, ServiceConfig deployer, String engineName, String deployerName) {
		if (searchEngine != null && deployer == null) {
			errors.add(engineName + " needs to have a corresponding " + deployerName + " defined.");
		}
	}

	@Override
	public void fileChanged(File file) {
		configFileChanged.countDown();
	}

	public FileTokenFactory getFileTokenFactory() {
		return fileTokenFactory;
	}

	public void setFileTokenFactory(FileTokenFactory fileTokenFactory) {
		this.fileTokenFactory = fileTokenFactory;
	}

	public Daemon.Factory getDaemonFactory() {
		return daemonFactory;
	}

	public void setDaemonFactory(Daemon.Factory factory) {
		this.daemonFactory = factory;
	}

	public MultiFactory getSwiftFactory() {
		return swiftFactory;
	}

	public void setSwiftFactory(MultiFactory swiftFactory) {
		this.swiftFactory = swiftFactory;
	}

}
