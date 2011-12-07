package edu.mayo.mprc.config;

public abstract class FactoryBase<C extends ResourceConfig, R> implements ResourceFactory<C, R> {
	@Override
	public R createSingleton(C config, DependencyResolver dependencies) {
		R result = (R) dependencies.resolveDependencyQuietly(config);
		if (result != null) {
			return result;
		}
		R newResult = create(config, dependencies);
		dependencies.addDependency(config, newResult);
		return newResult;
	}
}
