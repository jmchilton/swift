package edu.mayo.mprc.xtandem;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkCache;

public final class XTandemCache extends WorkCache<XTandemWorkPacket> {
	public static final String TYPE = "tandemCache";
	public static final String NAME = "X!Tandem Cache";
	public static final String DESC = "Caches previous X!Tandem search results. <p>Speeds up consecutive X!Tandem searches if the same file with same parameters is processed multiple times.</p>";

	public XTandemCache() {
	}

	public static final class Config extends CacheConfig {
		public Config() {
		}
	}

	public static final class Factory extends WorkCache.Factory<Config> {
		private static XTandemCache cache;

		@Override
		public WorkCache getCache() {
			return cache;
		}

		@Override
		public WorkCache createCache(Config config, DependencyResolver dependencies) {
			return cache = new XTandemCache();
		}
	}

	public static final class Ui implements ServiceUiFactory {
		private static final String DEFAULT_CACHE = "var/cache/tandem";

		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder
					.property(CacheConfig.CACHE_FOLDER, "X!Tandem cache folder", "When a file gets searched by X!Tandem, the result is stored in this folder. Subsequent searches of the same file with same parameters use the cached value."
							+ "<p>Ideally, this folder would be on a fast, potentially less reliable storage.</p>")
					.required()
					.defaultValue(DEFAULT_CACHE)

					.property(CacheConfig.SERVICE, "X!Tandem Search Engine", "The X!Tandem engine that will do the search. The cache just caches the results.")
					.reference("tandem", UiBuilder.NONE_TYPE);
		}
	}
}
