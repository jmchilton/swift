package edu.mayo.mprc.utilities.testing;

import com.google.common.collect.ImmutableMap;
import edu.mayo.mprc.config.ApplicationConfig;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.MultiFactory;
import edu.mayo.mprc.config.ServiceConfig;
import edu.mayo.mprc.daemon.Daemon;
import edu.mayo.mprc.daemon.MessageBroker;
import edu.mayo.mprc.daemon.SimpleRunner;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.database.DatabaseFactory;
import edu.mayo.mprc.database.DatabaseUtilities;
import edu.mayo.mprc.peaks.PeaksMappingFactory;
import edu.mayo.mprc.swift.db.SwiftDao;
import edu.mayo.mprc.swift.params2.ParamsDao;
import edu.mayo.mprc.swift.search.DatabaseValidator;
import edu.mayo.mprc.swift.search.SwiftSearcher;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.workspace.WorkspaceDao;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

/**
 * Support for setting up Spring test context.
 */
public final class TestApplicationContext {
	private static final Logger LOGGER = Logger.getLogger(TestApplicationContext.class);
	private static ApplicationContext testContext = null;

	private TestApplicationContext() {
	}

	/**
	 * This gets a context that overrides swiftContext.xml with properties in testContext.xml.  All tests should use
	 * this context.  If your test dirties the context for some reason then you will need to reset the context using the
	 * AfterClass or AfterTest methods.  Tests should be able to act on the database as it exists in this context and
	 * IF a test dirties the database so it is not usable by other tests (IT SHOULDN'T) then that test needs to drop
	 * and rebuild the database.  An alternative would be to manage the trasaction for the session by starting a new Transaction
	 * before the test and rolling it back after the test.
	 *
	 * @return The context that should be used for testing.
	 */
	public static synchronized ApplicationContext getTestApplicationContext() {
		if (testContext == null) {
			initialize(getInMemoryDatabaseConfig(), DatabaseUtilities.SchemaInitialization.CreateDrop);
		}
		return testContext;
	}

	public static void initialize(final DatabaseFactory.Config databaseConfig, DatabaseUtilities.SchemaInitialization schemaInitialization) {
		System.setProperty("SWIFT_INSTALL",
				new File(System.getenv("SWIFT_HOME"), "install.properties").getAbsolutePath());
		testContext = new ClassPathXmlApplicationContext(new String[]{"/testContext.xml"});

		LOGGER.info("Setting up Test Database.");

		final String fastaFolder = FileUtilities.createTempFolder().getAbsolutePath();
		final String fastaArchiveFolder = FileUtilities.createTempFolder().getAbsolutePath();
		final String fastaUploadFolder = FileUtilities.createTempFolder().getAbsolutePath();
		final String tempFolder = FileUtilities.createTempFolder().getAbsolutePath();

		final DatabaseValidator validator = (DatabaseValidator) getBean("databaseValidator");

		// Create a test application config with one daemon, message broker, database and searcher
		ApplicationConfig testConfig = new ApplicationConfig();

		DaemonConfig daemonConfig = DaemonConfig.getDefaultDaemonConfig("test", true);
		daemonConfig.setTempFolderPath(tempFolder);
		testConfig.addDaemon(daemonConfig);

		MessageBroker.Config messageBrokerConfig = MessageBroker.Config.getEmbeddedBroker();
		daemonConfig.addResource(messageBrokerConfig);

		daemonConfig.addResource(databaseConfig);

		SwiftSearcher.Config searcherConfig = new SwiftSearcher.Config(
				fastaFolder, fastaArchiveFolder, fastaUploadFolder,
				null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, databaseConfig);

		daemonConfig.addService(new ServiceConfig("searcher1", new SimpleRunner.Config(searcherConfig), messageBrokerConfig.getBrokerUrl() + "?simplequeue=searcher1"));

		validator.setDaemonConfig(daemonConfig);
		validator.setSearcherConfig(searcherConfig);
		validator.initialize(
				new ImmutableMap.Builder<String, String>()
						.put("action", DatabaseUtilities.SchemaInitialization.CreateDrop.getValue())
						.build());

		LOGGER.info("Done setting up database.");
	}

	private static DatabaseFactory.Config getInMemoryDatabaseConfig() {
		return new DatabaseFactory.Config(
				"jdbc:h2:mem:test",
				"sa",
				"",
				"org.h2.Driver",
				"org.hibernate.dialect.HSQLDialect",
				"PUBLIC",
				"PUBLIC");
	}

	/**
	 * Returns a bean of a given id using the context returned by {@link #getTestApplicationContext()} ()}.
	 *
	 * @param beanId Bean id we want.
	 * @return The bean for <code>beanId</code>.
	 */
	private static Object getBean(String beanId) {
		return getTestApplicationContext().getBean(beanId);
	}

	/* ============================================================================================================== */

	public static Daemon.Factory getDaemonFactory() {
		return (Daemon.Factory) getBean("daemonFactory");
	}

	public static SwiftDao getSwiftDao() {
		return (SwiftDao) getBean("swiftDao");
	}

	public static WorkspaceDao getWorkspaceDao() {
		return (WorkspaceDao) getBean("workspaceDao");
	}

	public static ParamsDao getParamsDao() {
		return (ParamsDao) getBean("paramsDao");
	}

	public static String getTitle() {
		return (String) getBean("title");
	}

	public static File getMsmsEvalExecutable() {
		return (File) getBean("msmsEvalExecutable");
	}

	public static PeaksMappingFactory getPeaksMappingFactory() {
		return (PeaksMappingFactory) getBean("peaksMappingFactory");
	}

	public static FileTokenFactory getFileTokenFactory() {
		return (FileTokenFactory) getBean("fileTokenFactory");
	}

	public static MultiFactory getResourceTable() {
		return (MultiFactory) getBean("resourceTable");
	}
}
