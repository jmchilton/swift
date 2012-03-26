package edu.mayo.mprc.peaks;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.enginedeployment.DeploymentRequest;
import edu.mayo.mprc.enginedeployment.DeploymentResult;
import edu.mayo.mprc.enginedeployment.DeploymentService;
import edu.mayo.mprc.peaks.core.Peaks;
import edu.mayo.mprc.peaks.core.PeaksAdmin;
import edu.mayo.mprc.peaks.core.PeaksDatabase;
import edu.mayo.mprc.peaks.core.PeaksURIs;
import edu.mayo.mprc.utilities.progress.ProgressReporter;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Peaks database deployer.
 */
public final class PeaksDeploymentService extends DeploymentService<DeploymentResult> {
	public static final String TYPE = "peaksDeployer";
	public static final String NAME = "Peaks DB Deployer";
	public static final String DESC = "Uploads the database into Peaks Online. You can run Peaks Online without this, uploading the databases manually.";
	private Peaks peaks;
	private String databaseFormat;

	public PeaksDeploymentService(final Peaks peaks, final String databaseFormat) {
		this.peaks = peaks;
		this.databaseFormat = databaseFormat;
	}

	public DeploymentResult performDeployment(final DeploymentRequest request) {
		final DeploymentResult deploymentResult = new DeploymentResult();

		try {
			if (peaks != null) {

				final PeaksAdmin peaksOnlineAdmin = peaks.getPeaksOnlineAdmin();

				for (final PeaksDatabase peaksOnlineDatabase : peaksOnlineAdmin.getAllDatabases()) {
					if (peaksOnlineDatabase.getDatabaseFilePath().equals(request.getCurationFile().getAbsolutePath())) {
						deploymentResult.addMessage("Database " + request.getShortName() + " has already been deployed.");
						return deploymentResult;
					}
				}

				if (peaksOnlineAdmin.addDatabase(request.getShortName()
						, request.getCurationFile().getAbsolutePath()
						, databaseFormat, false)) {

					for (final PeaksDatabase peaksOnlineDatabase : peaksOnlineAdmin.getAllDatabases()) {
						if (peaksOnlineDatabase.getDatabaseName().equals(request.getShortName())) {
							deploymentResult.addMessage("Database " + request.getShortName() + " successfully deployed.");
						}
					}
				} else {
					throw new MprcException("Peaks database deployment failed. Database unique name is " + request.getShortName() + ".");
				}
			} else {
				throw new MprcException("Peaks online client is not set. Set Peaks client and try again.");
			}
		} catch (Exception e) {
			throw new MprcException("Error while processing Peaks database request.", e);
		}

		return deploymentResult;
	}

	@Override
	public DeploymentResult performUndeployment(final DeploymentRequest request) {
		return null; //TODO: implement me
	}

	public Peaks getPeaks() {
		return peaks;
	}

	public void setPeaks(final Peaks peaks) {
		this.peaks = peaks;
	}

	public String getDatabaseFormat() {
		return databaseFormat;
	}

	public void setDatabaseFormat(final String databaseFormat) {
		this.databaseFormat = databaseFormat;
	}

	private static final Map<String, List<ProgressReporter>> coDeployments = new HashMap<String, List<ProgressReporter>>();

	public Map<String, List<ProgressReporter>> getCoDeployments() {
		return coDeployments;
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {

		private String databaseFormat;

		public String getDatabaseFormat() {
			return databaseFormat;
		}

		public void setDatabaseFormat(final String databaseFormat) {
			this.databaseFormat = databaseFormat;
		}

		@Override
		public Worker create(final Config config, final DependencyResolver dependencies) {
			PeaksDeploymentService worker = null;
			try {
				worker = new PeaksDeploymentService(new Peaks(new PeaksURIs(new URI(config.getBaseURI())), config.getUserName(), config.getPassword()), databaseFormat);
			} catch (Exception e) {
				throw new MprcException("Peaks deployment service worker could not be created.", e);
			}
			return worker;
		}
	}

	public static final class Config extends PeaksConfig {
		public Config() {
		}

		public Config(final String baseURI, final String userName, final String password) {
			super(baseURI, userName, password);
		}
	}

	public static final class Ui implements ServiceUiFactory {
		public void createUI(final DaemonConfig daemon, final ResourceConfig resource, final UiBuilder builder) {
			builder.property("baseURI", "Base URL", "Peaks Online base URL").required()
					.property("userName", "User name", "Administrator account user name").required()
					.property("password", "Password", "Administrator account password").required();
		}
	}
}
