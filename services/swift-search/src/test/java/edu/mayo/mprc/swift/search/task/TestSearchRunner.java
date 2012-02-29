package edu.mayo.mprc.swift.search.task;

import edu.mayo.mprc.config.DaemonConfigInfo;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.SimpleThreadPoolExecutor;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.swift.db.SearchEngine;
import edu.mayo.mprc.swift.db.SwiftDao;
import edu.mayo.mprc.swift.dbmapping.*;
import edu.mayo.mprc.swift.params2.*;
import edu.mayo.mprc.swift.search.SwiftSearchWorkPacket;
import edu.mayo.mprc.unimod.ModSet;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.progress.ProgressReporter;
import edu.mayo.mprc.workspace.User;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;

/**
 * @author Roman Zenka
 */
public class TestSearchRunner {
	private File outputFolder;
	private File raw1;
	private File raw2;

	@BeforeTest
	public void setup() throws IOException {
		outputFolder = FileUtilities.createTempFolder();
		raw1 = new File(outputFolder, "file1.RAW");
		raw1.createNewFile();
		raw2 = new File(outputFolder, "file2.RAW");
		raw2.createNewFile();
	}

	@AfterTest
	public void teardown() {
		FileUtilities.cleanupTempFile(outputFolder);
	}


	@Test
	public void singleExperimentRunner() throws IOException {
		SwiftSearchWorkPacket packet = new SwiftSearchWorkPacket(1, "task1", false, 0);

		Collection<SearchEngine> searchEngines = searchEngines();

		EnabledEngines engines = enabledEngines();

		List<FileSearch> inputFiles = Arrays.asList(
				new FileSearch(raw1, "biosample", "category", "experiment", engines),
				new FileSearch(raw2, "biosample2", "category", "experiment", engines)
		);

		SwiftSearchDefinition definition = defaultSearchDefinition(inputFiles);

		ProgressReporter reporter = mock(ProgressReporter.class);
		ExecutorService service = new SimpleThreadPoolExecutor(1, "testSwiftSearcher");

		SearchRun searchRun = null;

		SearchRunner runner = new SearchRunner(packet,
				definition,
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				searchEngines,
				reporter,
				service,
				mock(CurationDao.class),
				mock(SwiftDao.class),
				dummyFileTokenFactory(),
				searchRun);

		runner.initialize();
		Assert.assertEquals(runner.getWorkflowEngine().getNumTasks(), 23);
	}

	@Test
	public void multipleExperimentRunner() throws IOException {
		SwiftSearchWorkPacket packet = new SwiftSearchWorkPacket(1, "task1", false, 0);

		Collection<SearchEngine> searchEngines = searchEngines();

		EnabledEngines engines = enabledEngines();

		List<FileSearch> inputFiles = Arrays.asList(
				new FileSearch(raw1, "biosample", "category", "experiment1", engines),
				new FileSearch(raw2, "biosample2", "category", "experiment2", engines)
		);

		SwiftSearchDefinition definition = defaultSearchDefinition(inputFiles);

		ProgressReporter reporter = mock(ProgressReporter.class);
		ExecutorService service = new SimpleThreadPoolExecutor(1, "testSwiftSearcher");

		SearchRun searchRun = null;

		SearchRunner runner = new SearchRunner(packet,
				definition,
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				mock(DaemonConnection.class),
				searchEngines,
				reporter,
				service,
				mock(CurationDao.class),
				mock(SwiftDao.class),
				dummyFileTokenFactory(),
				searchRun);

		runner.initialize();
		// final String dot = runner.getWorkflowEngine().dumpDot();
		Assert.assertEquals(runner.getWorkflowEngine().getNumTasks(), 23 + 2 /* Scaffold+Scaffold3 */ + 1 /* Db load */);
	}

	private SwiftSearchDefinition defaultSearchDefinition(List<FileSearch> inputFiles) {
		return new SwiftSearchDefinition(
				"Test search",
				new User("Tester", "Testov", "test", "pwd"),
				outputFolder,
				new SpectrumQa("orbitrap", "msmsEval"),
				new PeptideReport(),
				new SearchEngineParameters(dummyCuration(), Protease.getInitial().get(0),
						1, new ModSet(), new ModSet(), new Tolerance(10, MassUnit.Ppm),
						new Tolerance(1, MassUnit.Da), Instrument.ORBITRAP,
						new ExtractMsnSettings("-M100"),
						new ScaffoldSettings(0.95, 0.95, 2, 0, new StarredProteins("ALBU_HUMAN", ",", false, false), false, false, true, true)
				),
				inputFiles,
				false,
				false
		);
	}

	private Collection<SearchEngine> searchEngines() {
		Collection<SearchEngine> searchEngines = new ArrayList<SearchEngine>();
		final SearchEngine mascot = searchEngine("MASCOT");
		final SearchEngine sequest = searchEngine("SEQUEST");
		final SearchEngine tandem = searchEngine("TANDEM");
		final SearchEngine scaffold = searchEngine("SCAFFOLD");
		final SearchEngine scaffold3 = searchEngine("SCAFFOLD3");
		searchEngines.add(mascot);
		searchEngines.add(sequest);
		searchEngines.add(tandem);
		searchEngines.add(scaffold);
		searchEngines.add(scaffold3);
		return searchEngines;
	}

	private EnabledEngines enabledEngines() {
		EnabledEngines engines = new EnabledEngines();
		engines.add(createSearchEngineConfig("MASCOT"));
		engines.add(createSearchEngineConfig("SEQUEST"));
		engines.add(createSearchEngineConfig("TANDEM"));
		engines.add(createSearchEngineConfig("SCAFFOLD"));
		engines.add(createSearchEngineConfig("SCAFFOLD3"));
		return engines;
	}

	private FileTokenFactory dummyFileTokenFactory() {
		FileTokenFactory fileTokenFactory = new FileTokenFactory();
		final DaemonConfigInfo mainDaemon = new DaemonConfigInfo("daemon1", "/");
		fileTokenFactory.setDaemonConfigInfo(mainDaemon);
		fileTokenFactory.setDatabaseDaemonConfigInfo(mainDaemon);
		return fileTokenFactory;
	}

	private Curation dummyCuration() {
		final Curation curation = new Curation();
		curation.setId(1);
		return curation;
	}

	private SearchEngine searchEngine(String code) {
		SearchEngine engine = new SearchEngine();
		engine.setCode(code);
		engine.setSearchDaemon(mock(DaemonConnection.class));
		engine.setDbDeployDaemon(mock(DaemonConnection.class));
		engine.setOutputDirName(code + "_output_dir");
		return engine;
	}

	private SearchEngineConfig createSearchEngineConfig(String code) {
		final SearchEngineConfig config = new SearchEngineConfig(code);
		config.setCode(code);
		return config;
	}

}
