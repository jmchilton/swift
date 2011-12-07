package edu.mayo.mprc.database;

import org.hibernate.SessionFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory instance of a database that defines specified mappings.
 */
public final class TestDatabase {
	private SessionFactory factory;

	public TestDatabase(List<String> mappingResources, DatabaseUtilities.SchemaInitialization initialization) {
		Map<String, String> props = new HashMap<String, String>();
		props.put("hibernate.show_sql", "true");
		props.put("hibernate.current_session_context_class", "thread");
		props.put("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");

		factory = DatabaseUtilities.getSessionFactory(
				"jdbc:h2:mem:test",
				"sa",
				"",
				"org.hibernate.dialect.H2Dialect",
				"org.h2.Driver",
				"PUBLIC",
				"PUBLIC",
				props,
				mappingResources,
				initialization);
	}

	public void setupDatabasePlaceholder(DatabasePlaceholder p) {
		p.setSessionFactory(getSessionFactory());
	}

	public SessionFactory getSessionFactory() {
		return factory;
	}
}
