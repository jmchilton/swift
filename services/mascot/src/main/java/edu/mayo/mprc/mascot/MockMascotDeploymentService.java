package edu.mayo.mprc.mascot;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.enginedeployment.DeploymentResult;
import edu.mayo.mprc.utilities.progress.ProgressReporter;

import java.util.Map;
import java.util.TreeMap;

/**
 * Test-only mascot deployment. Creates extremely simplistic {@link edu.mayo.mprc.enginedeployment.DeploymentResult}
 * for given request.
 */
public final class MockMascotDeploymentService implements Worker {
	public static final String TYPE = "mockMascotDeployer";
	public static final String NAME = "Mock Mascot DB Deployer";
	public static final String DESC = "If for some reason you cannot deploy new databases to mascot, use this 'mock deployer' that pretends the database was already deployed. You need to load the databases from Mascot to Swift before using this.";

	public MockMascotDeploymentService() {
	}

	public void processRequest(final WorkPacket workPacket, final ProgressReporter progressReporter) {
		// Send empty result (nobody cares anyway)
		try {
			progressReporter.reportStart();
			final DeploymentResult result = new DeploymentResult();
			progressReporter.reportProgress(result);
			workPacket.synchronizeFileTokensOnReceiver();
			progressReporter.reportSuccess();
		} catch (Exception t) {
			progressReporter.reportFailure(t);
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {

		public Config() {
		}

		@Override
		public Map<String, String> save(final DependencyResolver resolver) {
			return new TreeMap<String, String>();
		}

		@Override
		public void load(final Map<String, String> values, final DependencyResolver resolver) {
			//Do nothing
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		@Override
		public Worker create(final Config config, final DependencyResolver dependencies) {
			return new MockMascotDeploymentService();
		}
	}

	public static final class Ui implements ServiceUiFactory {
		public void createUI(final DaemonConfig daemon, final ResourceConfig resource, final UiBuilder builder) {
			// No UI needed
		}
	}
}
