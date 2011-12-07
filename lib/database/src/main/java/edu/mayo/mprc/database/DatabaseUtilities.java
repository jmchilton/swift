package edu.mayo.mprc.database;

import edu.mayo.mprc.MprcException;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.*;

public final class DatabaseUtilities {
	private static final Logger LOGGER = Logger.getLogger(DatabaseUtilities.class);

	private DatabaseUtilities() {
	}

	public enum SchemaInitialization {
		/**
		 * Empties database when creating
		 */
		Create("create"),

		/**
		 * Drops when database gets closed, empties when creating
		 */
		CreateDrop("create-drop"),

		Update("update"),

		None("");

		private String value;

		SchemaInitialization(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public static SessionFactory getSessionFactory(String url, String userName, String password,
	                                               String dialect,
	                                               String driverClassName,
	                                               String defaultSchema, String schema,
	                                               Map<String, String> hibernateProperties,
	                                               List<String> mappingResources,
	                                               SchemaInitialization initialization) {
		try {
			Configuration cfg = getHibernateConfiguration(url, userName, password, dialect, driverClassName, defaultSchema,
					schema, hibernateProperties, mappingResources, initialization);
			return cfg.buildSessionFactory();
		} catch (Exception t) {
			throw new MprcException("Could not establish database access", t);
		}
	}

	public static Configuration getHibernateConfiguration(String url, String userName, String password, String dialect,
	                                                      String driverClassName, String defaultSchema, String schema,
	                                                      Map<String, String> hibernateProperties, List<String> mappingResources,
	                                                      SchemaInitialization initialization) {
		Configuration cfg = new Configuration();

		for (String resource : mappingResources) {
			cfg.addResource(resource);
		}

		schemaInitialization(cfg, initialization);

		cfg.setProperty("hibernate.connection.driver_class", driverClassName);
		cfg.setProperty("hibernate.connection.username", userName);
		cfg.setProperty("hibernate.connection.password", password);
		if (dialect != null) {
			cfg.setProperty("hibernate.dialect", dialect);
		}
		if (url != null) {
			cfg.setProperty("hibernate.connection.url", url);
		}
		if (schema != null) {
			cfg.setProperty("hibernate.connection.schema", schema);
		}
		if (defaultSchema != null) {
			cfg.setProperty("hibernate.default_schema", defaultSchema);
		}

		cfg.setNamingStrategy(new SwiftDatabaseNamingStrategy());

		for (Map.Entry<String, String> entry : hibernateProperties.entrySet()) {
			cfg.setProperty(entry.getKey(), entry.getValue());
		}
		return cfg;
	}

	public static void schemaInitialization(Configuration cfg, SchemaInitialization initialization) {
		switch (initialization) {
			case Create:
				cfg.setProperty("hibernate.hbm2ddl.auto", "create");
				break;
			case CreateDrop:
				cfg.setProperty("hibernate.hbm2ddl.auto", "create-drop");
				break;
			case Update:
				cfg.setProperty("hibernate.hbm2ddl.auto", "update");
				break;
			case None:
				cfg.setProperty("hibernate.hbm2ddl.auto", "");
				break;
			default:
				throw new MprcException("Unsupported database initialization operation: " + initialization);
		}
	}

	/**
	 * @param mappingResources List of .hbm.xml files to use for mapping objects.
	 * @return A session factory for a test database.
	 */
	public static Configuration getTestHibernateConfiguration(List<String> mappingResources) {
		LOGGER.debug("Creating test database configuration");
		Map<String, String> hibernateProperties = new HashMap<String, String>();
		hibernateProperties.put("hibernate.show_sql", "false");
		hibernateProperties.put("hibernate.current_session_context_class", "thread");
		hibernateProperties.put("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
		hibernateProperties.put("hibernate.cache.provider_class", "org.hibernate.cache.HashtableCacheProvider");
		return getHibernateConfiguration(
				"jdbc:h2:mem:test", "sa", "", "org.hibernate.dialect.HSQLDialect", "org.h2.Driver", "PUBLIC", "PUBLIC", hibernateProperties, mappingResources,
				SchemaInitialization.CreateDrop);
	}

	public static SessionFactory getTestSessionFactory(List<String> mappingResources) {
		return getTestHibernateConfiguration(mappingResources).buildSessionFactory();
	}

	/**
	 * Turns a set of persistable objects into a list of their ids.
	 */
	public static <T extends PersistableBase> Integer[] getIdList(Set<T> items) {
		Integer[] ids = new Integer[items.size()];
		final Iterator<T> iterator = items.iterator();
		for (int i = 0; i < ids.length; i++) {
			final T item = iterator.next();
			ids[i] = item.getId();
		}
		return ids;
	}


}
