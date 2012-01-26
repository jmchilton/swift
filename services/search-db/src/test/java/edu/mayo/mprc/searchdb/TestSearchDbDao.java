package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.database.DaoTest;
import edu.mayo.mprc.searchdb.dao.Analysis;
import edu.mayo.mprc.searchdb.dao.SearchDbDaoHibernate;
import edu.mayo.mprc.swift.db.SwiftDaoHibernate;
import edu.mayo.mprc.swift.params2.ParamsDaoHibernate;
import edu.mayo.mprc.unimod.MockUnimodDao;
import edu.mayo.mprc.unimod.Unimod;
import edu.mayo.mprc.unimod.UnimodDaoHibernate;
import edu.mayo.mprc.utilities.ResourceUtilities;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.Reader;
import java.util.Arrays;

/**
 * Exercises the Search-db DAO.
 *
 * @author Roman Zenka
 */
public class TestSearchDbDao extends DaoTest {
    private SearchDbDaoHibernate searchDbDao;
    private final MockUnimodDao mockUnimodDao = new MockUnimodDao();
    private Unimod unimod;
    private Unimod scaffoldUnimod;

    private static final String SINGLE = "classpath:edu/mayo/mprc/searchdb/single.tsv";

    @BeforeMethod
    public void setup() {
        unimod = mockUnimodDao.load();
        scaffoldUnimod = new Unimod();
        scaffoldUnimod.parseUnimodXML(ResourceUtilities.getStream("classpath:edu/mayo/mprc/searchdb/scaffold_unimod.xml", Unimod.class));

        final SwiftDaoHibernate swiftDao = new SwiftDaoHibernate();
        final UnimodDaoHibernate unimodDao = new UnimodDaoHibernate();
        final ParamsDaoHibernate paramsDao = new ParamsDaoHibernate();

        searchDbDao = new SearchDbDaoHibernate();
        searchDbDao.setSwiftDao(swiftDao);
        initializeDatabase(Arrays.asList(swiftDao, unimodDao, paramsDao, searchDbDao));
        searchDbDao.begin();
    }

    @AfterMethod
    public void teardown() {
        searchDbDao.commit();
        teardownDatabase();
    }

    @Test
    public void shouldSaveSmallAnalysis() {
        final Reader reader = ResourceUtilities.getReader(SINGLE, TestScaffoldSpectraSummarizer.class);

        ScaffoldSpectraSummarizer summarizer = new ScaffoldSpectraSummarizer(unimod, scaffoldUnimod);
        summarizer.load(reader, SINGLE, "3");
        final Analysis analysis = summarizer.getAnalysis();

        searchDbDao.addAnalysis(analysis);
    }
}
