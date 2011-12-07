package edu.mayo.mprc.sge;

import edu.mayo.mprc.config.DaemonConfigInfo;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.messaging.rmi.MessengerInfo;

import java.net.URI;

/**
 * Class use to send work packet to grid engine. This object is process by the DaemonWorkerAllocator.
 */
public final class GridDaemonWorkerAllocatorInputObject {

	private Object workPacket;
	private MessengerInfo messengerInfo;
	private ResourceConfig workerFactoryConfig;
	private DaemonConfigInfo daemonConfigInfo;
	private URI fileSharingFactoryURI;
	private String sharedTempDirectory;

	public GridDaemonWorkerAllocatorInputObject() {
	}

	public GridDaemonWorkerAllocatorInputObject(Object workPacket, MessengerInfo messengerInfo, ResourceConfig workerFactoryConfig, DaemonConfigInfo daemonConfigInfo, URI fileSharingFactoryURI) {
		this.workPacket = workPacket;
		this.messengerInfo = messengerInfo;
		this.workerFactoryConfig = workerFactoryConfig;
		this.daemonConfigInfo = daemonConfigInfo;
		this.fileSharingFactoryURI = fileSharingFactoryURI;
	}

	public Object getWorkPacket() {
		return workPacket;
	}

	public void setWorkPacket(Object workPacket) {
		this.workPacket = workPacket;
	}

	public ResourceConfig getWorkerFactoryConfig() {
		return workerFactoryConfig;
	}

	public void setWorkerFactoryConfig(ResourceConfig workerFactoryConfig) {
		this.workerFactoryConfig = workerFactoryConfig;
	}

	public MessengerInfo getMessengerInfo() {
		return messengerInfo;
	}

	public void setMessengerInfo(MessengerInfo messengerInfo) {
		this.messengerInfo = messengerInfo;
	}

	public DaemonConfigInfo getDaemonConfigInfo() {
		return daemonConfigInfo;
	}

	public void setDaemonConfigInfo(DaemonConfigInfo daemonConfigInfo) {
		this.daemonConfigInfo = daemonConfigInfo;
	}

	public URI getFileSharingFactoryURI() {
		return fileSharingFactoryURI;
	}

	public void setFileSharingFactoryURI(URI fileSharingFactoryURI) {
		this.fileSharingFactoryURI = fileSharingFactoryURI;
	}

	public String getSharedTempDirectory() {
		return sharedTempDirectory;
	}

	public void setSharedTempDirectory(String sharedTempDirectory) {
		this.sharedTempDirectory = sharedTempDirectory;
	}
}
