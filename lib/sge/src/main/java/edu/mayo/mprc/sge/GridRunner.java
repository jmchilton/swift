package edu.mayo.mprc.sge;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.xml.DomDriver;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.FactoryBase;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.RunnerConfig;
import edu.mayo.mprc.daemon.*;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.messaging.rmi.BoundMessenger;
import edu.mayo.mprc.messaging.rmi.MessageListener;
import edu.mayo.mprc.messaging.rmi.MessengerFactory;
import edu.mayo.mprc.messaging.rmi.SimpleOneWayMessenger;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Daemon Runner that sends {@link GridDaemonWorkerAllocatorInputObject} objects to the Grid
 * to be proccess by the @{link DaemonWorkerAllocator} class. The {@link GridDaemonWorkerAllocatorInputObject} is
 * saved as a shared xml file and a {@link java.io.File} URI that represents the shared xml file
 * is sent through the Grid.
 */
public final class GridRunner extends AbstractRunner {

	public static final String TYPE = "sgeRunner";
	public static final String NAME = "Sun Grid Engine Runner";
	private boolean enabled;
	private boolean operational;
	private DaemonConnection daemonConnection;

	//Grid specific variables
	private GridEngineJobManager manager;
	private String nativeSpecification;
	private String queueName;
	private String memoryRequirement;
	private String wrapperScript;

	private File sharedWorkingDirectory;
	private File sharedTempDirectory;
	private File outputDirectory;
	private File sharedLogDirectory;
	private ResourceConfig workerFactoryConfig;

	private MessengerFactory messengerFactory;
	private GridScriptFactory gridScriptFactory;
	private FileTokenFactory fileTokenFactory;

	private static AtomicLong uniqueId = new AtomicLong(System.currentTimeMillis());

	private static final Logger LOGGER = Logger.getLogger(GridRunner.class);

	public GridRunner() {
		super();
	}

	public void stop() {
		super.stop();
		// Disables message processing
		enabled = false;
	}

	@Override
	public String toString() {
		return "Grid Daemon Runner for " + (daemonConnection == null ? "(null)" : daemonConnection.getConnectionName());
	}

	protected void processRequest(DaemonRequest request) {
		GridWorkPacket gridWorkPacket = getBaseGridWorkPacket(gridScriptFactory.getApplicationName(wrapperScript));
		File daemonWorkerAllocatorInputFile = new File(sharedTempDirectory, queueName + "_" + uniqueId.incrementAndGet());

		try {
			BoundMessenger<SimpleOneWayMessenger> boundMessenger = messengerFactory.createOneWayMessenger();
			DaemonWorkerAllocatorMessageListener allocatorListener = new DaemonWorkerAllocatorMessageListener(request);
			boundMessenger.getMessenger().addMessageListener(allocatorListener);

			GridDaemonWorkerAllocatorInputObject gridDaemonAllocatorInputObject =
					new GridDaemonWorkerAllocatorInputObject(request.getWorkPacket()
							, boundMessenger.getMessengerInfo()
							, workerFactoryConfig
							, fileTokenFactory.getDaemonConfigInfo()
							, fileTokenFactory.getFileSharingFactory().getBrokerUri());

			if (sharedTempDirectory != null) {
				gridDaemonAllocatorInputObject.setSharedTempDirectory(sharedTempDirectory.getAbsolutePath());
			}

			BufferedWriter bufferedWriter = null;

			try {
				XStream xStream = new XStream(new DomDriver());
				bufferedWriter = new BufferedWriter(new FileWriter(daemonWorkerAllocatorInputFile));
				bufferedWriter.write(xStream.toXML(gridDaemonAllocatorInputObject));
			} finally {
				FileUtilities.closeQuietly(bufferedWriter);
			}

			List<String> parameters = gridScriptFactory.getParameters(wrapperScript, daemonWorkerAllocatorInputFile);
			gridWorkPacket.setParameters(parameters);

			// Set our own listener to the work packet progress. When the packet returns, the execution will be resumed
			gridWorkPacket.setListener(new MyWorkPacketStateListener(request, daemonWorkerAllocatorInputFile, boundMessenger, allocatorListener));
			// Run the job
			String requestId = manager.passToGridEngine(gridWorkPacket);
			// Report the assigned ID
			sendResponse(request,
					new DaemonProgressMessage(DaemonProgress.UserSpecificProgressInfo, new AssignedTaskData(requestId, gridWorkPacket.getOutputLogFilePath(), gridWorkPacket.getErrorLogFilePath())),
					false);

			// We are not done yet! The grid work packet's progress listener will get called when the state of the task changes,
			// and either mark the task failed or successful.
		} catch (Exception t) {
			FileUtilities.quietDelete(daemonWorkerAllocatorInputFile);
			DaemonException daemonException = new DaemonException("Failed passing work packet " + gridWorkPacket.toString() + " to grid engine", t);
			sendResponse(request, daemonException, true);
			throw daemonException;
		}
	}

