package edu.mayo.mprc.dbundeploy;

import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executes undeployment task on available search engines.
 *
 * @deprecated This is implemented in too complicated fashion
 */
public final class DatabaseUndeployerRunner implements Runnable {

	private DatabaseUndeployerWorkPacket undeployerWorkPacket;
	private FileTokenFactory fileTokenFactory;

	private DaemonConnection mascotDeployerDaemon;
	private DaemonConnection omssaDeployerDaemon;
	private DaemonConnection tandemDeployerDaemon;
	private DaemonConnection sequestDeployerDaemon;
	private DaemonConnection scaffoldDeployerDaemon;
	private DaemonConnection peaksDeployerDaemon;

	private final ExecutorService executorService = Executors.newFixedThreadPool(6);

	/**
	 * Key: daemon connection name.
	 * Value: undeployment task.
	 */
	private Map<String, DatabaseUndeploymentTask> undeploymentNameTaskPairs;

	private DatabaseUndeployerResult undeployerResult;

	private final AtomicInteger taskCounter;

	private static final Logger LOGGER = Logger.getLogger(DatabaseUndeployerRunner.class);

	private DatabaseUndeployerState state;
	private static final int POLLING_INTERVAL = 10000;

	public DatabaseUndeployerRunner(DatabaseUndeployerWorkPacket undeployerWorkPacket, DaemonConnection mascotDeployerDaemon, DaemonConnection omssaDeployerDaemon, DaemonConnection tandemDeployerDaemon, DaemonConnection sequestDeployerDaemon, DaemonConnection scaffoldDeployerDaemon, DaemonConnection peaksDeployerDaemon, FileTokenFactory fileTokenFactory) {
		this.undeployerWorkPacket = undeployerWorkPacket;
		this.fileTokenFactory = fileTokenFactory;
		this.mascotDeployerDaemon = mascotDeployerDaemon;
		this.omssaDeployerDaemon = omssaDeployerDaemon;
		this.tandemDeployerDaemon = tandemDeployerDaemon;
		this.sequestDeployerDaemon = sequestDeployerDaemon;
		this.scaffoldDeployerDaemon = scaffoldDeployerDaemon;
		this.peaksDeployerDaemon = peaksDeployerDaemon;

		undeploymentNameTaskPairs = new HashMap<String, DatabaseUndeploymentTask>();
		undeployerResult = new DatabaseUndeployerResult();

		taskCounter = new AtomicInteger(0);

		state = DatabaseUndeployerState.NOTSTARTED;
	}

	@Override
	public void run() {
		synchronized (taskCounter) {
			createTasks();

			if (undeploymentNameTaskPairs.size() > 0) {
				state = DatabaseUndeployerState.RUNNING;
			} else {
				state = DatabaseUndeployerState.DONE;
			}

			taskCounter.addAndGet(undeploymentNameTaskPairs.size());
		}

		for (Map.Entry<String, DatabaseUndeploymentTask> me : undeploymentNameTaskPairs.entrySet()) {
			me.getValue().run();
			executorService.execute(new MyUndeploymentTaskMonitor(me.getKey()));
		}
	}

	public DatabaseUndeployerState getDatabaseUndeployerState() {
		return state;
	}

	public DatabaseUndeployerResult getDatabaseUndeployerResult() {
		synchronized (taskCounter) {
			while (taskCounter.get() > 0) {
				try {
					taskCounter.wait(POLLING_INTERVAL);
				} catch (InterruptedException e) {
					LOGGER.warn("Exception occurred while waiting for completion of undeployment tasks.", e);
				}
			}

			if (state == DatabaseUndeployerState.RUNNING) {
				state = DatabaseUndeployerState.DONE;
			}
		}

		return undeployerResult;
	}

	private void createTasks() {
		if (mascotDeployerDaemon != null) {
			DatabaseUndeploymentTask databaseUndeploymentTask = new DatabaseUndeploymentTask(mascotDeployerDaemon, undeployerWorkPacket.getDbToUndeploy(), fileTokenFactory);
			undeploymentNameTaskPairs.put(mascotDeployerDaemon.getConnectionName(), databaseUndeploymentTask);
		}

		if (scaffoldDeployerDaemon != null) {
			DatabaseUndeploymentTask databaseUndeploymentTask = new DatabaseUndeploymentTask(scaffoldDeployerDaemon, undeployerWorkPacket.getDbToUndeploy(), fileTokenFactory);
			undeploymentNameTaskPairs.put(scaffoldDeployerDaemon.getConnectionName(), databaseUndeploymentTask);
		}

		if (sequestDeployerDaemon != null) {
			DatabaseUndeploymentTask databaseUndeploymentTask = new DatabaseUndeploymentTask(sequestDeployerDaemon, undeployerWorkPacket.getDbToUndeploy(), fileTokenFactory);
			undeploymentNameTaskPairs.put(sequestDeployerDaemon.getConnectionName(), databaseUndeploymentTask);
		}

		if (tandemDeployerDaemon != null) {
			DatabaseUndeploymentTask databaseUndeploymentTask = new DatabaseUndeploymentTask(tandemDeployerDaemon, undeployerWorkPacket.getDbToUndeploy(), fileTokenFactory);
			undeploymentNameTaskPairs.put(tandemDeployerDaemon.getConnectionName(), databaseUndeploymentTask);
		}

		if (omssaDeployerDaemon != null) {
			DatabaseUndeploymentTask databaseUndeploymentTask = new DatabaseUndeploymentTask(omssaDeployerDaemon, undeployerWorkPacket.getDbToUndeploy(), fileTokenFactory);
			undeploymentNameTaskPairs.put(omssaDeployerDaemon.getConnectionName(), databaseUndeploymentTask);
		}

		/**
		 * Once peaks undeployment code is finalized, uncomment block of code.
		 */
//		if (peaksDeployerDaemon != null) {
//			DatabaseUndeploymentTask databaseUndeploymentTask = new DatabaseUndeploymentTask(peaksDeployerDaemon, undeployerWorkPacket.getDbToUndeploy(), fileTokenFactory);
//			databaseUndeploymentTask.setWorkflowEngine(new WorkflowEngine(undeployerWorkPacket.getTaskId()));
//			undeploymentNameTaskPairs.put(peaksDeployerDaemon.getConnectionName(), databaseUndeploymentTask);
//		}
	}

	/**
	 * Monitors completion of database underployment task.
	 */
	private class MyUndeploymentTaskMonitor implements Runnable {

		private String daemonConnectionName;

		private MyUndeploymentTaskMonitor(String daemonConnectionName) {
			this.daemonConnectionName = daemonConnectionName;
		}

		@Override
		public void run() {
			UndeploymentTaskResult taskResult = undeploymentNameTaskPairs.get(daemonConnectionName).waitUntilDone();

			undeployerResult.addUndeploymentTaskResult(daemonConnectionName, taskResult);

			synchronized (taskCounter) {
				if (taskCounter.decrementAndGet() == 0) {
					taskCounter.notifyAll();
				}
			}
		}
	}
}
