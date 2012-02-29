package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.DaoTest;
import edu.mayo.mprc.database.DummyFileTokenTranslator;
import edu.mayo.mprc.database.FileType;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDaoImpl;
import edu.mayo.mprc.fastadb.FastaDbDaoHibernate;
import edu.mayo.mprc.fastadb.SingleDatabaseTranslator;
import edu.mayo.mprc.searchdb.dao.Analysis;
import edu.mayo.mprc.searchdb.dao.PeptideSpectrumMatch;
import edu.mayo.mprc.searchdb.dao.SearchDbDaoHibernate;
import edu.mayo.mprc.swift.db.SwiftDaoHibernate;
import edu.mayo.mprc.swift.dbmapping.ReportData;
import edu.mayo.mprc.swift.dbmapping.SearchRun;
import edu.mayo.mprc.swift.params2.ParamsDaoHibernate;
import edu.mayo.mprc.unimod.MockUnimodDao;
import edu.mayo.mprc.unimod.Unimod;
import edu.mayo.mprc.unimod.UnimodDaoHibernate;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;
import edu.mayo.mprc.utilities.TestingUtilities;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
	private SwiftDaoHibernate swiftDao;
	private FastaDbDaoHibernate fastaDbDao;

	private static final String SINGLE = "classpath:edu/mayo/mprc/searchdb/single.tsv";

	@BeforeMethod
	public void setup() {
		FileType.initialize(new DummyFileTokenTranslator());

		final ParamsDaoHibernate paramsDao = new ParamsDaoHibernate();
		unimodDao = new UnimodDaoHibernate();
		curationDao = new CurationDaoImpl();
		fastaDbDao = new FastaDbDaoHibernate();
		swiftDao = new SwiftDaoHibernate();

		searchDbDao = new SearchDbDaoHibernate();
		searchDbDao.setSwiftDao(swiftDao);
		searchDbDao.setFastaDbDao(fastaDbDao);

		initializeDatabase(Arrays.asList(swiftDao, unimodDao, paramsDao, curationDao, searchDbDao, fastaDbDao));
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
		loadFasta("/edu/mayo/mprc/searchdb/currentSp.fasta", "Current_SP");

		searchDbDao.begin();

		final Analysis analysis = loadAnalysis(new Date());

		getDatabasePlaceholder().getSession().flush();

		StringWriter writer = new StringWriter();
		Report r = new Report(writer);

		analysis.htmlReport(r, searchDbDao, null);

		// TODO: Check that the analysis is saved properly
//        DatabaseConnection databaseConnection = new DatabaseConnection(getDatabasePlaceholder().getSession().connection());
//        FlatXmlDataSet.write(databaseConnection.createDataSet(), new FileOutputStream("/Users/m044910/database.xml"));

		searchDbDao.commit();

		searchDbDao.begin();

		final List<ReportData> searchRuns = searchDbDao.getSearchesForAccessionNumber("K1C10_HUMAN");
		Assert.assertEquals(searchRuns.size(), 1, "Must find our one search");
		Assert.assertTrue(null != searchRuns.get(0), "Must return correct type");

		searchDbDao.commit();
	}

	private Analysis loadAnalysis(Date now) {
		final Reader reader = ResourceUtilities.getReader(SINGLE, TestScaffoldSpectraSummarizer.class);

		ScaffoldSpectraSummarizer summarizer = new ScaffoldSpectraSummarizer(unimod, scaffoldUnimod,
				new SingleDatabaseTranslator(fastaDbDao, curationDao),
				new DummyMassSpecDataExtractor(now));
		summarizer.load(reader, SINGLE, "3", null);
		final Analysis analysis = summarizer.getAnalysis();

		SearchRun searchRun = swiftDao.fillSearchRun(null);
		ReportData reportData = swiftDao.storeReport(searchRun.getId(), new File("random.sf3"));

		searchDbDao.addAnalysis(analysis, reportData);
		return analysis;
	}

	/**
	 * Make sure that if we save the same thing twice, the database stays unchanged.
	 */
	@Test
	public void saveShouldBeIdempotent() throws DatabaseUnitException, SQLException, IOException {
		loadUnimod();
		loadScaffoldUnimod();
		loadFasta("/edu/mayo/mprc/searchdb/currentSp.fasta", "Current_SP");

		Date now = new Date();

		searchDbDao.begin();
		final Analysis analysis = loadAnalysis(now);
		getDatabasePlaceholder().getSession().flush();
		searchDbDao.commit();

		searchDbDao.begin();
		final long psm1 = searchDbDao.rowCount(PeptideSpectrumMatch.class);
		DatabaseConnection databaseConnection = new DatabaseConnection(getDatabasePlaceholder().getSession().connection());
		FlatXmlDataSet.write(databaseConnection.createDataSet(), new FileOutputStream("/Users/m044910/database1.xml", false));
		searchDbDao.commit();

		searchDbDao.begin();
		final Analysis analysis2 = loadAnalysis(now);
		getDatabasePlaceholder().getSession().flush();
		searchDbDao.commit();

		searchDbDao.begin();
		DatabaseConnection databaseConnection2 = new DatabaseConnection(getDatabasePlaceholder().getSession().connection());
		FlatXmlDataSet.write(databaseConnection2.createDataSet(), new FileOutputStream("/Users/m044910/database2.xml", false));
		searchDbDao.commit();

		searchDbDao.begin();
		final List<ReportData> searchRuns = searchDbDao.getSearchesForAccessionNumber("K1C10_HUMAN");
		Assert.assertEquals(searchRuns.size(), 2, "Must find two searches");
		Assert.assertTrue(null != searchRuns.get(0), "Must return correct type");
		final long psm2 = searchDbDao.rowCount(PeptideSpectrumMatch.class);
		searchDbDao.commit();

		Assert.assertEquals(psm2, psm1, "The oeptide spectrum match count has to stay the same");
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
			fastaDbDao.addFastaDatabase(curation, null);
			return curation;
		} catch (Exception e) {
			throw new MprcException("Failed to load database [" + shortName + "]", e);
		}
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
