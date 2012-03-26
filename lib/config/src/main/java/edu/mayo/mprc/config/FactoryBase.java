package edu.mayo.mprc.config;

public abstract class FactoryBase<C extends ResourceConfig, R> implements ResourceFactory<C, R> {
	@Override
	public R createSingleton(final C config, final DependencyResolver dependencies) {
		final R result = (R) dependencies.resolveDependencyQuietly(config);
		if (result != null) {
			return result;
		}
		final R newResult = create(config, dependencies);
		dependencies.addDependency(config, newResult);
		return newResult;
	}
}
