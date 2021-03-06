package edu.mayo.mprc.dbundeploy;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ServiceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Worker that processes database undeployment request. Given a request, it processes
 * by creating a DatabaseUndeployerRunner object and executing its run() method.
 */
public final class DatabaseUndeployerWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(DatabaseUndeployerWorker.class);

	public static final String TYPE = "databaseUndeployer";
	public static final String NAME = "Database Undeployer";
	public static final String DESC = "Removes previously deployed search databases from defined database deployers.";
	private FileTokenFactory fileTokenFactory;

	private DaemonConnection mascotDeployerDaemon;
	private DaemonConnection omssaDeployerDaemon;
	private DaemonConnection tandemDeployerDaemon;
	private DaemonConnection sequestDeployerDaemon;
	private DaemonConnection scaffoldDeployerDaemon;
	private DaemonConnection scaffold3DeployerDaemon;
	private DaemonConnection peaksDeployerDaemon;
	private CurationDao curationDao;

	private static final String MASCOT_DEPLOYER = "mascotDeployer";
	private static final String TANDEM_DEPLOYER = "tandemDeployer";
	private static final String SEQUEST_DEPLOYER = "sequestDeployer";
	private static final String OMSSA_DEPLOYER = "omssaDeployer";
	private static final String SCAFFOLD_DEPLOYER = "scaffoldDeployer";
	private static final String SCAFFOLD3_DEPLOYER = "scaffoldDeployer";
	private static final String PEAKS_DEPLOYER = "peaksDeployer";

	public DatabaseUndeployerWorker(FileTokenFactory fileTokenFactory, CurationDao curationDao) {
		this.fileTokenFactory = fileTokenFactory;
		this.curationDao = curationDao;
	}

	public DaemonConnection getMascotDeployerDaemon() {
		return mascotDeployerDaemon;
	}

	public void setMascotDeployerDaemon(DaemonConnection mascotDeployerDaemon) {
		this.mascotDeployerDaemon = mascotDeployerDaemon;
	}

	public DaemonConnection getOmssaDeployerDaemon() {
		return omssaDeployerDaemon;
	}

	public void setOmssaDeployerDaemon(DaemonConnection omssaDeployerDaemon) {
		this.omssaDeployerDaemon = omssaDeployerDaemon;
	}

	public DaemonConnection getTandemDeployerDaemon() {
		return tandemDeployerDaemon;
	}

	public void setTandemDeployerDaemon(DaemonConnection tandemDeployerDaemon) {
		this.tandemDeployerDaemon = tandemDeployerDaemon;
	}

	public DaemonConnection getSequestDeployerDaemon() {
		return sequestDeployerDaemon;
	}

	public void setSequestDeployerDaemon(DaemonConnection sequestDeployerDaemon) {
		this.sequestDeployerDaemon = sequestDeployerDaemon;
	}

	public DaemonConnection getScaffoldDeployerDaemon() {
		return scaffoldDeployerDaemon;
	}

	public void setScaffoldDeployerDaemon(DaemonConnection scaffoldDeployerDaemon) {
		this.scaffoldDeployerDaemon = scaffoldDeployerDaemon;
	}

	public DaemonConnection getPeaksDeployerDaemon() {
		return peaksDeployerDaemon;
	}

	public void setPeaksDeployerDaemon(DaemonConnection peaksDeployerDaemon) {
		this.peaksDeployerDaemon = peaksDeployerDaemon;
	}

	public DaemonConnection getScaffold3DeployerDaemon() {
		return scaffold3DeployerDaemon;
	}

	public void setScaffold3DeployerDaemon(DaemonConnection scaffold3DeployerDaemon) {
		this.scaffold3DeployerDaemon = scaffold3DeployerDaemon;
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

	protected void process(WorkPacket workPacket, ProgressReporter progressReporter) {
		DatabaseUndeployerWorkPacket undeployerWorkPacket = (DatabaseUndeployerWorkPacket) workPacket;

		DatabaseUndeployerRunner undeployerRunner = new DatabaseUndeployerRunner(undeployerWorkPacket
				, getMascotDeployerDaemon(), getOmssaDeployerDaemon()
				, getTandemDeployerDaemon(), getSequestDeployerDaemon(), getScaffoldDeployerDaemon()
				, getPeaksDeployerDaemon(), fileTokenFactory);

		undeployerRunner.run();

		DatabaseUndeployerResult undeployerResult = undeployerRunner.getDatabaseUndeployerResult();

		boolean success = true;

		for (UndeploymentTaskResult undeploymentResult : undeployerResult.getDatabaseUndeployerResults().values()) {
			success = success && undeploymentResult.wasSuccessful();
		}

		if (success) {
			curationDao.begin();
			try {
				final Curation curation = curationDao.findCuration(undeployerWorkPacket.getDbToUndeploy().getShortName());
				curationDao.deleteCuration(curation, new Change("Undeploying curation " + curation.getShortName(), new Date()));
				curationDao.commit();
			} catch (Exception e) {
				curationDao.rollback();
				// TODO: This needs to be reported to the user
				LOGGER.error("Could not delete curation from the database", e);
			}
		}

		progressReporter.reportProgress(undeployerResult);
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		private FileTokenFactory fileTokenFactory;
		private CurationDao curationDao;

		@Override
		public Worker create(Config config, DependencyResolver dependencies) {
			DatabaseUndeployerWorker worker = new DatabaseUndeployerWorker(fileTokenFactory, curationDao);

			if (config.mascotDeployer != null) {
				worker.setMascotDeployerDaemon((DaemonConnection) dependencies.createSingleton(config.mascotDeployer));
			}
			if (config.tandemDeployer != null) {
				worker.setTandemDeployerDaemon((DaemonConnection) dependencies.createSingleton(config.tandemDeployer));
			}
			if (config.sequestDeployer != null) {
				worker.setSequestDeployerDaemon((DaemonConnection) dependencies.createSingleton(config.sequestDeployer));
			}
			if (config.omssaDeployer != null) {
				worker.setOmssaDeployerDaemon((DaemonConnection) dependencies.createSingleton(config.omssaDeployer));
			}
			if (config.scaffoldDeployer != null) {
				worker.setScaffoldDeployerDaemon((DaemonConnection) dependencies.createSingleton(config.scaffoldDeployer));
			}
			if (config.scaffold3Deployer != null) {
				worker.setScaffold3DeployerDaemon((DaemonConnection) dependencies.createSingleton(config.scaffold3Deployer));
			}
			if (config.peaksDeployer != null) {
				worker.setPeaksDeployerDaemon((DaemonConnection) dependencies.createSingleton(config.peaksDeployer));
			}

			return worker;
		}

		public FileTokenFactory getFileTokenFactory() {
			return fileTokenFactory;
		}

		public void setFileTokenFactory(FileTokenFactory fileTokenFactory) {
			this.fileTokenFactory = fileTokenFactory;
		}

		public CurationDao getCurationDao() {
			return curationDao;
		}

		public void setCurationDao(CurationDao curationDao) {
			this.curationDao = curationDao;
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {
		private ServiceConfig scaffoldDeployer;
		private ServiceConfig scaffold3Deployer;
		private ServiceConfig omssaDeployer;
		private ServiceConfig sequestDeployer;
		private ServiceConfig tandemDeployer;
		private ServiceConfig mascotDeployer;
		private ServiceConfig peaksDeployer;

		public Config() {
		}

		public Config(ServiceConfig scaffoldDeployer, ServiceConfig scaffold3Deployer, ServiceConfig omssaDeployer, ServiceConfig sequestDeployer, ServiceConfig tandemDeployer, ServiceConfig mascotDeployer, ServiceConfig peaksDeployer) {
			this.scaffoldDeployer = scaffoldDeployer;
			this.scaffold3Deployer = scaffold3Deployer;
			this.omssaDeployer = omssaDeployer;
			this.sequestDeployer = sequestDeployer;
			this.tandemDeployer = tandemDeployer;
			this.mascotDeployer = mascotDeployer;
			this.peaksDeployer = peaksDeployer;
		}

		public ServiceConfig getScaffoldDeployer() {
			return scaffoldDeployer;
		}

		public void setScaffoldDeployer(ServiceConfig scaffoldDeployer) {
			this.scaffoldDeployer = scaffoldDeployer;
		}

		public ServiceConfig getScaffold3Deployer() {
			return scaffold3Deployer;
		}

		public void setScaffold3Deployer(ServiceConfig scaffold3Deployer) {
			this.scaffold3Deployer = scaffold3Deployer;
		}

		public ServiceConfig getOmssaDeployer() {
			return omssaDeployer;
		}

		public void setOmssaDeployer(ServiceConfig omssaDeployer) {
			this.omssaDeployer = omssaDeployer;
		}

		public ServiceConfig getSequestDeployer() {
			return sequestDeployer;
		}

		public void setSequestDeployer(ServiceConfig sequestDeployer) {
			this.sequestDeployer = sequestDeployer;
		}

		public ServiceConfig getTandemDeployer() {
			return tandemDeployer;
		}

		public void setTandemDeployer(ServiceConfig tandemDeployer) {
			this.tandemDeployer = tandemDeployer;
		}

		public ServiceConfig getMascotDeployer() {
			return mascotDeployer;
		}

		public void setMascotDeployer(ServiceConfig mascotDeployer) {
			this.mascotDeployer = mascotDeployer;
		}

		public ServiceConfig getPeaksDeployer() {
			return peaksDeployer;
		}

		public void setPeaksDeployer(ServiceConfig peaksDeployer) {
			this.peaksDeployer = peaksDeployer;
		}

		@Override
		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put(MASCOT_DEPLOYER, resolver.getIdFromConfig(mascotDeployer));
			map.put(TANDEM_DEPLOYER, resolver.getIdFromConfig(tandemDeployer));
			map.put(SEQUEST_DEPLOYER, resolver.getIdFromConfig(sequestDeployer));
			map.put(OMSSA_DEPLOYER, resolver.getIdFromConfig(omssaDeployer));
			map.put(SCAFFOLD_DEPLOYER, resolver.getIdFromConfig(scaffoldDeployer));
			map.put(SCAFFOLD3_DEPLOYER, resolver.getIdFromConfig(scaffold3Deployer));
			map.put(PEAKS_DEPLOYER, resolver.getIdFromConfig(peaksDeployer));
			return map;
		}

		public void load(Map<String, String> values, DependencyResolver resolver) {
			mascotDeployer = (ServiceConfig) resolver.getConfigFromId(values.get(MASCOT_DEPLOYER));
			tandemDeployer = (ServiceConfig) resolver.getConfigFromId(values.get(TANDEM_DEPLOYER));
			sequestDeployer = (ServiceConfig) resolver.getConfigFromId(values.get(SEQUEST_DEPLOYER));
			omssaDeployer = (ServiceConfig) resolver.getConfigFromId(values.get(OMSSA_DEPLOYER));
			scaffoldDeployer = (ServiceConfig) resolver.getConfigFromId(values.get(SCAFFOLD_DEPLOYER));
			scaffold3Deployer = (ServiceConfig) resolver.getConfigFromId(values.get(SCAFFOLD3_DEPLOYER));
			peaksDeployer = (ServiceConfig) resolver.getConfigFromId(values.get(PEAKS_DEPLOYER));
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {

		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder.property(SEQUEST_DEPLOYER, "Sequest Database Deployer", "Database deployer must provide database undeployment functionality.")
					.reference("sequestDeployer", UiBuilder.NONE_TYPE)

					.property(TANDEM_DEPLOYER, "X!Tandem Database Deployer", "Database deployer must provide database undeployment functionality.")
					.reference("tandemDeployer", UiBuilder.NONE_TYPE)

					.property(OMSSA_DEPLOYER, "Omssa Database Deployer", "Database deployer must provide database undeployment functionality.")
					.reference("omssaDeployer", UiBuilder.NONE_TYPE)

					.property(PEAKS_DEPLOYER, "Peaks Database Deployer", "Database deployer must provide database undeployment functionality.")
					.reference("peaksDeployer", UiBuilder.NONE_TYPE)

					.property(SCAFFOLD_DEPLOYER, "Scaffold Database Deployer", "Database deployer must provide database undeployment functionality.")
					.reference("scaffoldDeployer", UiBuilder.NONE_TYPE)

					.property(SCAFFOLD3_DEPLOYER, "Scaffold3 Database Deployer", "Database deployer must provide database undeployment functionality.")
					.reference("scaffold3Deployer", UiBuilder.NONE_TYPE)

					.property(MASCOT_DEPLOYER, "Mascot Database Deployer", "Database deployer must provide database undeployment functionality.")
					.reference("mascotDeployer", UiBuilder.NONE_TYPE);
		}
	}
}
