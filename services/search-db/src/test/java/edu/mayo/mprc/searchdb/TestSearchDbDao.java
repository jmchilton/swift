package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.DaoTest;
import edu.mayo.mprc.database.DummyFileTokenTranslator;
import edu.mayo.mprc.database.FileType;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDaoImpl;
import edu.mayo.mprc.searchdb.dao.Analysis;
import edu.mayo.mprc.searchdb.dao.SearchDbDaoHibernate;
import edu.mayo.mprc.swift.db.SwiftDaoHibernate;
import edu.mayo.mprc.swift.params2.ParamsDaoHibernate;
import edu.mayo.mprc.unimod.MockUnimodDao;
import edu.mayo.mprc.unimod.Unimod;
import edu.mayo.mprc.unimod.UnimodDaoHibernate;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.Log4jTestSetup;
import edu.mayo.mprc.utilities.ResourceUtilities;
import edu.mayo.mprc.utilities.TestingUtilities;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

/**
 * Exercises the Search-db DAO.
 *
 * @author Roman Zenka
 */
public class TestSearchDbDao extends DaoTest {
    private SearchDbDaoHibernate searchDbDao;
    private Unimod unimod;
    private Unimod scaffoldUnimod;
    private CurationDaoImpl curationDao;
    private UnimodDaoHibernate unimodDao;

    private static final String SINGLE = "classpath:edu/mayo/mprc/searchdb/single.tsv";

    static {
        Log4jTestSetup.configure();
    }

    @BeforeMethod
    public void setup() {
        FileType.initialize(new DummyFileTokenTranslator());

        final SwiftDaoHibernate swiftDao = new SwiftDaoHibernate();
        final ParamsDaoHibernate paramsDao = new ParamsDaoHibernate();
        unimodDao = new UnimodDaoHibernate();
        curationDao = new CurationDaoImpl();

        searchDbDao = new SearchDbDaoHibernate();
        searchDbDao.setSwiftDao(swiftDao);

        initializeDatabase(Arrays.asList(swiftDao, unimodDao, paramsDao, curationDao, searchDbDao));
    }

    private void loadScaffoldUnimod() {
        scaffoldUnimod = new Unimod();
        scaffoldUnimod.parseUnimodXML(ResourceUtilities.getStream("classpath:edu/mayo/mprc/searchdb/scaffold_unimod.xml", Unimod.class));
    }

    private void loadUnimod() {
        unimodDao.begin();
        MockUnimodDao mockUnimodDao = new MockUnimodDao();
        unimod = mockUnimodDao.load();
        unimodDao.upgrade(unimod, new Change("Initial Unimod install", new Date()));
        unimodDao.commit();
    }

    @AfterMethod
    public void teardown() {
        teardownDatabase();
    }

    @Test
    public void shouldSaveSmallAnalysis() throws DatabaseUnitException, SQLException, IOException {
        loadUnimod();
        loadScaffoldUnimod();
        Curation currentSp = loadFasta("/edu/mayo/mprc/searchdb/currentSp.fasta", "Current_SP");

        searchDbDao.begin();

        final Reader reader = ResourceUtilities.getReader(SINGLE, TestScaffoldSpectraSummarizer.class);

        ScaffoldSpectraSummarizer summarizer = new ScaffoldSpectraSummarizer(unimod, scaffoldUnimod, new SingleDatabaseTranslator(searchDbDao, curationDao));
        summarizer.load(reader, SINGLE, "3");
        final Analysis analysis = summarizer.getAnalysis();

        searchDbDao.addAnalysis(analysis);

        getDatabasePlaceholder().getSession().flush();

        DatabaseConnection databaseConnection = new DatabaseConnection(getDatabasePlaceholder().getSession().connection());

        FlatXmlDataSet.write(databaseConnection.createDataSet(), new FileOutputStream("/Users/m044910/database.xml"));

        searchDbDao.commit();
    }

    private Curation loadFasta(String resource, String shortName) {
        File file = null;
        try {
            file = TestingUtilities.getTempFileFromResource(resource, true, null);
            return loadFasta(file, shortName);
        } catch (Exception e) {
            throw new MprcException("Failed to load database [" + shortName + "]", e);
        } finally {
            FileUtilities.cleanupTempFile(file);
        }
    }

    private Curation loadFasta(File file, String shortName) {
        try {
            Curation curation = addCurationToDatabase(shortName, file);
            searchDbDao.addFastaDatabase(curation);
            return curation;
        } catch (Exception e) {
            throw new MprcException("Failed to load database [" + shortName + "]", e);
        }
    }

    @Test
    public void shouldLoadFasta() throws IOException, DatabaseUnitException, SQLException {
        Curation currentSp = loadFasta("/edu/mayo/mprc/searchdb/currentSp.fasta", "Current_SP");
        // Curation currentSp = loadFasta(new File("/Users/m044910/Documents/Databases/Current_SP.fasta"), "Current_SP");

        searchDbDao.begin();
        Assert.assertEquals(searchDbDao.countDatabaseEntries(currentSp), 9);
        searchDbDao.commit();

        // Add the same thing again. Nothing should happen.
        searchDbDao.addFastaDatabase(currentSp);

        searchDbDao.begin();
        Assert.assertEquals(searchDbDao.countDatabaseEntries(currentSp), 9);
        searchDbDao.commit();
    }

    private Curation addCurationToDatabase(String databaseName, File currentSpFasta) {
        Curation currentSp = null;
        try {
            curationDao.begin();
            currentSp = new Curation();
            currentSp.setShortName(databaseName);
            currentSp.setCurationFile(currentSpFasta);
            curationDao.addCuration(currentSp);
            curationDao.commit();
        } catch (Exception e) {
            org.testng.Assert.fail("Cannot load fasta database", e);
        }
        return currentSp;
    }
}
