package edu.mayo.mprc.workflow.persistence;

import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.swift.db.SwiftDaoHibernate;
import edu.mayo.mprc.swift.dbmapping.*;
import edu.mayo.mprc.swift.params2.*;
import edu.mayo.mprc.unimod.ModSet;
import edu.mayo.mprc.utilities.testing.TestApplicationContext;
import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

@Test(sequential = true)
public final class SwiftDaoTest {
	private SwiftDaoHibernate swiftDao;
	private ParamsDao paramsDao;

	@BeforeClass
	public void setup() {
		swiftDao = (SwiftDaoHibernate) TestApplicationContext.getSwiftDao();
		paramsDao = TestApplicationContext.getParamsDao();
	}


	@Test
	public void doubleSaveExtractMsn() throws Throwable {
		swiftDao.begin();
		try {

			ExtractMsnSettings settings = new ExtractMsnSettings("-D");
			settings = paramsDao.addExtractMsnSettings(settings);

			ExtractMsnSettings settings2 = new ExtractMsnSettings("-D");
			settings2 = paramsDao.addExtractMsnSettings(settings2);

			Assert.assertEquals(settings.getId(), settings2.getId(), "Same objects have to get the same id");
			swiftDao.commit();
		} catch (Exception t) {
			swiftDao.rollback();
			throw t;
		}
	}

	@Test
	public void doubleSaveQa() throws Throwable {
		swiftDao.begin();
		try {

			SpectrumQa settings = new SpectrumQa("qa.txt", "msmsEval");
			settings = swiftDao.addSpectrumQa(settings);

			SpectrumQa settings2 = new SpectrumQa("qa.txt", "msmsEval");
			settings2 = swiftDao.addSpectrumQa(settings2);

			Assert.assertEquals(settings.getId(), settings2.getId(), "Same objects have to get the same id");
			swiftDao.commit();
		} catch (Exception t) {
			swiftDao.rollback();
			throw t;
		}
	}

	@Test
	public void doubleSaveScaffoldSettings() throws Throwable {
		swiftDao.begin();
		try {

			ScaffoldSettings settings = new ScaffoldSettings(0.95, 0.95, 2, 1, null, false, false, false, false);
			settings = paramsDao.addScaffoldSettings(settings);

			final List list = swiftDao.getDatabasePlaceholder().getSession().createQuery("from ScaffoldSettings").list();

			ScaffoldSettings settings2 = new ScaffoldSettings(0.95, 0.95, 2, 1, null, false, false, false, false);
			settings2 = paramsDao.addScaffoldSettings(settings2);

			Assert.assertEquals(settings.getId(), settings2.getId(), "Same objects have to get the same id");

			swiftDao.commit();
		} catch (Exception t) {
			swiftDao.rollback();
			throw t;
		}
	}

	@Test
	public void doubleSaveScaffoldSettingsWithStarred() throws Throwable {
		swiftDao.begin();
		try {
			StarredProteins starredProteins = new StarredProteins();
			starredProteins.setDelimiter(",");
			starredProteins.setMatchName(true);
			starredProteins.setStarred("ALBU_HUMAN,OVAL_CHICK");
			starredProteins.setRegularExpression(false);
			starredProteins = paramsDao.addStarredProteins(starredProteins);

			ScaffoldSettings settings = new ScaffoldSettings(0.95, 0.95, 2, 1, starredProteins, false, false, false, false);
			settings = paramsDao.addScaffoldSettings(settings);

			ScaffoldSettings settings2 = new ScaffoldSettings(0.95, 0.95, 2, 1, starredProteins, false, false, false, false);
			settings2 = paramsDao.addScaffoldSettings(settings2);

			Assert.assertEquals(settings.getId(), settings2.getId(), "Same objects have to get the same id");

			swiftDao.commit();
		} catch (Exception t) {
			swiftDao.rollback();
			throw t;
		}
	}

	@Test
	public void shouldDoubleSaveStarredCaseSensitive() throws Throwable {
		swiftDao.begin();
		try {

			StarredProteins starredProteins = makeRegexStarredProteins("ALBU_HUMAN OVAL_CHICK");
			starredProteins = paramsDao.addStarredProteins(starredProteins);

			StarredProteins starredProteins2 = makeRegexStarredProteins("ALBU_Human Oval_Chick");
			starredProteins2 = paramsDao.addStarredProteins(starredProteins2);

			Assert.assertNotSame(starredProteins, starredProteins2);

		} catch (Exception e) {
			swiftDao.rollback();
			throw e;
		}
	}

	private StarredProteins makeRegexStarredProteins(String proteins) {
		StarredProteins starredProteins = new StarredProteins();
		starredProteins.setDelimiter("\\s+");
		starredProteins.setMatchName(true);
		starredProteins.setStarred(proteins);
		starredProteins.setRegularExpression(true);
		return starredProteins;
	}

