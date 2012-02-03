package edu.mayo.mprc.searchdb;

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
import edu.mayo.mprc.searchdb.dao.SearchDbDao;

import java.util.Map;
import java.util.TreeMap;

/**
 * Can handle multiple requests:
 * <ul>
 * <li>{@link SearchDbWorkPacket} - loads a given Scaffold search result</li>
 * <li>{@link }</li>
 * </ul>
 */
public final class SearchDbWorker implements Worker {
    private DatabaseFactory.Config database;
    private SearchDbDao dao;

    public static final String TYPE = "search-db";
    public static final String NAME = "Database of search results";
    public static final String DESC = "Loads the search results into a database for fast future queries.";

    private static final String DATABASE = "database";

    public SearchDbWorker(SearchDbDao dao) {
        this.dao = dao;
    }

    @Override
    public void processRequest(WorkPacket workPacket, ProgressReporter progressReporter) {

    }

    /**
     * A factory capable of creating the worker
     */
    public static final class Factory extends WorkerFactoryBase<Config> {
        private SearchDbDao searchDbDao;

        public SearchDbDao getSearchDbDao() {
            return searchDbDao;
        }

        public void setSearchDbDao(SearchDbDao searchDbDao) {
            this.searchDbDao = searchDbDao;
        }

        @Override
        public Worker create(Config config, DependencyResolver dependencies) {
            SearchDbWorker worker = new SearchDbWorker(searchDbDao);
            return worker;
        }
    }

    /**
     * Configuration for the factory
     */
    public static final class Config implements ResourceConfig {
        private DatabaseFactory.Config database;

        public DatabaseFactory.Config getDatabase() {
            return database;
        }


        @Override
        public Map<String, String> save(DependencyResolver resolver) {
            Map<String, String> map = new TreeMap<String, String>();
            map.put(DATABASE, resolver.getIdFromConfig(database));
            return map;
        }

        @Override
        public void load(Map<String, String> values, DependencyResolver resolver) {
            database = (DatabaseFactory.Config) resolver.getConfigFromId(values.get(DATABASE));
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    public static final class Ui implements ServiceUiFactory {

        @Override
        public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
            //TODO: implement me
        }
    }
}