	/**
	 * Listens to RMI calls from the SGE daemon. None of the messages is final.
	 */
	private class DaemonWorkerAllocatorMessageListener implements MessageListener {
		private static final long serialVersionUID = 20090324L;
		private DaemonRequest request;
		private Throwable lastThrowable;

		public DaemonWorkerAllocatorMessageListener(DaemonRequest request) {
			this.request = request;
		}

		public synchronized Throwable getLastThrowable() {
			return lastThrowable;
		}

		public void messageReceived(Object message) {
			if (message instanceof Serializable) {
				if (message instanceof Throwable) {
					// We do send an error message now.
					// That is done once SGE detects termination of the process.
					synchronized (this) {
						lastThrowable = (Throwable) message;
					}
				} else {
					// Not final - a progress message
					sendResponse(request, (Serializable) message, false);
				}
			} else {
				sendResponse(request, "Progress message from DaemonWorkerAllocator " + message.toString(), false);
			}
		}
	}

	/**
	 * This listener is running within the grid engine monitor thread.
	 */
	private class MyWorkPacketStateListener implements GridWorkPacketStateListener {
		private boolean reported;
		private DaemonRequest request;
		private File daemonWorkerAllocatorInputFile;
		private BoundMessenger boundMessenger;
		private DaemonWorkerAllocatorMessageListener allocatorListener;

		/**
		 * @param allocatorListener The listener for the RMI messages. We use it so we can send an exception that was cached when SGE terminates.
		 */
		public MyWorkPacketStateListener(DaemonRequest request, File daemonWorkerAllocatorInputFile, BoundMessenger boundMessenger, DaemonWorkerAllocatorMessageListener allocatorListener) {
			reported = false;
			this.request = request;
			this.daemonWorkerAllocatorInputFile = daemonWorkerAllocatorInputFile;
			this.boundMessenger = boundMessenger;
			this.allocatorListener = allocatorListener;
		}

		/**
		 * Process message from the grid engine itself.
		 *
		 * @param w
		 */
		public void stateChanged(GridWorkPacket w) {
			// We report state change just once.
			if (!reported) {
				try {
					if (w.getPassed()) {
						// This is the last response we will send - request is completed.
						// There might have been an error from RMI, check that
						if (allocatorListener.getLastThrowable() == null) {
							sendResponse(request, new DaemonProgressMessage(DaemonProgress.RequestCompleted), true);
						} else {
							sendResponse(request, new DaemonException(allocatorListener.getLastThrowable()), true);
						}
					} else if (w.getFailed()) {
						// This is the last response we will send - request failed
						sendResponse(request, new DaemonException(w.getErrorMessage()), true);
					}
					reported = true;

					boundMessenger.dispose();
				} catch (IOException e) {
					LOGGER.warn("Error disposing messenger: " + boundMessenger.getMessengerInfo().getMessengerRemoteName(), e);
				} finally {
					if (w != null) {
						//Delete workPacket file
						LOGGER.debug("Deleting daemon worker allocator input temp file: " + daemonWorkerAllocatorInputFile.getAbsolutePath());
						FileUtilities.quietDelete(daemonWorkerAllocatorInputFile);
					}
				}
			}
		}
	}

	public boolean isOperational() {
		return operational || !enabled;
	}

	public void setOperational(boolean operational) {
		this.operational = operational;
	}

