package edu.mayo.mprc.swift.params2;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.chem.AminoAcidSet;
import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.database.DatabaseUtilities;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDaoImpl;
import edu.mayo.mprc.unimod.*;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public final class ParamsDaoTest {
	private static final Logger LOGGER = Logger.getLogger(ParamsDaoTest.class);

	private SessionFactory factory;
	private ParamsDao dao;
	private UnimodDao unimodDao;
	private CurationDao curationDao;
	private static final String PARAMS_FOLDER = "edu/mayo/mprc/swift/params2/";
	private final DatabasePlaceholder databasePlaceholder = new DatabasePlaceholder();
	private Mod mod1;
	private Mod mod2;
	private Mod mod3;

	private SpecificityBuilder build1;
	private SpecificityBuilder build2;
	private SpecificityBuilder build3;

	private ModSpecificity spec1;
	private ModSpecificity spec2;
	private ModSpecificity spec3;

	@BeforeMethod
	public void setup() {
		factory = DatabaseUtilities.getTestSessionFactory(Arrays.asList(
				"edu/mayo/mprc/database/Change.hbm.xml",
				"edu/mayo/mprc/dbcurator/model/Curation.hbm.xml",
				"edu/mayo/mprc/dbcurator/model/SourceDatabaseArchive.hbm.xml",
				"edu/mayo/mprc/dbcurator/model/curationsteps/CurationStep.hbm.xml",
				"edu/mayo/mprc/dbcurator/model/curationsteps/DataSource.hbm.xml",
				"edu/mayo/mprc/dbcurator/model/curationsteps/HeaderTransform.hbm.xml",
				"edu/mayo/mprc/unimod/Mod.hbm.xml",
				"edu/mayo/mprc/unimod/ModSet.hbm.xml",
				"edu/mayo/mprc/unimod/ModSpecificity.hbm.xml",
				"edu/mayo/mprc/workspace/User.hbm.xml",
				PARAMS_FOLDER + "IonSeries.hbm.xml",
				PARAMS_FOLDER + "Instrument.hbm.xml",
				PARAMS_FOLDER + "Protease.hbm.xml",
				PARAMS_FOLDER + "SearchEngineParameters.hbm.xml",
				PARAMS_FOLDER + "SavedSearchEngineParameters.hbm.xml",
				PARAMS_FOLDER + "ExtractMsnSettings.hbm.xml",
				PARAMS_FOLDER + "ScaffoldSettings.hbm.xml",
				PARAMS_FOLDER + "StarredProteins.hbm.xml"
		));

		databasePlaceholder.setSessionFactory(factory);
		dao = new ParamsDaoImpl(databasePlaceholder);
		unimodDao = new UnimodDaoImpl(databasePlaceholder);
		curationDao = new CurationDaoImpl(databasePlaceholder);

		databasePlaceholder.begin();

		// Initialize unimod and three mods
		Unimod unimod = new Unimod();

		HashSet<String> altNames = new HashSet<String>();
		altNames.add("Testing mod");
		altNames.add("Discard");
		build1 = new SpecificityBuilder(AminoAcidSet.DEFAULT.getForSingleLetterCode("C"), Terminus.Anywhere, false, false, "Classification1", 1);
		mod1 = new Mod("Mod 1", "Name of mod 1", 12, 13.4, 13.52, "C2H0", altNames, build1);
		spec1 = mod1.getModSpecificities().iterator().next();

		build2 = new SpecificityBuilder(AminoAcidSet.DEFAULT.getForSingleLetterCode("R"), Terminus.Anywhere, false, false, "Classification2", 1);
		mod2 = new Mod("Mod 2", "Full name of mod 2", 24, 24.5, 24.63, "C4H02", null, build2);
		spec2 = mod2.getModSpecificities().iterator().next();

		build3 = new SpecificityBuilder(AminoAcidSet.DEFAULT.getForSingleLetterCode("K"), Terminus.Cterm, true, false, "Classification3", 1);
		mod3 = new Mod("Mod 3", "Full name of mod 3", 35, 35.6, 35.74, "C8H3", null, build3);
		spec3 = mod3.getModSpecificities().iterator().next();

		unimod.add(mod1);
		unimod.add(mod2);
		unimod.add(mod3);
		unimodDao.upgrade(unimod, new Change("Uploading test unimod", new Date()));
	}

	@AfterMethod
	public void teardown() throws Throwable {
		try {
			databasePlaceholder.commit();
		} catch (Exception t) {
			LOGGER.error("Failed closing transaction ", t);
			databasePlaceholder.rollback();
			throw t;
		}

		dao = null;
		factory.close();
	}

	@Test
	public void shouldListNoIonSeriesWhenInitialized() {
		Assert.assertEquals(dao.ionSeries().size(), 0, "no instruments initially");
	}

	@Test
	public void shouldListOneIonSeriesWhenAdded() {
		IonSeries series = new IonSeries("a");
		Change change = new Change("test add", new Date());
		dao.addIonSeries(series, change);

		final List<IonSeries> list = dao.ionSeries();
		Assert.assertEquals(list.size(), 1, "One ion series");
	}

	@Test(expectedExceptions = MprcException.class)
	public void shouldPreventDoubleIonSeriesAddition() {
		IonSeries series = new IonSeries("a");
		Change change = new Change("test add", new Date());
		dao.addIonSeries(series, change);

		IonSeries series2 = new IonSeries("a");
		Change change2 = new Change("test add 2", new Date());
		dao.addIonSeries(series2, change2);
	}

	@Test()
	public void shouldIgnoreIonSeriesUpdates() {
		IonSeries series = new IonSeries("a");
		Change change = new Change("test add", new Date());
		dao.addIonSeries(series, change);

		Change change2 = new Change("test add 2", new Date());
		series = dao.updateIonSeries(series, change2);

		final List<IonSeries> seriesList = dao.ionSeries();
		Assert.assertEquals(seriesList.size(), 1, "Just one active series");
		Assert.assertEquals(seriesList.get(0).getCreation(), change, "The previous update must be listed, because there was no change applied");
	}

	@Test
	public void shouldDeleteIonSeries() {
		IonSeries series = new IonSeries("a");
		Change change = new Change("test add", new Date());
		dao.addIonSeries(series, change);

		dao.deleteIonSeries(series, new Change("test delete", new Date()));

		final List<IonSeries> list = dao.ionSeries();
		Assert.assertEquals(list.size(), 0, "Series got deleted");

		// Double delete does nothing
		dao.deleteIonSeries(series, new Change("test delete 2", new Date()));
	}

	@Test
	public void shouldSaveInstrumentWithSeries() {
		Instrument instrument = getSimpleInstrument();

		instrument = dao.addInstrument(instrument, new Change("Creating new instrument", new Date()));

		final List<IonSeries> ionSeriesList = dao.ionSeries();
		Assert.assertEquals(ionSeriesList.size(), 2, "Two ion series from the instrument");
	}

	private Instrument getSimpleInstrument() {
		HashSet<IonSeries> set = new HashSet<IonSeries>();
		set.add(new IonSeries("b"));
		set.add(new IonSeries("y"));
		return new Instrument("Simple instrument", set, "SIMPLE");
	}

	@Test
	public void shouldUpdateInstrument() {
		Instrument instrument = getSimpleInstrument();

		instrument = dao.addInstrument(instrument, new Change("Creating new instrument", new Date()));
		int oldId = instrument.getId();

		HashSet<IonSeries> set2 = new HashSet<IonSeries>();
		set2.add(new IonSeries("a"));
		set2.add(new IonSeries("y"));
		Instrument instrument2 = new Instrument(instrument.getName(), set2, instrument.getMascotName());

		instrument2 = dao.updateInstrument(instrument2, new Change("Updating instrument", new Date()));

		Assert.assertEquals(dao.ionSeries().size(), 3, "Three ion series total instrument");
		Assert.assertEquals(dao.instruments().size(), 1, "One instrument");
		Assert.assertFalse(instrument2.getId().equals(oldId), "The instrument id has to change");
	}

	@Test
	public void shouldNotUpdateInstrumentWhenSame() {
		Instrument instrument = getSimpleInstrument();

		instrument = dao.addInstrument(instrument, new Change("Creating new instrument", new Date()));
		int oldId = instrument.getId();

		Instrument instrument2 = new Instrument(instrument.getName(), instrument.getSeries(), instrument.getMascotName());

		instrument2 = dao.updateInstrument(instrument2, new Change("Updating instrument", new Date()));

		Assert.assertEquals(dao.ionSeries().size(), 2, "Two ion series total instrument");
		Assert.assertEquals(dao.instruments().size(), 1, "One instrument");
		Assert.assertTrue(instrument2.getId().equals(oldId), "The instrument id is the same");
	}

	@Test
	public void shouldDeleteInstrument() {
		Instrument instrument = getSimpleInstrument();
		instrument = dao.addInstrument(instrument, new Change("Creating new instrument", new Date()));

		dao.deleteInstrument(instrument, new Change("Deleting instrument", new Date()));

		Assert.assertEquals(dao.instruments().size(), 0, "No instruments");
		Assert.assertEquals(instrument.getDeletion().getReason(), "Deleting instrument", "Must have good reason to be deleted");
	}

	@Test
	public void shouldAddModSet() {
		ModSet modSet = new ModSet();
		modSet.add(spec1);
		modSet.add(spec2);
		modSet = dao.updateModSet(modSet);
		Assert.assertNotNull(modSet.getId());

		ModSet modSet2 = new ModSet();
		modSet2.add(spec2);
		modSet2.add(spec3);
		modSet2 = dao.updateModSet(modSet2);
		Assert.assertNotNull(modSet2.getId());

		ModSet modSet3 = new ModSet();
		modSet3.add(spec2);
		modSet3.add(spec1);
		modSet3 = dao.updateModSet(modSet3);
		Assert.assertEquals(modSet3.getId(), modSet.getId());
	}

	@Test
	public void shouldAddSearchParameters() {
		SearchEngineParameters params = new SearchEngineParameters();
		Curation database = new Curation();
		database.setTitle("Test Curation");
		database.setShortName("test");
		curationDao.addCuration(database);

		params.setDatabase(database);

		ModSet fixedMods = new ModSet();
		fixedMods.add(spec1);
		fixedMods.add(spec2);
		fixedMods = dao.updateModSet(fixedMods);
		params.setFixedModifications(fixedMods);

		ModSet variableMods = new ModSet();
		variableMods = dao.updateModSet(variableMods);
		params.setVariableModifications(variableMods);

		params.setPeptideTolerance(new Tolerance("10 ppm"));
		params.setFragmentTolerance(new Tolerance("0.5 Da"));
		params.setExtractMsnSettings(new ExtractMsnSettings("-Z10"));
		params.setScaffoldSettings(new ScaffoldSettings(0.95, 0.95, 2, 2, null, true, true, true, true));

		Instrument instrument = getSimpleInstrument();
		instrument = dao.updateInstrument(instrument, new Change("Updating instrument", new Date()));
		params.setInstrument(instrument);

		params.setMissedCleavages(3);

		Protease protease = new Protease("Trypsin (allow P)", "KR", "");
		dao.addProtease(protease, new Change("Adding Trypsin", new Date()));
		params.setProtease(protease);

		params = dao.addSearchEngineParameters(params);
		Assert.assertNotNull(params.getId());

		databasePlaceholder.getSession().flush();
		databasePlaceholder.getSession().clear();

		ModSet dummyMods = new ModSet();
		dummyMods.add(spec1);
		dummyMods = dao.updateModSet(dummyMods);
		Assert.assertNotSame(dummyMods.getId(), variableMods.getId());

		ModSet variableMods2 = new ModSet();
		variableMods2 = dao.updateModSet(variableMods2);
		Assert.assertEquals(variableMods.getId(), variableMods2.getId(), "Empty mod set has to have identical ID");

		SearchEngineParameters params2 = new SearchEngineParameters();
		params2.setExtractMsnSettings(new ExtractMsnSettings("-Z10"));
		params2.setScaffoldSettings(new ScaffoldSettings(0.95, 0.95, 2, 2, null, true, true, true, true));
		params2.setDatabase(database);
		params2.setFixedModifications(fixedMods);
		params2.setVariableModifications(variableMods2);
		params2.setPeptideTolerance(new Tolerance("10 ppm"));
		params2.setFragmentTolerance(new Tolerance("0.5 Da"));
		params2.setInstrument(instrument);
		params2.setMissedCleavages(3);
		params2.setProtease(protease);
		params2 = dao.addSearchEngineParameters(params2);
		Assert.assertEquals(params2.getId(), params.getId(), "Must save as the same object");

		SearchEngineParameters params3 = new SearchEngineParameters();
		params3.setExtractMsnSettings(new ExtractMsnSettings("-Z10"));
		params3.setScaffoldSettings(new ScaffoldSettings(0.95, 0.95, 2, 2, null, true, true, true, true));
		params3.setDatabase(database);
		params3.setFixedModifications(fixedMods);
		params3.setVariableModifications(variableMods);
		params3.setPeptideTolerance(new Tolerance("10.1 ppm"));
		params3.setFragmentTolerance(new Tolerance("0.5 Da"));
		params3.setInstrument(instrument);
		params3.setMissedCleavages(3);
		params3.setProtease(protease);
		params3 = dao.addSearchEngineParameters(params3);
		Assert.assertNotSame(params3.getId(), params.getId(), "Must save as different object");
	}
}
