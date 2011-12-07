package edu.mayo.mprc.config;

import edu.mayo.mprc.MprcException;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds currently instantiated triples (id, config, object). When given one of these, returns the other two.
 */
public final class DependencyResolver {
	private ResourceFactory<ResourceConfig, Object> factory;
	private final Map<ResourceConfig, IdObjectPair> dependencies = new HashMap<ResourceConfig, IdObjectPair>();
	private int id = 1;

	public DependencyResolver(ResourceFactory<ResourceConfig, Object> factory) {
		this.factory = factory;
	}

	/**
	 * Creates a new object from given config. Keeps track of created objects, so next time
	 * same config is provided, identical object is returned.
	 */
	public Object createSingleton(ResourceConfig config) {
		Object o = resolveDependencyQuietly(config);
		if (o != null) {
			return o;
		}
		return factory.createSingleton(config, this);
	}

	/**
	 * @param config Dependency configuration.
	 * @return Null if the dependency does not exist.
	 */
	public Object resolveDependencyQuietly(ResourceConfig config) {
		final IdObjectPair objectPair = dependencies.get(config);
		return objectPair == null ? null : objectPair.getObject();
	}

	/**
	 * No object, we are just mapping ids to configurations.
	 */
	public void addConfig(String id, ResourceConfig config) {
		testUniqueness(config);
		dependencies.put(config, new IdObjectPair(id, null));
	}

	public void addDependency(ResourceConfig config, Object dependency) {
		testUniqueness(config);
		dependencies.put(config, new IdObjectPair(String.valueOf(id++), dependency));
	}

	private void testUniqueness(ResourceConfig config) {
		if (dependencies.containsKey(config)) {
			throw new MprcException("Dependency  for " + config + " already defined.");
		}
	}

	public String getIdFromDependency(Object dependency) {
		for (IdObjectPair idObject : dependencies.values()) {
			if (idObject.getObject().equals(dependency)) {
				return idObject.getId();
			}
		}
		return null;
	}

	public Object getDependencyFromId(String id) {
		for (IdObjectPair idObject : dependencies.values()) {
			if (idObject.getId().equals(id)) {
				return idObject.getObject();
			}
		}
		return null;
	}

	public String getIdFromConfig(ResourceConfig config) {
		final IdObjectPair objectPair = dependencies.get(config);
		return objectPair == null ? null : objectPair.getId();
	}

	public ResourceConfig getConfigFromId(String id) {
		for (Map.Entry<ResourceConfig, IdObjectPair> entry : dependencies.entrySet()) {
			if (entry.getValue().getId().equals(id)) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * This is actually quite nasty. We are saying that the same model's corresponding config has changed from
	 * one object to another.
	 *
	 * @param oldConfig Old resource that used to be mapped.
	 * @param newConfig New resource to map in its place.
	 */
	public void changeConfigType(ResourceConfig oldConfig, ResourceConfig newConfig) {
		final IdObjectPair idObjectPair = dependencies.get(oldConfig);
		dependencies.remove(oldConfig);
		dependencies.put(newConfig, idObjectPair);
	}

	private static final class IdObjectPair {
		private final String id;
		private final Object object;

		private IdObjectPair(String id, Object object) {
			this.id = id;
			this.object = object;
		}

		public String getId() {
			return id;
		}

		public Object getObject() {
			return object;
		}
	}
}
                                       	