	private GridWorkPacket getBaseGridWorkPacket(String command) {
		GridWorkPacket gridWorkPacket = new GridWorkPacket(command, null);

		if (nativeSpecification != null) {
			gridWorkPacket.setNativeSpecification(nativeSpecification);
		}
		if (queueName != null) {
			gridWorkPacket.setJobQueue(queueName);
		}
		if (memoryRequirement != null) {
			gridWorkPacket.setForcedMemoryRequirement(memoryRequirement);
		}

		gridWorkPacket.setWorkingFolder(sharedWorkingDirectory.getAbsolutePath());
		gridWorkPacket.setLogFolder(FileUtilities.getDateBasedDirectory(sharedLogDirectory, new Date()).getAbsolutePath());

		return gridWorkPacket;
	}

	public File getSharedWorkingDirectory() {
		return sharedWorkingDirectory;
	}

	public void setSharedWorkingDirectory(File sharedWorkingDirectory) {
		this.sharedWorkingDirectory = sharedWorkingDirectory;
	}

	public File getSharedTempDirectory() {
		return sharedTempDirectory;
	}

	public void setSharedTempDirectory(File sharedTempDirectory) {
		this.sharedTempDirectory = sharedTempDirectory;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public File getSharedLogDirectory() {
		return sharedLogDirectory;
	}

	public void setSharedLogDirectory(File sharedLogDirectory) {
		this.sharedLogDirectory = sharedLogDirectory;
	}

	public GridEngineJobManager getManager() {
		return manager;
	}

	public void setManager(GridEngineJobManager manager) {
		this.manager = manager;
	}

	public ResourceConfig getWorkerFactoryConfig() {
		return workerFactoryConfig;
	}

	public void setWorkerFactoryConfig(ResourceConfig workerFactoryConfig) {
		this.workerFactoryConfig = workerFactoryConfig;
	}

	public String getNativeSpecification() {
		return nativeSpecification;
	}

	public void setNativeSpecification(String nativeSpecification) {
		this.nativeSpecification = nativeSpecification;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getMemoryRequirement() {
		return memoryRequirement;
	}

	public void setMemoryRequirement(String memoryRequirement) {
		this.memoryRequirement = memoryRequirement;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public MessengerFactory getMessengerFactory() {
		return messengerFactory;
	}

	public void setMessengerFactory(MessengerFactory messengerFactory) {
		this.messengerFactory = messengerFactory;
	}

	public GridScriptFactory getGridScriptFactory() {
		return gridScriptFactory;
	}

	public void setGridScriptFactory(GridScriptFactory gridScriptFactory) {
		this.gridScriptFactory = gridScriptFactory;
	}

	public DaemonConnection getDaemonConnection() {
		return daemonConnection;
	}

	public void setDaemonConnection(DaemonConnection daemonConnection) {
		this.daemonConnection = daemonConnection;
	}

	public String getWrapperScript() {
		return wrapperScript;
	}

	public void setWrapperScript(String wrapperScript) {
		this.wrapperScript = wrapperScript;
	}

	public FileTokenFactory getFileTokenFactory() {
		return fileTokenFactory;
	}

	public void setFileTokenFactory(FileTokenFactory fileTokenFactory) {
		this.fileTokenFactory = fileTokenFactory;
	}

	@XStreamAlias("gridDaemonRunner")
	public static final class Config extends RunnerConfig {
		private String queueName;
		private String memoryRequirement;
		private String nativeSpecification;
		private String sharedWorkingDirectory;
		private String sharedTempDirectory;
		private String sharedLogDirectory;
		private String wrapperScript;

		public Config() {
		}

		public Config(ResourceConfig workerFactory) {
			super(workerFactory);
		}

		public String getQueueName() {
			return queueName;
		}

		public void setQueueName(String queueName) {
			this.queueName = queueName;
		}

		public String getMemoryRequirement() {
			return memoryRequirement;
		}

		public void setMemoryRequirement(String memoryRequirement) {
			this.memoryRequirement = memoryRequirement;
		}

		public String getNativeSpecification() {
			return nativeSpecification;
		}

		public String getSharedWorkingDirectory() {
			return sharedWorkingDirectory;
		}

		public void setSharedWorkingDirectory(String sharedWorkingDirectory) {
			this.sharedWorkingDirectory = sharedWorkingDirectory;
		}

		public void setNativeSpecification(String nativeSpecification) {
			this.nativeSpecification = nativeSpecification;
		}

		public String getWrapperScript() {
			return wrapperScript;
		}

		public void setWrapperScript(String wrapperScript) {
			this.wrapperScript = wrapperScript;
		}

		public String getSharedTempDirectory() {
			return sharedTempDirectory;
		}

		public void setSharedTempDirectory(String sharedTempDirectory) {
			this.sharedTempDirectory = sharedTempDirectory;
		}

		public String getSharedLogDirectory() {
			return sharedLogDirectory;
		}

		public void setSharedLogDirectory(String sharedLogDirectory) {
			this.sharedLogDirectory = sharedLogDirectory;
		}

		public Map<String, String> save(DependencyResolver resolver) {
			TreeMap<String, String> map = new TreeMap<String, String>();
			map.put("queueName", queueName);
			map.put("memoryRequirement", memoryRequirement);
			map.put("nativeSpecification", nativeSpecification);
			map.put("sharedWorkingDirectory", sharedWorkingDirectory);
			map.put("sharedTempDirectory", sharedTempDirectory);
			map.put("sharedLogDirectory", sharedLogDirectory);
			map.put("wrapperScript", wrapperScript);
			return map;
		}

		public void load(Map<String, String> values, DependencyResolver resolver) {
			queueName = values.get("queueName");
			memoryRequirement = values.get("memoryRequirement");
			nativeSpecification = values.get("nativeSpecification");
			sharedWorkingDirectory = values.get("sharedWorkingDirectory");
			sharedTempDirectory = values.get("sharedTempDirectory");
			sharedLogDirectory = values.get("sharedLogDirectory");
			wrapperScript = values.get("wrapperScript");
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Factory extends FactoryBase<Config, GridRunner> {

		private GridEngineJobManager gridEngineManager;
		private GridScriptFactory gridScriptFactory;
		private MessengerFactory messengerFactory;
		private FileTokenFactory fileTokenFactory;

		@Override
		public GridRunner create(Config config, DependencyResolver dependencies) {
			// Check that SGE is initialized. If it cannot initialize, we cannot create the runners
			gridEngineManager.initialize();

			GridRunner runner = new GridRunner();

			runner.setEnabled(true);

			runner.setQueueName(config.getQueueName());

			if (config.getMemoryRequirement() != null) {
				runner.setMemoryRequirement(config.getMemoryRequirement());
			}

			if (config.getNativeSpecification() != null) {
				runner.setNativeSpecification(config.getNativeSpecification());
			}

			runner.setGridScriptFactory(gridScriptFactory);
			runner.setManager(gridEngineManager);
			runner.setMessengerFactory(messengerFactory);
			runner.setSharedWorkingDirectory(new File(config.getSharedWorkingDirectory()).getAbsoluteFile());
			runner.setSharedTempDirectory(new File(config.getSharedTempDirectory()).getAbsoluteFile());
			runner.setOutputDirectory(new File(config.getSharedTempDirectory()).getAbsoluteFile());
			runner.setSharedLogDirectory(new File(config.getSharedLogDirectory()).getAbsoluteFile());
			runner.setWrapperScript(config.getWrapperScript());
			runner.setWorkerFactoryConfig(config.getWorkerConfiguration());
			runner.setFileTokenFactory(fileTokenFactory);

			return runner;
		}

		public GridEngineJobManager getGridEngineManager() {
			return gridEngineManager;
		}

		public void setGridEngineManager(GridEngineJobManager gridEngineManager) {
			this.gridEngineManager = gridEngineManager;
		}

		public GridScriptFactory getGridScriptFactory() {
			return gridScriptFactory;
		}

		public void setGridScriptFactory(GridScriptFactory gridScriptFactory) {
			this.gridScriptFactory = gridScriptFactory;
		}

		public MessengerFactory getMessengerFactory() {
			return messengerFactory;
		}

		public void setMessengerFactory(MessengerFactory messengerFactory) {
			this.messengerFactory = messengerFactory;
		}

		public FileTokenFactory getFileTokenFactory() {
			return fileTokenFactory;
		}

		public void setFileTokenFactory(FileTokenFactory fileTokenFactory) {
			this.fileTokenFactory = fileTokenFactory;
		}
	}
}
