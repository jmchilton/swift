package edu.mayo.mprc.daemon;

import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.FactoryBase;
import edu.mayo.mprc.config.ResourceConfig;

/**
 * A worker factory. Can create {@link Worker} instances.
 *
 * @param <C> The configuration the given worker needs.
 */
public abstract class WorkerFactoryBase<C extends ResourceConfig> extends FactoryBase<C, Worker> implements WorkerFactory {
	private C config;
	private DependencyResolver dependencies;
	private String description;

	protected WorkerFactoryBase() {
	}

	@Override
	public Worker createWorker() {
		return create(config, dependencies);
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public C getConfig() {
		return config;
	}

	public void setConfig(final C config) {
		this.config = config;
	}

	public DependencyResolver getDependencies() {
		return dependencies;
	}

	public void setDependencies(final DependencyResolver dependencies) {
		this.dependencies = dependencies;
	}
}
