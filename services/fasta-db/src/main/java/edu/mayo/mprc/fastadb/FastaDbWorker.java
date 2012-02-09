package edu.mayo.mprc.fastadb;

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
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;

import java.util.Map;
import java.util.TreeMap;

/**
 * Wrapper around the {@link FastaDbDao#addFastaDatabase} API.
 * <p/>
 * This worker is separate module so we can:
 * <ul>
 * <li>run the database load long before we have search results</li>
 * <li>can run the load on the same node as the database to minimize network traffic</li>
 * </ul>
 *
 * @author Roman Zenka
 */
public class FastaDbWorker implements Worker {
    public static final String TYPE = "fasta-db";
    public static final String NAME = "Database of FASTA entries";
    public static final String DESC = "Loads the FASTA files into a database for easier management.";

    private static final String DATABASE = "database";

    private FastaDbDao fastaDbDao;
    private CurationDao curationDao;

    public FastaDbWorker(FastaDbDao fastaDbDao, CurationDao curationDao) {
        this.fastaDbDao = fastaDbDao;
        this.curationDao = curationDao;
    }

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
        FastaDbWorkPacket workPacket = (FastaDbWorkPacket) wp;
        curationDao.begin();
        try {
            Curation database = curationDao.getCuration(workPacket.getCurationId());
            if (database == null) {
                throw new MprcException("Curation #" + workPacket.getCurationId() + " is not in the database.");
            }
            fastaDbDao.addFastaDatabase(database, reporter);
            curationDao.commit();
        } catch (Exception e) {
            curationDao.rollback();
            throw new MprcException("Could not load curation #" + workPacket.getCurationId() + " into the database", e);
        }
    }

    /**
     * A factory capable of creating the worker
     */
    public static final class Factory extends WorkerFactoryBase<Config> {
        private FastaDbDao fastaDbDao;
        private CurationDao curationDao;

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

        @Override
        public Worker create(Config config, DependencyResolver dependencies) {
            FastaDbWorker worker = new FastaDbWorker(fastaDbDao, curationDao);
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
