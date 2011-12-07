package edu.mayo.mprc.daemon;

import com.google.common.base.Joiner;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.*;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A daemon - collection of multiple runners that provide services.
 */
public final class Daemon {
	private static final Logger LOGGER = Logger.getLogger(Daemon.class);

	private List<AbstractRunner> runners;
	private List<Object> resources;

	public Daemon(List<AbstractRunner> runners, List<Object> resources) {
		this.runners = runners;
		this.resources = resources;
	}

	/**
	 * Runs all the defined daemons runners.
	 */
	public void start() {
		for (AbstractRunner runner : runners) {
			try {
				runner.start();
			} catch (Exception t) {
				throw new MprcException("The runner " + runner.toString() + " failed to start.", t);
			}
		}
	}

	/**
	 * Stops the daemon runners. Does not block until the runners terminate.
	 */
	public void stop() {
		for (AbstractRunner runner : runners) {
			runner.stop();
		}
	}

	/**
	 * Wait until the runners all terminate.
	 */
	public void awaitTermination() {
		for (AbstractRunner runner : runners) {
			runner.awaitTermination();
		}
	}

	public int getNumRunners() {
		return runners.size();
	}

	public List<Object> getResources() {
		return resources;
	}

	@Override
	public String toString() {
		return "Daemon running following services: " +
				Joiner.on(",\n").join(runners);
	}

	/**
	 * Runs a daemon from its config.
	 */
	public static final class Factory {
		/* Factory for the particular daemon components */
		private MultiFactory factory;

		public Daemon createDaemon(DaemonConfig config) {

			DependencyResolver dependencies = new DependencyResolver(factory);

			// Create daemon resources
			List<Object> resources = new ArrayList<Object>(config.getResources().size());
			addResourcesToList(resources, config.getResources(), dependencies);

			// Create runners
			List<AbstractRunner> runners = new ArrayList<AbstractRunner>(config.getServices().size());
			addRunnersToList(runners, config.getServices(), dependencies);

			return new Daemon(runners, resources);
		}

		private void addResourcesToList(List<Object> resources, List<ResourceConfig> configs, DependencyResolver dependencies) {
			Collections.sort(configs, new ResourceComparator());
			for (ResourceConfig resourceConfig : configs) {
				final ResourceFactory resourceFactory = this.factory.getFactory(resourceConfig.getClass());
				final Object resource = resourceFactory.createSingleton(resourceConfig, dependencies);
				resources.add(resource);
			}
		}

		private void addRunnersToList(List<AbstractRunner> runners, List<ServiceConfig> services, DependencyResolver dependencies) {
			for (ServiceConfig serviceConfig : services) {
				if (serviceConfig == null) {
					LOGGER.error("Programmer error: service configuration was null - listing the configurations: ");
					for (ServiceConfig config : services) {
						LOGGER.error(config);
					}
					throw new MprcException("Programmer error: service configuration was null.");
				}
				DaemonConnection daemonConnection = (DaemonConnection) factory.createSingleton(serviceConfig, dependencies);
				RunnerConfig runnerConfig = serviceConfig.getRunner();
				AbstractRunner runner = (AbstractRunner) factory.createSingleton(runnerConfig, dependencies);
				runner.setDaemonConnection(daemonConnection);
				runners.add(runner);
			}
		}

		public MultiFactory getMultiFactory() {
			return factory;
		}

		public void setMultiFactory(MultiFactory factory) {
			this.factory = factory;
		}
	}

	private static final class ResourceComparator implements Comparator<ResourceConfig>, Serializable {
		private static final long serialVersionUID = 20101123L;

		public int compare(ResourceConfig o1, ResourceConfig o2) {
			return Integer.valueOf(o2.getPriority()).compareTo(o1.getPriority());
		}
	}
}
