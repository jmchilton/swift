package edu.mayo.mprc.swift.configuration.client.model;

import java.util.ArrayList;

/**
 * A model of an application configuration.
 * <ul>
 * <li>application consists of multiple daemons - different physical JVMs</li>
 * <li>daemon runs several modules (each module provides some functionality)</li>
 * <li>module consists of a runner (information about how to run tasks) and a service (does the actual work, runs within the runner)</li>
 * <li>service is configured using a bunch of properties</li>
 * </ul>
 * This model roughly corresponds to {@link edu.mayo.mprc.config.ApplicationConfig}. The main difference is that
 * the config has <pre>service -> runner -> worker</pre> hierarchy, while the {@link @ApplicationModel} uses
 * <pre>module -> service + runner</pre>, where the latter service is a mix of both {@link edu.mayo.mprc.config.ServiceConfig} and {@link edu.mayo.mprc.config.ResourceConfig} for the worker.
 * <p>
 * To map from {@link edu.mayo.mprc.config.ApplicationConfig} to {@link ApplicationModel} use {@link edu.mayo.mprc.swift.configuration.server.ConfigurationData}.
 * </p>
 */
public final class ApplicationModel extends ResourceModel {
	private static final long serialVersionUID = -581997481411688812L;
	private AvailableModules availableModules;

	public ApplicationModel() {
		super("Application", "application");
	}

	public AvailableModules getAvailableModules() {
		return availableModules;
	}

	public void setAvailableModules(final AvailableModules availableModules) {
		this.availableModules = availableModules;
	}

	public void addDaemon(final DaemonModel daemonModel) {
		addChild(daemonModel);
	}

	public void removeDaemon(final DaemonModel daemonModel) {
		removeChild(daemonModel);
	}

	public ArrayList<DaemonModel> getDaemons() {
		return (ArrayList<DaemonModel>) (ArrayList) getChildren();
	}

	/**
	 * Returns a service/resource of given id (go through all daemons, look at all their children)
	 */
	public ResourceModel getResourceModelForId(final String id) {
		for (final ResourceModel daemonModel : getChildren()) {
			for (final ResourceModel resourceModel : daemonModel.getChildren()) {
				if (resourceModel.getId().equals(id)) {
					return resourceModel;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a service/resource of given type (go through all daemons, look at all their children)
	 */
	public ArrayList<ResourceModel> getResourceModelsForType(final String type) {
		final ArrayList<ResourceModel> resources = new ArrayList<ResourceModel>();
		for (final ResourceModel daemonModel : getChildren()) {
			for (final ResourceModel resourceModel : daemonModel.getChildren()) {
				if (resourceModel.getType().equals(type)) {
					resources.add(resourceModel);
				}
			}
		}
		return resources;
	}
}
