package edu.mayo.mprc.swift.configuration.server;

import edu.mayo.mprc.GWTServiceExceptionFactory;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.common.client.GWTServiceException;
import edu.mayo.mprc.common.client.StringUtilities;
import edu.mayo.mprc.config.*;
import edu.mayo.mprc.config.ui.PropertyChangeListener;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.daemon.MessageBroker;
import edu.mayo.mprc.daemon.SimpleRunner;
import edu.mayo.mprc.database.DatabaseFactory;
import edu.mayo.mprc.sge.GridRunner;
import edu.mayo.mprc.swift.*;
import edu.mayo.mprc.swift.configuration.client.model.*;
import edu.mayo.mprc.swift.search.SwiftSearcher;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.exceptions.ExceptionUtilities;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Since we have a split between the server and client (GWT cannot access a lot of server-side code directly),
 * we have TWO models rooted in {@link edu.mayo.mprc.config.ApplicationConfig} (server) and {@link edu.mayo.mprc.swift.configuration.client.model.ApplicationModel}
 * (client). This class keeps them in sync. It should be stored in the server context (one per server).
 * <p>
 * All operations that alter the config should also alter the corresponding model (accessible using the {@link DependencyResolver}).
 * This includes operations like {@link #createChild(String, String)} or {@link #removeChild(String)}.
 * </p>
 */
public class ConfigurationData {
	private ApplicationConfig config;
	private DependencyResolver resolver;
	private HashMap<ConcreteProperty, PropertyChangeListener> listeners = new HashMap<ConcreteProperty, PropertyChangeListener>(100);
	private final ResourceTable resourceTable = MainFactoryContext.getResourceTable();

	private ResourceTable getResourceTable() {
		return resourceTable;
	}

	public ConfigurationData() {
		config = new ApplicationConfig();
		initResolver();
	}

	public ApplicationConfig getConfig() {
		return config;
	}

	public void setConfig(final ApplicationConfig config) {
		this.config = config;
		initResolver();
		mapConfigToModel();
	}

	public ApplicationModel getModel() {
		final Object obj = resolver.getDependencyFromId(getId(config));
		if (obj instanceof ApplicationModel) {
			return (ApplicationModel) obj;
		} else {
			ExceptionUtilities.throwCastException(obj, ApplicationModel.class);
			return null;
		}
	}

	public String getId(final ResourceConfig config) {
		return resolver.getIdFromConfig(config);
	}

	public ResourceConfig getResourceConfig(final String id) {
		return resolver.getConfigFromId(id);
	}

	public ResourceModel createChild(final String parentId, final String type) throws GWTServiceException {
		final String index = getFreeIndex(type);
		final ResourceConfig parent = getResourceConfig(parentId);
		if (parent instanceof DaemonConfig) {
			return createResource(index, type, (DaemonConfig) parent);
		} else if (parent instanceof ApplicationConfig) {
			return createDaemon(MessageFormat.format("Daemon {0}", index), false);
		}
		return null;
	}

	/**
	 * For given type (e.g. "mascot") finds all objects with names "mascot1", "mascot2"... etc.
	 * Returns lowest number referring to a name that does not exist yet (e.g. "3") in this case.
	 */
	private String getFreeIndex(final String type) {
		int count = 1;
		while (true) {
			final String countedName = type + count;
			if (!nameExists(countedName)) {
				return "" + count;
			}
			count++;
		}
	}

	private boolean nameExists(final String name) {
		for (final DaemonConfig daemonConfig : getConfig().getDaemons()) {
			if (daemonConfig.getName().equals(name)) {
				return true;
			}
			for (final ServiceConfig serviceConfig : daemonConfig.getServices()) {
				if (serviceConfig.getName().equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Create new daemon and add it to the parent.
	 *
	 * @param local If the daemon is to run on the local machine.
	 */
	private DaemonModel createDaemon(final String name, final boolean local) {
		final DaemonConfig daemon = DaemonConfig.getDefaultDaemonConfig(name, local);
		getConfig().addDaemon(daemon);
		return mapDaemonConfigToModel(daemon);
	}

	/**
	 * Create new resource and add it to the given parent.
	 */
	private ResourceModel createResource(final String index, final String type, final DaemonConfig parent) throws GWTServiceException {
		try {
			final ResourceTable moduleConfigTable = getResourceTable();

			final ResourceConfig resourceConfig = getDefaultResourceConfig(type, parent, moduleConfigTable);

			final ResourceTable.ResourceType resourceType = moduleConfigTable.getResourceTypeAsType(type);
			if (resourceType == ResourceTable.ResourceType.Worker) {
				// A service needs a runner
				final ServiceConfig serviceConfig = new ServiceConfig();

				serviceConfig.setRunner(new SimpleRunner.Config(resourceConfig));
				serviceConfig.setName(type + '_' + index); // The name is type_index
				serviceConfig.setBrokerUrl(getServiceBrokerUri(getMessageBrokerUrl(), serviceConfig.getName()));

				parent.addService(serviceConfig);
				final ModuleModel moduleModel = mapServiceConfigToModel(index, parent, serviceConfig);
				return moduleModel;
			} else if (resourceType == ResourceTable.ResourceType.Resource) {
				// A resource is more simple
				parent.addResource(resourceConfig);
				final ResourceModel resourceModel = mapResourceConfigToModel(parent, resourceConfig);
				return resourceModel;
			} else {
				throw GWTServiceExceptionFactory.createException("Cannot create child of type " + resourceType, null);
			}
		} catch (Exception e) {
			throw GWTServiceExceptionFactory.createException("Cannot create configuration element of type " + type + " as a child of " + parent.getName(), e);
		}
	}

	/**
	 * Create resourceConfig for given type. Fill it with default values, as specified in the UI builder.
	 *
	 * @param type
	 * @param parent
	 * @param moduleConfigTable
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private ResourceConfig getDefaultResourceConfig(final String type, final DaemonConfig parent, final ResourceTable moduleConfigTable) {
		final Class<? extends ResourceConfig> configClass = moduleConfigTable.getConfigClassForType(type);
		final ResourceConfig resourceConfig;
		try {
			resourceConfig = configClass.newInstance();
		} catch (InstantiationException e) {
			throw new MprcException("Could not create resource configuration for " + type, e);
		} catch (IllegalAccessException e) {
			throw new MprcException("Access denied when creating resource configuration for " + type, e);
		}

		// Now that we have a config, we need to fill it with default values
		final ServiceUiFactory uiFactory = moduleConfigTable.getUiFactory(type);
		if (uiFactory != null) {
			final Map<String, String> initialValues = resourceConfig.save(resolver);
			final DefaultSettingUiBuilder builder = new DefaultSettingUiBuilder(initialValues, resolver);
			uiFactory.createUI(parent, resourceConfig, builder);
			resourceConfig.load(builder.getValues(), resolver);
		}
		return resourceConfig;
	}

	private String getMessageBrokerUrl() {
		final List<ResourceConfig> brokerModules = config.getModulesOfConfigType(MessageBroker.Config.class);
		if (brokerModules.size() > 0 && (brokerModules.get(0) instanceof MessageBroker.Config)) {
			final MessageBroker.Config broker = (MessageBroker.Config) brokerModules.get(0);
			return broker.effectiveBrokerUrl();
		}
		return "";
	}

	public void removeChild(final String childId) throws GWTServiceException {
		final ResourceConfig resourceConfig = getResourceConfig(childId);
		if (resourceConfig == null) {
			throw new GWTServiceException("Could not find element: " + childId, null);
		}
		getConfig().remove(resourceConfig);
	}

	/**
	 * Fill in the resolver, making sure we know that a particular config matches the particular model.
	 *
	 * @param config The configuration.
	 * @param model  The model of the configuration being sent to the client.
	 */
	public void bindConfigToModel(final ResourceConfig config, final ResourceModel model) {
		resolver.addDependency(config, model);
		model.setId(resolver.getIdFromConfig(config));
	}

	private void initResolver() {
		resolver = new DependencyResolver(getResourceTable());
	}

	private ApplicationModel mapConfigToModel() {
		final ApplicationModel application = new ApplicationModel();
		bindConfigToModel(getConfig(), application);

		final AvailableModules availableModules = makeAvailableModules();
		application.setAvailableModules(availableModules);

		// For each ResourceConfig in Config, make sure the resolver has an id defined
		for (final DaemonConfig daemon : getConfig().getDaemons()) {
			final DaemonModel daemonModel = mapDaemonConfigToModel(daemon);
			application.addDaemon(daemonModel);
		}

		// Now we can actually transfer the properties, since the resolver has been fully initialized
		for (final DaemonModel daemon : application.getDaemons()) {
			for (final ResourceModel resource : daemon.getChildren()) {
				loadModelData(resource);
			}
		}

		return application;
	}

	private AvailableModules makeAvailableModules() {
		final AvailableModules availableModules = new AvailableModules();
		final ResourceTable resourceTable = getResourceTable();
		for (final String type : resourceTable.getAllTypes()) {
			availableModules.add(resourceTable.getUserName(type), type, resourceTable.getDescription(type), resourceTable.getResourceTypeAsType(type) == ResourceTable.ResourceType.Worker);
		}
		return availableModules;
	}

	private DaemonModel mapDaemonConfigToModel(final DaemonConfig daemon) {
		final DaemonModel daemonModel = new DaemonModel();
		bindConfigToModel(daemon, daemonModel);

		daemonModel.setName(daemon.getName());
		daemonModel.setHostName(daemon.getHostName());
		daemonModel.setOsArch(daemon.getOsArch());
		daemonModel.setOsName(daemon.getOsName());
		daemonModel.setSharedFileSpacePath(daemon.getSharedFileSpacePath());
		daemonModel.setTempFolderPath(daemon.getTempFolderPath());
		for (final ServiceConfig service : daemon.getServices()) {
			final ResourceTable table = getResourceTable();
			final String type = table.getId(service.getRunner().getWorkerConfiguration().getClass());
			final String moduleIndex = service.getName().substring(type.length() + "_".length());
			final ModuleModel module = mapServiceConfigToModel(moduleIndex, daemon, service);
			daemonModel.addChild(module);
		}
		for (final ResourceConfig resourceConfig : daemon.getResources()) {
			final ResourceModel resource = mapResourceConfigToModel(daemon, resourceConfig);
			daemonModel.addChild(resource);
		}
		return daemonModel;
	}

	private ResourceModel mapResourceConfigToModel(final DaemonConfig daemon, final ResourceConfig resourceConfig) {
		final ResourceTable table = getResourceTable();

		final String resourceType = table.getId(resourceConfig.getClass());
		final String resourceName = table.getUserName(resourceType);

		final ResourceModel resource = new ResourceModel(resourceName, resourceType);
		setResourceUi(daemon, resourceConfig, resourceType, resource);

		bindConfigToModel(resourceConfig, resource);
		return resource;
	}

	private ModuleModel mapServiceConfigToModel(final String index, final DaemonConfig daemon, final ServiceConfig service) {
		final ResourceModel serviceModel = new ResourceModel(service.getName(), ResourceTable.SERVICE);

		resolver.addDependency(service, serviceModel);
		serviceModel.setId(resolver.getIdFromConfig(service));

		final RunnerConfig runner = service.getRunner();
		final ResourceModel runnerModel = mapRunnerConfigToModel(runner);
		bindConfigToModel(runner, runnerModel);

		final ResourceConfig moduleConfig = runner.getWorkerConfiguration();
		final ResourceTable table = getResourceTable();
		final String moduleType = table.getId(moduleConfig.getClass());
		final String moduleName = table.getUserName(moduleType) + ("1".equals(index) ? "" : " " + index);

		final ModuleModel module = new ModuleModel(moduleName, moduleType, serviceModel, runnerModel);
		setResourceUi(daemon, moduleConfig, moduleType, module);
		bindConfigToModel(moduleConfig, module);

		return module;
	}

	private void setResourceUi(final DaemonConfig daemon, final ResourceConfig config, final String type, final ResourceModel resource) {
		// Save the config properties, store them into the model
		resource.setProperties(new HashMap<String, String>(config.save(resolver)));

		final ServiceUiFactory factory = getResourceTable().getUiFactory(type);
		final SerializingUiBuilder builder = new SerializingUiBuilder(new ListenerMapBuilder() {
			@Override
			public void setListener(final String propertyName, final PropertyChangeListener listener) {
				listeners.put(new ConcreteProperty(config, propertyName), listener);
			}

			@Override
			public void setDaemonListener(final PropertyChangeListener listener) {
				listeners.put(new ConcreteProperty(daemon, null), listener);
			}
		}, resolver);
		factory.createUI(daemon, config, builder);
		resource.setReplayer(builder.getReplayer());
	}

	private void loadModelData(final ResourceModel resource) {
		final String id = resolver.getIdFromDependency(resource);
		final ResourceConfig resourceConfig = resolver.getConfigFromId(id);
		resource.setProperties(new HashMap<String, String>(resourceConfig.save(resolver)));
	}

	private ResourceModel mapRunnerConfigToModel(final RunnerConfig runner) {
		final ResourceModel runnerModel;
		if (runner instanceof SimpleRunner.Config) {
			runnerModel = new ResourceModel("localRunner", "localRunner");
		} else if (runner instanceof GridRunner.Config) {
			runnerModel = new ResourceModel("sgeRunner", "sgeRunner");
		} else {
			throw new MprcException("Not supported runner config: " + runner.getClass().getName());
		}

		runnerModel.setProperties(new HashMap<String, String>(runner.save(null)));
		return runnerModel;
	}


	/**
	 * Produce a default daemon model to present to the user if configuration is missing.
	 * Since we are calling the service methods that need a counterpart running on the UI side,
	 * we are responsible for properly adding the daemon and resources as children.
	 *
	 * @return Default swift model.
	 */
	public void loadDefaultConfig() throws GWTServiceException {
		final ApplicationConfig config = new ApplicationConfig();
		setConfig(config);

		final DaemonModel daemonModel = createDaemon("main", true);
		getModel().addDaemon(daemonModel);

		final DaemonConfig daemon = (DaemonConfig) getResourceConfig(daemonModel.getId());

		final ResourceModel database = createResource("1", DatabaseFactory.TYPE, daemon);
		daemonModel.addChild(database);

		final ResourceModel messageBroker = createResource("1", MessageBroker.TYPE, daemon);
		daemonModel.addChild(messageBroker);

		final ResourceModel swiftSearcher = createResource("1", SwiftSearcher.TYPE, daemon);
		daemonModel.addChild(swiftSearcher);

		final ResourceModel webUi = createResource("1", WebUi.TYPE, daemon);
		daemonModel.addChild(webUi);
	}

	/**
	 * Save config to the disk. Also create all the daemon runner scripts.
	 *
	 * @param parentFolder Where to put the generated scripts and config.
	 * @return List of UI changes (validations triggered by save).
	 */
	public UiChangesReplayer saveConfig(final File parentFolder) {
		final File configFile = new File(parentFolder, Swift.CONFIG_FILE_NAME).getAbsoluteFile();
		if (configFile.getParent() != null) {
			FileUtilities.ensureFolderExists(configFile.getParentFile());
		}

		final SerializingUiChanges uiChanges = new SerializingUiChanges(resolver);

		final ApplicationConfig applicationConfig = getConfig();
		final List<String> errorList = SwiftConfig.validateSwiftConfig(applicationConfig);

		// Clear errors
		uiChanges.displayPropertyError(applicationConfig, null, null);
		for (final String error : errorList) {
			uiChanges.displayPropertyError(applicationConfig, null, error);
		}
		applicationConfig.save(configFile, getResourceTable());
		for (final DaemonConfig daemon : applicationConfig.getDaemons()) {
			final String scriptName = daemon.getName().replaceAll("[^a-zA-Z0-9-_+]", "_") + "-run";
			boolean hasWeb = false;
			for (final ResourceConfig resource : daemon.getResources()) {
				if (resource instanceof WebUi.Config) {
					hasWeb = true;
					break;
				}
			}

			boolean linux = true;
			boolean windows = true;

			if (StringUtilities.toLowerCase(daemon.getOsName()).contains("windows")) {
				linux = false;
			}
			if (StringUtilities.toLowerCase(daemon.getOsName()).contains("linux")) {
				windows = false;
			}

			if (linux) {
				final File linuxScript = new File(parentFolder, scriptName + ".sh");
				FileUtilities.writeStringToFile(linuxScript, ""
						+ "while true\n"
						+ "do\n"
						+ "echo ==== Starting daemon " + daemon.getName() + " ====\n"
						+ "./swift" + (hasWeb ? "Web" : "") + ".sh --daemon='" + daemon.getName() + "' $*\n"
						+ "if [ $? -ne 2 ]; then break; fi\n"
						+ "done\n"
						, true);
			}

			if (windows) {
				final File dosScript = new File(parentFolder, scriptName + ".bat");
				FileUtilities.writeStringToFile(dosScript, ""
						+ "@echo off\r\n"
						+ ":RUN_DAEMON\r\n"
						+ "echo ====  Starting daemon " + daemon.getName() + " ====\r\n"
						+ "call swift" + (hasWeb ? "Web" : "") + ".bat --daemon=\"" + daemon.getName() + "\"\r\n"
						+ "IF ERRORLEVEL 2 GOTO RUN_DAEMON\r\n"
						, true);
			}
		}
		return uiChanges.getReplayer();
	}

	private String getServiceBrokerUri(final String brokerUrl, final String serviceName) {
		return "jms." + brokerUrl + (brokerUrl.indexOf('?') == -1 ? "?" : "&") + "simplequeue=" + serviceName;
	}

	public UiChangesReplayer setProperty(final ResourceConfig resourceConfig, final String propertyName, final String newValue, final boolean onDemand) {
		// Set the property on the config
		final Map<String, String> data = resourceConfig.save(resolver);
		data.put(propertyName, newValue);
		resourceConfig.load(data, resolver);

		// Set the property on the corresponding model
		final ResourceModel model = (ResourceModel) resolver.getDependencyFromId(resolver.getIdFromConfig(resourceConfig));
		model.setProperty(propertyName, newValue);

		// Fire the property change event, serialize all UI changes that are needed as a response to property changing
		final SerializingUiChanges changes = new SerializingUiChanges(resolver);

		final PropertyChangeListener listener = getPropertyListener(resourceConfig, propertyName);
		if (listener != null) {
			listener.propertyChanged(resourceConfig, propertyName, newValue, changes, onDemand);
		}

		return changes.getReplayer();
	}

	/**
	 * A property listener is either specific to the property name, or, if no such listener exists,
	 * specific to the entire config with property name set to <c>null</c>.
	 *
	 * @param resourceConfig Config to find property listener for.
	 * @param propertyName   Name of the property. <c>null</c> means "any property"
	 * @return The change listener associated with the property.
	 */
	private PropertyChangeListener getPropertyListener(final ResourceConfig resourceConfig, final String propertyName) {
		final PropertyChangeListener propertyChangeListener = listeners.get(new ConcreteProperty(resourceConfig, propertyName));
		if (propertyChangeListener == null) {
			return listeners.get(new ConcreteProperty(resourceConfig, null));
		}
		return propertyChangeListener;
	}

	public void fix(final ResourceConfig resourceConfig, final String propertyName, final String action) {
		final PropertyChangeListener listener = getPropertyListener(resourceConfig, propertyName);
		if (listener != null) {
			listener.fixError(resourceConfig, propertyName, action);
		}
	}

	public void changeRunner(final ServiceConfig serviceConfig, final RunnerConfig newRunner) {
		// Update mapping for runner of this service
		resolver.changeConfigType(serviceConfig.getRunner(), newRunner);
		serviceConfig.setRunner(newRunner);
	}
}


