package edu.mayo.mprc.sequest;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkCache;

public final class SequestCache extends WorkCache<SequestMGFWorkPacket> {

	public static final String TYPE = "sequestCache";
	public static final String NAME = "Sequest Cache";
	public static final String DESC = "Caches previous Sequest search results. <p>Speeds up consecutive Sequest searches if the same file with same parameters is processed multiple times.</p>";

	public SequestCache() {
	}


	public static final class Config extends CacheConfig {
		public Config() {
		}
	}

	public static final class Factory extends WorkCache.Factory<Config> {
		private static SequestCache cache;

		@Override
		public WorkCache getCache() {
			return cache;
		}

		@Override
		public WorkCache createCache(Config config, DependencyResolver dependencies) {
			return cache = new SequestCache();
		}
	}

	public static final class Ui implements ServiceUiFactory {
		private static final String DEFAULT_CACHE = "var/cache/sequest";

		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder
					.property(CacheConfig.CACHE_FOLDER, "Sequest cache folder", "When a file gets searched by Sequest, the result is stored in this folder. Subsequent searches of the same file with same parameters use the cached value."
							+ "<p>Ideally, this folder would be on a fast, potentially less reliable storage.</p>")
					.required()
					.defaultValue(DEFAULT_CACHE)

					.property(CacheConfig.SERVICE, "Sequest Search Engine", "The Sequest engine that will do the search. The cache just caches the results.")
					.reference("sequest", UiBuilder.NONE_TYPE);
		}
	}
}
