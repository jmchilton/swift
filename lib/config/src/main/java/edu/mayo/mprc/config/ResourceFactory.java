package edu.mayo.mprc.config;

public interface ResourceFactory<C extends ResourceConfig, R> {
	/**
	 * Creates a new instance. The instance is not added to the cache of already created objects.
	 */
	R create(C config, DependencyResolver dependencies);

	/**
	 * Looks into the cache of dependencies. If our object is already in there,
	 * it is returned. Otherwise {@link #create} is called and the result is added to the cache.
	 */
	R createSingleton(C config, DependencyResolver dependencies);
}
