package edu.mayo.mprc.swift;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.ReleaseInfoCore;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.daemon.DaemonProgress;
import edu.mayo.mprc.daemon.DaemonProgressMessage;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.files.FileTokenHolder;
import edu.mayo.mprc.filesharing.jms.JmsFileTransferHandlerFactory;
import edu.mayo.mprc.messaging.rmi.BoundMessenger;
import edu.mayo.mprc.messaging.rmi.MessengerFactory;
import edu.mayo.mprc.messaging.rmi.OneWayMessenger;
import edu.mayo.mprc.messaging.rmi.RemoteObjectHandler;
import edu.mayo.mprc.sge.GridDaemonWorkerAllocatorInputObject;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.progress.ProgressInfo;
import edu.mayo.mprc.utilities.progress.ProgressReporter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

public final class SgeJobRunner {

	private static final Logger LOGGER = Logger.getLogger(SgeJobRunner.class);
	private ResourceTable resourceTable;

	// Two minutes should be more than enough for the file written on one machine to appear on another machine
	private static final int INPUT_FILE_TIMEOUT = 2 * 60 * 1000;

	public SgeJobRunner() {
	}

	/**
	 * Takes a name with work packet serialized into xml. Executes the work packet and communicates the results.
	 *
	 * @param workPacketXmlFile File containing the serialized work packet.
	 */
	public void run(File workPacketXmlFile) {
		// Wait for the work packet to fully materialize in case it was transferred over a shared filesystem
		FileUtilities.waitForFile(workPacketXmlFile, INPUT_FILE_TIMEOUT);

		FileInputStream fileInputStream = null;
		GridDaemonWorkerAllocatorInputObject daemonWorkerAllocatorInputObject = null;
		BoundMessenger<OneWayMessenger> boundMessenger = null;
		RemoteObjectHandler handler = new RemoteObjectHandler();
		MessengerFactory messengerFactory = new MessengerFactory(handler);

		try {
			LOGGER.info("Running grid job in host: " + InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			LOGGER.error("Could not get host name.", e);
		}

		try {
			LOGGER.debug(ReleaseInfoCore.infoString());
			LOGGER.info("Parsing xml file: " + workPacketXmlFile.getAbsolutePath());

			XStream xStream = new XStream(new DomDriver());

			fileInputStream = new FileInputStream(workPacketXmlFile);

			daemonWorkerAllocatorInputObject = (GridDaemonWorkerAllocatorInputObject) xStream.fromXML(fileInputStream);

			//If the work packet is an instance of a FileTokenHolder, set the the FileTokenFactory on it. The FileTokenFactory object
			//needs to be reset because it is a transient object.
			if (daemonWorkerAllocatorInputObject.getWorkPacket() instanceof FileTokenHolder) {
				FileTokenHolder fileTokenHolder = (FileTokenHolder) daemonWorkerAllocatorInputObject.getWorkPacket();
				FileTokenFactory fileTokenFactory = new FileTokenFactory(daemonWorkerAllocatorInputObject.getDaemonConfigInfo());

				if (daemonWorkerAllocatorInputObject.getSharedTempDirectory() != null) {
					fileTokenFactory.setTempFolderRepository(new File(daemonWorkerAllocatorInputObject.getSharedTempDirectory()));
				}

				fileTokenFactory.setFileSharingFactory(new JmsFileTransferHandlerFactory(daemonWorkerAllocatorInputObject.getFileSharingFactoryURI()), false);
				fileTokenHolder.translateOnReceiver(fileTokenFactory, fileTokenFactory);
			}

			boundMessenger = messengerFactory.getOneWayMessenger(daemonWorkerAllocatorInputObject.getMessengerInfo());

			DependencyResolver dependencies = new DependencyResolver(resourceTable);
			Worker daemonWorker = (Worker) resourceTable.createSingleton(daemonWorkerAllocatorInputObject.getWorkerFactoryConfig(), dependencies);
			daemonWorker.processRequest((WorkPacket) daemonWorkerAllocatorInputObject.getWorkPacket(), new DaemonWorkerProgressReporter(boundMessenger));
		} catch (Exception e) {
			String errorMessage = "Failed to process work packet " + ((daemonWorkerAllocatorInputObject == null || daemonWorkerAllocatorInputObject.getWorkPacket() == null) ? "null" : daemonWorkerAllocatorInputObject.getWorkPacket().toString());
			LOGGER.error(errorMessage, e);

			try {
				reportProgress(boundMessenger, e);
			} catch (RemoteException ex) {
				LOGGER.error("Error sending exception " + MprcException.getDetailedMessage(e) + " to GridRunner", ex);
				// SWALLOWED
			}

			System.exit(1);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					LOGGER.warn("Error closing file input stream.", e);
					// SWALLOWED
				}
			}
		}

		LOGGER.info("Work packet " + daemonWorkerAllocatorInputObject.getWorkPacket().toString() + " successfully processed.");
		System.exit(0);
	}

	public ResourceTable getResourceTable() {
		return resourceTable;
	}

	public void setResourceTable(ResourceTable resourceTable) {
		this.resourceTable = resourceTable;
	}

	private static void reportProgress(BoundMessenger<OneWayMessenger> boundMessenger, Serializable serializable) throws RemoteException {
		boundMessenger.getMessenger().sendMessage(serializable);
	}

	static class DaemonWorkerProgressReporter implements ProgressReporter {
		private BoundMessenger<OneWayMessenger> boundMessenger;

		DaemonWorkerProgressReporter(BoundMessenger<OneWayMessenger> boundMessenger) {
			this.boundMessenger = boundMessenger;
		}

		@Override
		public void reportStart() {
			try {
				SgeJobRunner.reportProgress(boundMessenger, new DaemonProgressMessage(DaemonProgress.RequestProcessingStarted));
			} catch (Exception t) {
				try {
					SgeJobRunner.reportProgress(boundMessenger, t);
				} catch (RemoteException ex) {
					LOGGER.error("Error sending exception " + MprcException.getDetailedMessage(t) + " to GridRunner", ex);
					// SWALLOWED
				}
				System.exit(1);
			}
		}

		public void reportProgress(ProgressInfo progressInfo) {
			try {
				SgeJobRunner.reportProgress(boundMessenger, new DaemonProgressMessage(DaemonProgress.UserSpecificProgressInfo, progressInfo));
			} catch (RemoteException e) {
				LOGGER.error("Error reporting daemon worker progress.", e);
				//SWALLOWED
			}
		}

		public void reportSuccess() {
			//Do nothing. GridRunner gets notified of completion by SGE.
		}

		public void reportFailure(Throwable t) {
			try {
				SgeJobRunner.reportProgress(boundMessenger, t);
			} catch (RemoteException e) {
				LOGGER.error("Error reporting daemon worker failure.", e);
				//SWALLOWED
			}
		}
	}
}
