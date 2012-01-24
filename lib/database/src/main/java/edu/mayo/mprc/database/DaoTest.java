package edu.mayo.mprc.database;

import com.google.common.collect.Lists;
import org.hibernate.SessionFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

/**
 * This is a base class providing utilities to test a Dao.
 * It will setup a fresh in-memory database before every method call.
 */
public abstract class DaoTest {
    private SessionFactory factory;
    private final DatabasePlaceholder databasePlaceholder = new DatabasePlaceholder();

    /**
     * Shortcut for {@link #initializeDatabase(java.util.Collection, String...)}.
     *
     * @param daoToInitialize Single dao to initialize.
     */
    public void initializeDatabase(DaoBase daoToInitialize) {
        initializeDatabase(Arrays.asList(daoToInitialize));
    }

    /**
     * Initializes the database and given DAOs with it.
     *
     * @param daosToInitialize List of DAOs to initialize. The DAOs also list hibernate mapping files needed.
     * @param mappingFiles     Additional mapping files to use.
     */
    public void initializeDatabase(Collection<? extends DaoBase> daosToInitialize, String... mappingFiles) {
        TreeSet<String> strings = new TreeSet<String>();
        Collections.addAll(strings, mappingFiles);

        for (DaoBase daoBase : daosToInitialize) {
            strings.addAll(daoBase.getHibernateMappings());
        }

        factory = DatabaseUtilities.getTestSessionFactory(Lists.newArrayList(strings));
        databasePlaceholder.setSessionFactory(factory);

        for (DaoBase daoBase : daosToInitialize) {
            daoBase.setDatabasePlaceholder(databasePlaceholder);
        }
    }

    /**
     * Closes the current database.
     */
    public void teardownDatabase() {
        factory.close();
    }

    /**
     * @return Current database placeholder if you need to create e.g. an additional DAO.
     */
    public DatabasePlaceholder getDatabasePlaceholder() {
        return databasePlaceholder;
    }

    /**
     * Utility method that will commit the current transaction and immediatelly restart a new one.
     * Useful for tests that need to do some action in multiple transactions.
     */
    public void nextTransaction() {
        databasePlaceholder.flushSession();
        databasePlaceholder.getSession().clear();
        databasePlaceholder.commit();
        databasePlaceholder.begin();
    }
}