	@Test
	public void doubleSaveSearchDefinition() throws Throwable {
		swiftDao.begin();
		try {
			SwiftSearchDefinition blank1 = new SwiftSearchDefinition("Blank search", null, null, null, null, null, new ArrayList<FileSearch>(), false, false);
			blank1 = swiftDao.addSwiftSearchDefinition(blank1);
			SwiftSearchDefinition blank2 = new SwiftSearchDefinition("Blank search", null, null, null, null, null, new ArrayList<FileSearch>(), false, false);
			blank2 = swiftDao.addSwiftSearchDefinition(blank2);
			Assert.assertEquals(blank2.getId(), blank1.getId(), "Blank searches has to be stored as one");

			final File f = new File("testFolder");
			final SwiftSearchDefinition folder1 = new SwiftSearchDefinition("Folder search", null, f, null, null, null, new ArrayList<FileSearch>(), false, false);
			blank1 = swiftDao.addSwiftSearchDefinition(blank1);
			final File f2 = new File("testFolder");
			final SwiftSearchDefinition folder2 = new SwiftSearchDefinition("Folder search", null, f2, null, null, null, new ArrayList<FileSearch>(), false, false);
			blank2 = swiftDao.addSwiftSearchDefinition(blank2);
			Assert.assertEquals(folder2.getId(), folder1.getId(), "Folder searches has to be stored as one");

			final SearchEngineConfig searchEngineConfig = new SearchEngineConfig("MASCOT");
			swiftDao.addSearchEngineConfig(searchEngineConfig, new Change("Test search engine added", new DateTime()));
			final EnabledEngines engines = new EnabledEngines();
			engines.add(searchEngineConfig);

			SearchEngineParameters parameters = getSearchEngineParameters();

			parameters = paramsDao.addSearchEngineParameters(parameters);

			final FileSearch fileSearch1 = new FileSearch(new File("input.RAW"), "bio sample", null, "experiment", engines);
			final FileSearch fileSearch2 = new FileSearch(new File("input.RAW"), "bio sample", null, "experiment", engines);

			final LinkedList<FileSearch> fileSearches1 = new LinkedList<FileSearch>();
			fileSearches1.add(fileSearch1);
			final LinkedList<FileSearch> fileSearches2 = new LinkedList<FileSearch>();
			fileSearches2.add(fileSearch2);

			SwiftSearchDefinition file1 = new SwiftSearchDefinition("File search", null, null, null, null, null, fileSearches1, false, false);
			file1 = swiftDao.addSwiftSearchDefinition(file1);
			SwiftSearchDefinition file2 = new SwiftSearchDefinition("File search", null, null, null, null, null, fileSearches2, false, false);
			file2 = swiftDao.addSwiftSearchDefinition(file2);
			Assert.assertEquals(file2.getId(), file1.getId(), "File searches have to be stored as one");

			swiftDao.commit();
		} catch (Exception t) {
			swiftDao.rollback();
			throw t;
		}
	}

	private SearchEngineParameters getSearchEngineParameters() {
		final ModSet fixedModifications = new ModSet();
		final ModSet variableModifications = new ModSet();

		final Instrument orbi = paramsDao.getInstrumentByName("Orbi/FT (ESI-FTICR)");
		final Protease protease = paramsDao.getProteaseByName("Trypsin (allow P)");


		return new SearchEngineParameters(null, protease, 1, fixedModifications, variableModifications,
				new Tolerance(1, MassUnit.Da), new Tolerance(10, MassUnit.Ppm), orbi,
				ExtractMsnSettings.DEFAULT,
				ScaffoldSettings.DEFAULT);
	}

	@Test
	public void addSearchEngine() throws Throwable {
		swiftDao.begin();
		try {
			final Change change = new Change("Test search engine add", new DateTime());
			final SearchEngineConfig config = new SearchEngineConfig("TEST_ENGINE");
			swiftDao.addSearchEngineConfig(config, change);
			Assert.assertNotNull(config.getId(), "Save did not work");

			final Change change2 = new Change("Test search engine add 2", new DateTime());
			final SearchEngineConfig config2 = new SearchEngineConfig("TEST_ENGINE");
			Assert.assertTrue(config.equals(config2), "The two changes must be identical");

			swiftDao.addSearchEngineConfig(config2, change2);
			Assert.assertEquals(config2.getId(), config.getId(), "Save must produce same id");

			swiftDao.commit();
		} catch (Exception t) {
			swiftDao.rollback();
			throw t;
		}
	}

	@Test
	public void addEnabledEngines() throws Throwable {
		swiftDao.begin();
		try {
			final Change change = new Change("Test enabled engines", new DateTime());
			final SearchEngineConfig engine1 = new SearchEngineConfig("TEST_ENGINE1");
			final SearchEngineConfig engine2 = new SearchEngineConfig("TEST_ENGINE2");
			swiftDao.addSearchEngineConfig(engine1, change);
			swiftDao.addSearchEngineConfig(engine2, change);

			final EnabledEngines engines = new EnabledEngines();
			engines.add(engine1);
			engines.add(engine2);

			final EnabledEngines engines1 = swiftDao.addEnabledEngineSet(Arrays.asList(engine1.getCode(), engine2.getCode()));
			final EnabledEngines engines2 = swiftDao.addEnabledEngineSet(Arrays.asList(engine2.getCode(), engine1.getCode()));
			final EnabledEngines engines3 = swiftDao.addEnabledEngineSet(new ArrayList<String>());
			final EnabledEngines engines4 = swiftDao.addEnabledEngineSet(new ArrayList<String>());

			Assert.assertEquals(engines2, engines1, "Have to be identical sets");
			Assert.assertNotSame(engines3, engines1, "Empty engine set has to be different");
			Assert.assertEquals(engines4, engines3, "Empty sets have to be identical");

			swiftDao.commit();
		} catch (Exception t) {
			swiftDao.rollback();
			throw t;
		}
	}

	@Test
	public void twoTransactionsPerSession() throws Throwable {
		swiftDao.begin();
		final TaskStateData taskStateData = swiftDao.getTaskState(TaskState.READY);
		final TaskData taskData = new TaskData("Test task", new Date(), new Date(), null, null, taskStateData, "Test task description");
		swiftDao.updateTask(taskData);
		swiftDao.commit();

		swiftDao.begin();
		swiftDao.updateTask(taskData);
		taskData.setDescriptionLong("New description");
		swiftDao.commit();
	}
}
