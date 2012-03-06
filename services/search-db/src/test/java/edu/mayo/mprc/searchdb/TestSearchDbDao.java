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
import edu.mayo.mprc.searchdb.dao.*;
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
import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private static final String TRIVIAL = "classpath:edu/mayo/mprc/searchdb/trivial.tsv";
	private static final String TRIVIAL_NOTANDEM = "classpath:edu/mayo/mprc/searchdb/trivial_notandem.tsv";

	/**
	 * @return All reports to test.
	 */
	@DataProvider(name = "reports")
	private Object[][] listReports() {
		return new Object[][]{
				{TRIVIAL},
				{TRIVIAL_NOTANDEM},
				{SINGLE},
		};
	}


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
		unimodDao.upgrade(unimod, new Change("Initial Unimod install", new DateTime()));
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

		final Analysis analysis = loadAnalysis(new DateTime(), SINGLE, saveNewReportData());

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

	private ReportData saveNewReportData() {
		SearchRun searchRun = swiftDao.fillSearchRun(null);
		return swiftDao.storeReport(searchRun.getId(), new File("random.sf3"));
	}

	private Analysis loadAnalysis(final DateTime now, final String reportToLoad, ReportData reportData) {
		final Reader reader = ResourceUtilities.getReader(reportToLoad, TestScaffoldSpectraSummarizer.class);

		ScaffoldSpectraSummarizer summarizer = new ScaffoldSpectraSummarizer(unimod, scaffoldUnimod,
				new SingleDatabaseTranslator(fastaDbDao, curationDao),
				new DummyMassSpecDataExtractor(now));
		summarizer.load(reader, reportToLoad, "3", null);
		final Analysis analysis = summarizer.getAnalysis();

		searchDbDao.addAnalysis(analysis, reportData);
		return analysis;
	}

	/**
	 * Memorizes how many rows were there for a particular set of classes.
	 */
	private class ClassCounts {
		private HashMap<Class<?>, Long> counts = new HashMap<Class<?>, Long>(10);

		public void add(Class<?> clazz) {
			final long count = searchDbDao.rowCount(clazz);
			counts.put(clazz, count);
		}

		public void assertSame(ClassCounts other) {
			for (Map.Entry<Class<?>, Long> entry : this.counts.entrySet()) {
				Long otherCount = other.counts.get(entry.getKey());
				Assert.assertEquals(entry.getValue(), otherCount, "The count of [" + entry.getKey().getSimpleName() + "] should not change.");
			}
		}
	}

	/**
	 * @return Loaded counts of all fields that should be idempotent (saving twice will not increase their amount).
	 */
	private ClassCounts countIdempotentClasses() {
		final ClassCounts counts = new ClassCounts();
		counts.add(Analysis.class);
		counts.add(BiologicalSample.class);
		counts.add(BiologicalSampleList.class);
		counts.add(IdentifiedPeptide.class);
		counts.add(LocalizedModification.class);
		counts.add(LocalizedModList.class);
		counts.add(PeptideSpectrumMatch.class);
		counts.add(ProteinGroup.class);
		counts.add(ProteinGroupList.class);
		counts.add(ProteinSequenceList.class);
		counts.add(PsmList.class);
		counts.add(SearchResult.class);
		counts.add(SearchResultList.class);
		counts.add(TandemMassSpectrometrySample.class);
		return counts;
	}

	/**
	 * Make sure that if we save the same thing twice, the database stays unchanged.
	 */
	@Test(dataProvider = "reports")
	public void saveShouldBeIdempotent(String report) throws DatabaseUnitException, SQLException, IOException {
		loadUnimod();
		loadScaffoldUnimod();
		loadFasta("/edu/mayo/mprc/searchdb/currentSp.fasta", "Current_SP");

		DateTime now = new DateTime();
		searchDbDao.begin();
		final ReportData reportData = saveNewReportData();
		searchDbDao.commit();

		searchDbDao.begin();
		final Analysis analysis = loadAnalysis(now, report, reportData);
		getDatabasePlaceholder().getSession().flush();
		searchDbDao.commit();

		searchDbDao.begin();
		final ClassCounts classCounts = countIdempotentClasses();
		searchDbDao.commit();

		searchDbDao.begin();
		final Analysis analysis2 = loadAnalysis(now, report, reportData);
		getDatabasePlaceholder().getSession().flush();
		searchDbDao.commit();

		searchDbDao.begin();
		final List<ReportData> searchRuns = searchDbDao.getSearchesForAccessionNumber("TERA_BOVIN");
		Assert.assertEquals(searchRuns.size(), 1, "Must find single search");
		Assert.assertTrue(null != searchRuns.get(0), "Must return correct type");
		final ClassCounts classCounts2 = countIdempotentClasses();
		Assert.assertNotSame(analysis, analysis2, "Analysis differs because it points to a different report");
		searchDbDao.commit();

		classCounts.assertSame(classCounts2);
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
