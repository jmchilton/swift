package edu.mayo.mprc.daemon;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.mayo.mprc.config.*;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.utilities.progress.ProgressInfo;
import edu.mayo.mprc.utilities.progress.ProgressReporter;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

/**
 * Runs {@link edu.mayo.mprc.daemon.Worker} instances either
 * <ul>
 * <li>using specified {@link java.util.concurrent.ExecutorService} and
 * {@link WorkerFactory}</li>
 * <li>in a single thread when {@link #setWorker(edu.mayo.mprc.daemon.Worker)} method is used.</li>
 * </ul>
 */
public final class SimpleRunner extends AbstractRunner {
	private static final Logger LOGGER = Logger.getLogger(SimpleRunner.class);
	public static final String TYPE = "localRunner";
	public static final String NAME = "Local Runner";
	private boolean enabled = true;
	private boolean operational;
	private WorkerFactory factory;
	private ExecutorService executorService;
	private File logOutputFolder;
	private DaemonConnection daemonConnection = null;

	public SimpleRunner() {
		super();
	}

	@Override
	public String toString() {
		return "Daemon Runner for " + factory.getDescription();
	}

	protected void processRequest(final DaemonRequest request) {
		final Worker worker = factory.createWorker();
		executorService.execute(new RequestProcessor(worker, request));
	}

	public void stop() {
		super.stop();
		executorService.shutdownNow();
	}

	public WorkerFactory getFactory() {
		return factory;
	}

	public void setFactory(WorkerFactory factory) {
		this.factory = factory;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public File getLogOutputFolder() {
		return logOutputFolder;
	}

	public void setLogOutputFolder(File logOutputFolder) {
		this.logOutputFolder = logOutputFolder;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isOperational() {
		return operational;
	}

	public void setOperational(boolean operational) {
		this.operational = operational;
	}

	public DaemonConnection getDaemonConnection() {
		return daemonConnection;
	}

	public void setDaemonConnection(DaemonConnection daemonConnection) {
		this.daemonConnection = daemonConnection;
	}

	/**
	 * Special case - we have only a single worker, so we will be running in a single thread.
	 * In this case the user does not have to specify (@link #factory} - we will create a trivial one for them.
	 *
	 * @param worker Worker to execute in the single thread.
	 */
	public void setWorker(Worker worker) {
		this.executorService = new SimpleThreadPoolExecutor(1,
				(daemonConnection != null ? daemonConnection.getConnectionName() : worker.getClass().getSimpleName()) + "-runner");
		this.factory = new MyWorkerFactory(worker);
	}

	/**
	 * Fake factory - single worker in a single thread has the same worker object recycled all the time
	 */
	private static final class MyWorkerFactory implements WorkerFactory {
		private final Worker finalWorker;

		public MyWorkerFactory(Worker finalWorker) {
			this.finalWorker = finalWorker;
		}

		public Worker createWorker() {
			return finalWorker;
		}

		public String getDescription() {
			return finalWorker.toString();
		}
	}

	private final class MyProgressReporter implements ProgressReporter {
		private final DaemonRequest request;
		private final LoggingSetup loggingSetup;

		private MyProgressReporter(DaemonRequest request, LoggingSetup loggingSetup) {
			this.request = request;
			this.loggingSetup = loggingSetup;
		}

		@Override
		public void reportStart() {
			sendResponse(request, new DaemonProgressMessage(DaemonProgress.RequestProcessingStarted), false);
			if (loggingSetup != null) {
				sendResponse(request, new DaemonProgressMessage(DaemonProgress.UserSpecificProgressInfo,
						new AssignedTaskData(loggingSetup.getStandardOutFile(), loggingSetup.getStandardErrorFile())), false);
			}
		}

		public void reportProgress(ProgressInfo progressInfo) {
			sendResponse(request, new DaemonProgressMessage(DaemonProgress.UserSpecificProgressInfo, progressInfo), false);
		}

		public void reportSuccess() {
			sendResponse(request, new DaemonProgressMessage(DaemonProgress.RequestCompleted), true);
		}

		public void reportFailure(Throwable t) {
			sendResponse(request, t, true);
		}
	}

	@XStreamAlias("simpleDaemonRunner")
	public static final class Config extends RunnerConfig {
		private int numThreads = 1;
		private String logOutputFolder = ".";

		public Config() {
		}

		public Config(ResourceConfig workerFactory) {
			super(workerFactory);
		}

		public int getNumThreads() {
			return numThreads;
		}

		public void setNumThreads(int numThreads) {
			this.numThreads = numThreads;
		}

		public String getLogOutputFolder() {
			return logOutputFolder;
		}

		public void setLogOutputFolder(String logOutputFolder) {
			this.logOutputFolder = logOutputFolder;
		}

		@Override
		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put("numThreads", String.valueOf(numThreads));
			map.put("logOutputFolder", logOutputFolder);
			return map;
		}

		@Override
		public void load(Map<String, String> values, DependencyResolver resolver) {
			String numThreadsString = values.get("numThreads");
			numThreads = Integer.parseInt(numThreadsString);

			logOutputFolder = values.get("logOutputFolder");
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class SimpleDaemonRunnerFactory extends FactoryBase<Config, SimpleRunner> {
		private MultiFactory table;

		@Override
		public SimpleRunner create(Config config, DependencyResolver dependencies) {
			SimpleRunner runner = new SimpleRunner();

			runner.setEnabled(true);

			ResourceConfig workerFactoryConfig = config.getWorkerConfiguration();

			runner.setFactory(getWorkerFactory(getTable(), workerFactoryConfig, dependencies));
			final int numThreads = config.getNumThreads();
			runner.setExecutorService(new SimpleThreadPoolExecutor(numThreads, runner.getFactory().getDescription()));
			// Important to convert the log file to absolute, otherwise user.dir is not taken into account and
			// the behavior is inconsistent within the IDE while debugging
			runner.setLogOutputFolder(new File(config.getLogOutputFolder()).getAbsoluteFile());

			return runner;
		}

		public void setTable(MultiFactory table) {
			this.table = table;
		}

		public MultiFactory getTable() {
			return table;
		}

		private static WorkerFactoryBase getWorkerFactory(MultiFactory table, ResourceConfig config, DependencyResolver dependencies) {
			final WorkerFactoryBase factory =
					(WorkerFactoryBase) table.getFactory(config.getClass());
			// Since the factory by default does not even know what is it creating (that would require duplicating
			// the description into the factory), set the description to the user name of created module.
			factory.setDescription(table.getUserName(config));
			factory.setConfig(config);
			factory.setDependencies(dependencies);

			return factory;
		}
	}

	private final class RequestProcessor implements Runnable {
		private final Worker worker;
		private final DaemonRequest request;

		public RequestProcessor(Worker worker, DaemonRequest request) {
			this.worker = worker;
			this.request = request;
		}

		public void run() {
			LoggingSetup logging;
			if (worker instanceof NoLoggingWorker) {
				logging = null;
			} else {
				logging = new LoggingSetup(logOutputFolder);
				try {
					logging.startLogging();
				} catch (Exception e) {
					sendResponse(request, new DaemonException("Cannot establish logging for request", e), true);
				}

			}
			try {
				worker.processRequest(request.getWorkPacket(), new MyProgressReporter(request, logging));
			} catch (Exception t) {
				// SWALLOWED
				LOGGER.error("Exception was thrown when processing a request - that means a communication error, or badly implemented worker.\nWorkers must never thrown an exception - they must report a failure.", t);
			} finally {
				if (logging != null) {
					logging.stopLogging();
				}
			}
		}

	}
}
