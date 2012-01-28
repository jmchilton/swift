package edu.mayo.mprc.database;

import com.google.common.collect.Lists;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.FactoryBase;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.utilities.exceptions.ExceptionUtilities;
import org.hibernate.SessionFactory;

import java.util.*;

public final class DatabaseFactory extends FactoryBase<ResourceConfig, SessionFactory> {

	public static final String TYPE = "database";
	public static final String NAME = "Database";
	public static final String DESC = "Database for storing information about Swift searches and Swift configuration.<p>The database gets created and initialized through the module that uses it (in this case, the Swift Searcher module).<p><b>Important:</b> Swift Searcher and Swift Website have to run within the same daemon as the database.";
	private Map<String, String> hibernateProperties;
	private List<DaoBase> daoList;
	private DatabasePlaceholder placeholder;

	public DatabaseFactory() {
	}

	/**
	 * Collect all mapping resources from a selection of DAOs and additionally specified mapping files.
	 *
	 * @param daos         List of DAOs.
	 * @param mappingFiles Array of additional mapping files.
	 * @return All resources needed for the DAOs in a list. Each resource listed once.
	 */
	public static ArrayList<String> collectMappingResouces(Collection<? extends DaoBase> daos, String... mappingFiles) {
		TreeSet<String> strings = new TreeSet<String>();
		Collections.addAll(strings, mappingFiles);

		for (DaoBase daoBase : daos) {
			strings.addAll(daoBase.getHibernateMappings());
		}

		return Lists.newArrayList(strings);
	}

	public Map<String, String> getHibernateProperties() {
		return hibernateProperties;
	}

	public void setHibernateProperties(Map<String, String> hibernateProperties) {
		this.hibernateProperties = hibernateProperties;
	}

	public List<DaoBase> getDaoList() {
		return daoList;
	}

	public void setDaoList(List<DaoBase> daoList) {
		this.daoList = daoList;
	}

	public DatabasePlaceholder getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(DatabasePlaceholder placeholder) {
		this.placeholder = placeholder;
	}

	@Override
	public SessionFactory create(ResourceConfig config, DependencyResolver dependencies) {
		if (!(config instanceof Config)) {
			ExceptionUtilities.throwCastException(config, Config.class);
			return null;
		}
		Config localConfig = (Config) config;

		final SessionFactory sessionFactory = DatabaseUtilities.getSessionFactory(localConfig.getUrl()
				, localConfig.getUserName()
				, localConfig.getPassword()
				, localConfig.getDialect()
				, localConfig.getDriverClassName()
				, localConfig.getDefaultSchema()
				, localConfig.getSchema()
				, getHibernateProperties()
				, collectMappingResouces(daoList),
				DatabaseUtilities.SchemaInitialization.None);

		placeholder.setSessionFactory(sessionFactory);

		return sessionFactory;
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {

		private String url;
		private String userName;
		private String password;
		private String driverClassName;
		private String dialect;
		private String defaultSchema;
		private String schema;

		public Config() {
		}

		public Config(String url, String userName, String password, String driverClassName, String dialect, String defaultSchema, String schema) {
			this.url = url;
			this.userName = userName;
			this.password = password;
			this.driverClassName = driverClassName;
			this.dialect = dialect;
			this.defaultSchema = defaultSchema;
			this.schema = schema;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getDriverClassName() {
			return driverClassName;
		}

		public void setDriverClassName(String driverClassName) {
			this.driverClassName = driverClassName;
		}

		public String getDialect() {
			return dialect;
		}

		public void setDialect(String dialect) {
			this.dialect = dialect;
		}

		public String getDefaultSchema() {
			return defaultSchema;
		}

		public void setDefaultSchema(String defaultSchema) {
			this.defaultSchema = defaultSchema;
		}

		public String getSchema() {
			return schema;
		}

		public void setSchema(String schema) {
			this.schema = schema;
		}

		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put("url", url);
			map.put("username", userName);
			map.put("password", password);
			map.put("driverClassName", driverClassName);
			map.put("dialect", dialect);
			map.put("defaultSchema", defaultSchema);
			map.put("schema", schema);
			return map;
		}

		public void load(Map<String, String> values, DependencyResolver resolver) {
			url = values.get("url");
			userName = values.get("username");
			password = values.get("password");
			driverClassName = values.get("driverClassName");
			dialect = values.get("dialect");
			defaultSchema = values.get("defaultSchema");
			schema = values.get("schema");
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		@Override
		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder.nativeInterface("database");
		}
	}
}