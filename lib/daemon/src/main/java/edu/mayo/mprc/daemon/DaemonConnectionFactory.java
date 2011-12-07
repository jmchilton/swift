package edu.mayo.mprc.daemon;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.FactoryBase;
import edu.mayo.mprc.config.ServiceConfig;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.messaging.Service;
import edu.mayo.mprc.messaging.ServiceFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Knows all about communication between daemons.
 * Knows which daemon it is a part of.
 * Capable of creating either the receiving or the sending end for a service of a given id.
 */
public final class DaemonConnectionFactory extends FactoryBase<ServiceConfig, DaemonConnection> {
	private FileTokenFactory fileTokenFactory;
	private ServiceFactory serviceFactory;

	public FileTokenFactory getFileTokenFactory() {
		return fileTokenFactory;
	}

	public void setFileTokenFactory(FileTokenFactory fileTokenFactory) {
		this.fileTokenFactory = fileTokenFactory;
	}

	public ServiceFactory getServiceFactory() {
		return serviceFactory;
	}

	public void setServiceFactory(ServiceFactory serviceFactory) {
		this.serviceFactory = serviceFactory;
	}

	public DaemonConnection create(ServiceConfig config, DependencyResolver dependencies) {
		try {
			final String brokerUrl = config.getBrokerUrl();
			final URI serviceUri = new URI(brokerUrl);
			final ServiceFactory factory = getServiceFactory();
			final Service service = factory.createService(serviceUri);
			return new DirectDaemonConnection(service, fileTokenFactory);
		} catch (URISyntaxException e) {
			throw new MprcException("Wrong service uri ", e);
		}
	}
}
