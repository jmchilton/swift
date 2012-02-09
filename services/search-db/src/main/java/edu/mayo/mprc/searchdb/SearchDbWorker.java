package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.database.DatabaseFactory;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.fastadb.FastaDbDao;
import edu.mayo.mprc.fastadb.ProteinSequenceTranslator;
import edu.mayo.mprc.fastadb.SingleDatabaseTranslator;
import edu.mayo.mprc.searchdb.dao.SearchDbDao;
import edu.mayo.mprc.swift.db.SwiftDao;
import edu.mayo.mprc.swift.dbmapping.ReportData;
import edu.mayo.mprc.unimod.Unimod;
import edu.mayo.mprc.unimod.UnimodDao;
import edu.mayo.mprc.utilities.FileUtilities;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * Loads search results from a given Swift experiment into the database.
 */
public final class SearchDbWorker implements Worker {
    private DatabaseFactory.Config database;
    private SearchDbDao dao;
    private FastaDbDao fastaDbDao;
    private CurationDao curationDao;
    private UnimodDao unimodDao;
    private SwiftDao swiftDao;
    private File scaffoldModSet;
    private Unimod databaseUnimod;
    private Unimod scaffoldUnimod;

    public static final String TYPE = "search-db";
    public static final String NAME = "Database of search results";
    public static final String DESC = "Loads the search results into a database for fast future queries.";

    private static final String DATABASE = "database";
    private static final String SCAFFOLD_MOD_SET = "scaffoldModSet";

    public SearchDbWorker(SearchDbDao dao, FastaDbDao fastaDbDao, CurationDao curationDao, UnimodDao unimodDao, SwiftDao swiftDao, File scaffoldModSet) {
        this.dao = dao;
        this.fastaDbDao = fastaDbDao;
        this.curationDao = curationDao;
        this.unimodDao = unimodDao;
        this.swiftDao = swiftDao;
        this.scaffoldModSet = scaffoldModSet;
        loadScaffoldUnimod(scaffoldModSet);
        loadDatabaseUnimod();
    }

    private void loadDatabaseUnimod() {
        unimodDao.begin();
        try {
            databaseUnimod = unimodDao.load();
            unimodDao.commit();
        } catch (Exception e) {
            unimodDao.rollback();
            throw new MprcException("Could not load unimod from the database", e);
        }
    }

    private void loadScaffoldUnimod(File scaffoldModSet) {
        scaffoldUnimod = new Unimod();
        scaffoldUnimod.parseUnimodXML(FileUtilities.getInputStream(scaffoldModSet));
    }

    @Override
    public void processRequest(WorkPacket workPacket, ProgressReporter progressReporter) {
        try {
            progressReporter.reportStart();
            process(workPacket, progressReporter);
            workPacket.synchronizeFileTokensOnReceiver();
            progressReporter.reportSuccess();
        } catch (Exception t) {
            progressReporter.reportFailure(t);
        }
    }

    private void process(WorkPacket wp, ProgressReporter reporter) {
        SearchDbWorkPacket workPacket = (SearchDbWorkPacket) wp;
        dao.begin();
        try {
            ReportData reportData = swiftDao.getReportForId(workPacket.getReportDataId());

            ProteinSequenceTranslator translator = new SingleDatabaseTranslator(fastaDbDao, curationDao);
            ScaffoldSpectraSummarizer summarizer = new ScaffoldSpectraSummarizer(databaseUnimod, scaffoldUnimod, translator);
            summarizer.load(workPacket.getScaffoldSpectrumReport(), "3");

            dao.addAnalysis(summarizer.getAnalysis(), reportData);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            throw new MprcException(
                    "Could not load search results from ["
                            + ((SearchDbWorkPacket) wp).getScaffoldSpectrumReport().getAbsolutePath()
                            + "]", e);
        }
    }

    /**
     * A factory capable of creating the worker
     */
    public static final class Factory extends WorkerFactoryBase<Config> {
        private SearchDbDao searchDbDao;
        private FastaDbDao fastaDbDao;
        private CurationDao curationDao;
        private UnimodDao unimodDao;
        private SwiftDao swiftDao;

        public SearchDbDao getSearchDbDao() {
            return searchDbDao;
        }

        public void setSearchDbDao(SearchDbDao searchDbDao) {
            this.searchDbDao = searchDbDao;
        }

        public FastaDbDao getFastaDbDao() {
            return fastaDbDao;
        }

        public void setFastaDbDao(FastaDbDao fastaDbDao) {
            this.fastaDbDao = fastaDbDao;
        }

        public CurationDao getCurationDao() {
            return curationDao;
        }

        public void setCurationDao(CurationDao curationDao) {
            this.curationDao = curationDao;
        }

        public UnimodDao getUnimodDao() {
            return unimodDao;
        }

        public void setUnimodDao(UnimodDao unimodDao) {
            this.unimodDao = unimodDao;
        }

        public SwiftDao getSwiftDao() {
            return swiftDao;
        }

        public void setSwiftDao(SwiftDao swiftDao) {
            this.swiftDao = swiftDao;
        }

        @Override
        public Worker create(Config config, DependencyResolver dependencies) {
            SearchDbWorker worker = new SearchDbWorker(searchDbDao, fastaDbDao, curationDao, unimodDao, swiftDao,
                    new File(config.getScaffoldModSet()));
            return worker;
        }
    }

    /**
     * Configuration for the factory
     */
    public static final class Config implements ResourceConfig {
        private DatabaseFactory.Config database;
        private String scaffoldModSet;

        public DatabaseFactory.Config getDatabase() {
            return database;
        }

        public String getScaffoldModSet() {
            return scaffoldModSet;
        }

        @Override
        public Map<String, String> save(DependencyResolver resolver) {
            Map<String, String> map = new TreeMap<String, String>();
            map.put(DATABASE, resolver.getIdFromConfig(database));
            map.put(SCAFFOLD_MOD_SET, scaffoldModSet);
            return map;
        }

        @Override
        public void load(Map<String, String> values, DependencyResolver resolver) {
            database = (DatabaseFactory.Config) resolver.getConfigFromId(values.get(DATABASE));
            scaffoldModSet = values.get(SCAFFOLD_MOD_SET);
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    public static final class Ui implements ServiceUiFactory {

        @Override
        public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
            DatabaseFactory.Config database = (DatabaseFactory.Config) daemon.firstResourceOfType(DatabaseFactory.Config.class);

            builder.property(DATABASE, "Database", "Database we will be storing data into")
                    .reference(DatabaseFactory.TYPE, UiBuilder.NONE_TYPE)
                    .defaultValue(database);

            builder.property(SCAFFOLD_MOD_SET, "Scaffold's unimod.xml", "A location of Scaffold's current unimod.xml file.<p>" +
                    "On Linux it is typically in <tt>/opt/Scaffold3/parameters/unimod.xml.<p>" +
                    "The file has to be available where the searcher is running, so you might have to copy it elsewhere first.")
                    .existingFile()
                    .required();
        }
    }
}
