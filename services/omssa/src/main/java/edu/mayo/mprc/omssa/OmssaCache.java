package edu.mayo.mprc.omssa;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkCache;

public final class OmssaCache extends WorkCache<OmssaWorkPacket> {
	public static final String TYPE = "omssaCache";
	public static final String NAME = "OMSSA Cache";
	public static final String DESC = "Caches previous OMSSA search results. <p>Speeds up consecutive OMSSA searches if the same file with same parameters is processed multiple times.</p>";

	public OmssaCache() {
	}

	public static final class Config extends WorkCache.CacheConfig {
		public Config() {
		}
	}

	public static final class Factory extends WorkCache.Factory<Config> {
		private static OmssaCache cache;

		@Override
		public WorkCache getCache() {
			return cache;
		}

		@Override
		public WorkCache createCache(Config config, DependencyResolver dependencies) {
			return cache = new OmssaCache();
		}
	}

	public static final class Ui implements ServiceUiFactory {
		private static final String DEFAULT_CACHE = "var/cache/omssa";

		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder
					.property(WorkCache.CacheConfig.CACHE_FOLDER, "OMSSA cache folder", "When a file gets searched by OMSSA, the result is stored in this folder. Subsequent searches of the same file with same parameters use the cached value."
							+ "<p>Ideally, this folder would be on a fast, potentially less reliable storage.</p>")
					.required()
					.defaultValue(DEFAULT_CACHE)

					.property(WorkCache.CacheConfig.SERVICE, "Omssa Search Engine", "The Omssa engine that will do the search. The cache just caches the results.")
					.reference("omssa", UiBuilder.NONE_TYPE);
		}
	}
}
