package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.database.DaoTest;
import edu.mayo.mprc.searchdb.dao.SearchDbDaoHibernate;
import edu.mayo.mprc.swift.db.SwiftDaoHibernate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Exercises the Search-db DAO.
 *
 * @author Roman Zenka
 */
public class TestSearchDbDao extends DaoTest {
    private SearchDbDaoHibernate searchDbDao;

    @BeforeMethod
    public void setup() {
        final SwiftDaoHibernate swiftDao = new SwiftDaoHibernate();
        searchDbDao = new SearchDbDaoHibernate();
        searchDbDao.setSwiftDao(swiftDao);
        initializeDatabase(Arrays.asList(swiftDao, searchDbDao));
        searchDbDao.begin();
    }

    @AfterMethod
    public void teardown() {
        searchDbDao.commit();
        teardownDatabase();
    }

    @Test
    public void shouldSaveSmallAnalysis() {

    }
}
