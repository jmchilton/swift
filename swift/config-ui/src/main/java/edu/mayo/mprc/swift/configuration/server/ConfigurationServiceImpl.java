package edu.mayo.mprc.swift.configuration.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import edu.mayo.mprc.GWTServiceExceptionFactory;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.common.client.GWTServiceException;
import edu.mayo.mprc.config.ApplicationConfig;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.RunnerConfig;
import edu.mayo.mprc.config.ServiceConfig;
import edu.mayo.mprc.daemon.SimpleRunner;
import edu.mayo.mprc.sge.GridRunner;
import edu.mayo.mprc.swift.MainFactoryContext;
import edu.mayo.mprc.swift.Swift;
import edu.mayo.mprc.swift.configuration.client.ConfigurationService;
import edu.mayo.mprc.swift.configuration.client.model.ApplicationModel;
import edu.mayo.mprc.swift.configuration.client.model.ResourceModel;
import edu.mayo.mprc.swift.configuration.client.model.UiChangesReplayer;
import edu.mayo.mprc.utilities.exceptions.ExceptionUtilities;

import java.io.File;

/**
 * Does all the server-side work needed for configuration process to function.
 * <p/>
 * Note: The implementation must never throw a runtime exception. Runtime exceptions are not transfered properly by GWT.
 */
public final class ConfigurationServiceImpl extends RemoteServiceServlet implements ConfigurationService {
	private static final long serialVersionUID = 20101221L;

	/**
	 * We need to take the client-side model of the configuration and convert it to {@link edu.mayo.mprc.config.ApplicationConfig}.
	 * <p/>
	 * As the configuration gets saved, we produce startup scripts as well.
	 */
	public UiChangesReplayer saveConfiguration() throws GWTServiceException {
		try {
			return getData().saveConfig(null);
		} catch (Exception t) {
			throw GWTServiceExceptionFactory.createException(MprcException.getDetailedMessage(t), t);
		}
	}

	public ApplicationModel loadConfiguration() throws GWTServiceException {
		MainFactoryContext.initialize();
		File configFile = new File(Swift.CONFIG_FILE_NAME).getAbsoluteFile();
		try {
			if (configFile.exists()) {
				getData().setConfig(ApplicationConfig.load(configFile, MainFactoryContext.getResourceTable()));
			} else {
				getData().loadDefaultConfig();
			}
			return getData().getModel();
		} catch (Exception t) {
			throw GWTServiceExceptionFactory.createException("Cannot load configuration from " + configFile.getPath(), t);
		}
	}

	@Override
	public ResourceModel createChild(String parentId, String type) throws GWTServiceException {
		return getData().createChild(parentId, type);
	}

	@Override
	public void removeChild(String childId) throws GWTServiceException {
		getData().removeChild(childId);
	}

	@Override
	public void changeRunner(String serviceId, String newRunnerType) throws GWTServiceException {
		final ServiceConfig serviceConfig = (ServiceConfig) getData().getResourceConfig(serviceId);
		RunnerConfig runner=null;
		if ("localRunner".equals(newRunnerType)) {
			runner = new SimpleRunner.Config(serviceConfig.getRunner().getWorkerConfiguration());
		} else {
			runner = new GridRunner.Config(serviceConfig.getRunner().getWorkerConfiguration());
		}
		getData().changeRunner(serviceConfig, runner);
	}

	@Override
	public UiChangesReplayer propertyChanged(String modelId, String propertyName, String newValue, boolean onDemand) throws GWTServiceException {
		final ResourceConfig resourceConfig = getData().getResourceConfig(modelId);
		return getData().setProperty(resourceConfig, propertyName, newValue, onDemand);
	}

	@Override
	public void fix(String moduleId, String propertyName, String action) throws GWTServiceException {
		final ResourceConfig resourceConfig = getData().getResourceConfig(moduleId);
		getData().fix(resourceConfig, propertyName, action);
	}

	private ConfigurationData getData() {
		final Object obj = getServletContext().getAttribute("configurationData");
		if (obj == null) {
			final ConfigurationData configurationData = new ConfigurationData();
			getServletContext().setAttribute("configurationData", configurationData);
			return configurationData;
		}
		if (obj instanceof ConfigurationData) {
			return (ConfigurationData) obj;
		} else {
			ExceptionUtilities.throwCastException(obj, ConfigurationData.class);
			return null;
		}
	}

}