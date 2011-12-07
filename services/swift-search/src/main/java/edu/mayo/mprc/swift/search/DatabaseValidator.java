package edu.mayo.mprc.swift.search;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.ApplicationConfig;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.RuntimeInitializer;
import edu.mayo.mprc.config.ui.FixTag;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.database.DatabaseFactory;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.database.DatabaseUtilities;
import edu.mayo.mprc.database.FileType;
import edu.mayo.mprc.swift.db.FileTokenFactoryWrapper;
import org.hibernate.SessionFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Validates the database. Initializes the database when needed. This is a master class that is responsible
 * for all database-related initialization.
 */
public final class DatabaseValidator implements RuntimeInitializer {

	private List<String> mappingResources;
	private Map<String, String> hibernateProperties;
	private DatabasePlaceholder databasePlaceholder;
	private SwiftSearcher.Config searcherConfig;
	private DaemonConfig daemonConfig;
	private List<RuntimeInitializer> runtimeInitializers;
	private FileTokenFactory fileTokenFactory;
	private final static ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

	/**
	 * Sets up the file token factory. File token factory needs to know which daemon we are running in,
	 * and where is the database module. The database module is located within the config.
	 *
	 * @param daemonConfig Config for the active daemon.
	 */
	public static void setupFileTokenFactory(DaemonConfig daemonConfig, final FileTokenFactory fileTokenFactory) {
		// Setup the actual daemon
		fileTokenFactory.setDaemonConfigInfo(daemonConfig.createDaemonConfigInfo());
		if (daemonConfig.getTempFolderPath() == null) {
			throw new MprcException("The temporary folder is not configured for this daemon. Swift cannot run.");
		}
		fileTokenFactory.setTempFolderRepository(new File(daemonConfig.getTempFolderPath()));

		DaemonConfig databaseDaemonConfig = getDatabaseDaemonConfig(daemonConfig.getApplicationConfig());

		fileTokenFactory.setDatabaseDaemonConfigInfo(databaseDaemonConfig.createDaemonConfigInfo());

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

	/**
	 * Initialize the connection to the database.
	 * <p/>
	 * Initialize the {@link edu.mayo.mprc.daemon.files.FileTokenFactory}.
	 * <p/>
	 * Open a session and a transaction, making everything ready to write into the database.
	 *
	 * @param schemaInitialization How to initialize the database.
	 */
	private void beginTransaction(DatabaseUtilities.SchemaInitialization schemaInitialization) {
		final DatabaseFactory.Config database = searcherConfig.getDatabase();
		final SessionFactory sessionFactory = DatabaseUtilities.getSessionFactory(database.getUrl()
				, database.getUserName()
				, database.getPassword()
				, database.getDialect()
				, database.getDriverClassName()
				, database.getDefaultSchema()
				, database.getSchema()
				, hibernateProperties
				, mappingResources
				, schemaInitialization);

		databasePlaceholder.setSessionFactory(sessionFactory);

		setupFileTokenFactory(daemonConfig, fileTokenFactory);

		databasePlaceholder.begin();
	}

	private void commitTransaction() {
		databasePlaceholder.commit();
	}

	private void rollbackTransaction() {
		databasePlaceholder.rollback();
	}

	@Override
	public String check(Map<String, String> params) {
		final HashMap<String, String> newParams = new HashMap<String, String>(params);
		newParams.put(CurationInitializer.FASTA_FOLDER, searcherConfig.getFastaPath());
		newParams.put(CurationInitializer.FASTA_ARCHIVE_FOLDER, searcherConfig.getFastaArchivePath());

		final Future<String> future = EXECUTOR.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				String errors = "";
				try {
					// No initialization for the check
					beginTransaction(DatabaseUtilities.SchemaInitialization.None);

					String initializationToDo = null;

					// Go through a list of RuntimeInitializer, stop when one of them reports it is not ready
					for (RuntimeInitializer initializer : runtimeInitializers) {
						final String result = initializer.check(newParams);
						databasePlaceholder.getSession().flush();
						if (result != null) {
							initializationToDo = result;
							break;
						}
					}

					if (initializationToDo != null) {
						errors += "Database is not initialized: " + initializationToDo + " - " + FixTag.getTag(
								DatabaseUtilities.SchemaInitialization.Update.getValue(), "Initialize Database");
					}
					commitTransaction();
				} catch (Exception e) {
					errors += "Database connection could not be established.<br/>Error: " + e.getMessage()
							+ "<br/>Database may not exist. " + FixTag.getTag(
							DatabaseUtilities.SchemaInitialization.Create.getValue(), "Create Database");
					rollbackTransaction();
				}

				return "".equals(errors) ? null : errors;
			}
		});

		try {
			return future.get();
		} catch (Exception e) {
			throw new MprcException("Could not check the database", e);
		}
	}

	@Override
	/**
	 * @param params Recognizes "action" key that can be one of
	 * {@link edu.mayo.mprc.database.DatabaseUtilities.SchemaInitialization#getValue()}.
	 */
	public void initialize(Map<String, String> params) {
		final String action = params.get("action");
		final HashMap<String, String> newParams = new HashMap<String, String>(params);
		newParams.put(CurationInitializer.FASTA_FOLDER, searcherConfig.getFastaPath());
		newParams.put(CurationInitializer.FASTA_ARCHIVE_FOLDER, searcherConfig.getFastaArchivePath());

		final Future<?> future = EXECUTOR.submit(new Runnable() {
			@Override
			public void run() {
				try {
					DatabaseUtilities.SchemaInitialization initialization = DatabaseUtilities.SchemaInitialization.Update;
					for (DatabaseUtilities.SchemaInitialization schema : DatabaseUtilities.SchemaInitialization.values()) {
						if (schema.getValue().equals(action)) {
							initialization = schema;
						}
					}

					beginTransaction(initialization);

					for (RuntimeInitializer initializer : runtimeInitializers) {
						initializer.initialize(newParams);
						databasePlaceholder.getSession().flush();
						// We completely wipe out the caches between the initialization steps to prevent
						// huge memory consumption.
						databasePlaceholder.getSession().clear();
					}

					commitTransaction();
				} catch (Exception e) {
					rollbackTransaction();
					throw new MprcException(e);
				}
			}
		});

		try {
			future.get();
		} catch (Exception e) {
			throw new MprcException("Failed to initialize the database", e);
		}
	}

	public SwiftSearcher.Config getSearcherConfig() {
		return searcherConfig;
	}

	public void setSearcherConfig(SwiftSearcher.Config searcherConfig) {
		this.searcherConfig = searcherConfig;
	}

	public DaemonConfig getDaemonConfig() {
		return daemonConfig;
	}

	public void setDaemonConfig(DaemonConfig daemonConfig) {
		this.daemonConfig = daemonConfig;
	}

	public DatabasePlaceholder getDatabasePlaceholder() {
		return databasePlaceholder;
	}

	public void setDatabasePlaceholder(DatabasePlaceholder databasePlaceholder) {
		this.databasePlaceholder = databasePlaceholder;
	}

	public List<String> getMappingResources() {
		return mappingResources;
	}

	public void setMappingResources(List<String> mappingResources) {
		this.mappingResources = mappingResources;
	}

	public Map<String, String> getHibernateProperties() {
		return hibernateProperties;
	}

	public void setHibernateProperties(Map<String, String> hibernateProperties) {
		this.hibernateProperties = hibernateProperties;
	}

	public List<RuntimeInitializer> getRuntimeInitializers() {
		return runtimeInitializers;
	}

	public void setRuntimeInitializers(List<RuntimeInitializer> runtimeInitializers) {
		this.runtimeInitializers = runtimeInitializers;
	}

	public FileTokenFactory getFileTokenFactory() {
		return fileTokenFactory;
	}

	public void setFileTokenFactory(FileTokenFactory fileTokenFactory) {
		this.fileTokenFactory = fileTokenFactory;
	}
}
