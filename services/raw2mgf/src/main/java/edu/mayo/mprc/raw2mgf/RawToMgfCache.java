package edu.mayo.mprc.raw2mgf;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkCache;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.utilities.FileUtilities;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class RawToMgfCache extends WorkCache<RawToMgfWorkPacket> {

	public static final String TYPE = "raw2mgfCache";
	public static final String NAME = ".RAW to .mgf Cache";
	public static final String DESC = "<p>Caches .mgf files previously converted from .RAW. This can speed up operation if one file is being processed multiple times.</p>";

	public RawToMgfCache() {
	}

	public static final class Config extends CacheConfig {
		public Config() {
		}
	}

	public static final class Factory extends WorkCache.Factory<Config> {
		private static RawToMgfCache cache;

		@Override
		public WorkCache getCache() {
			return cache;
		}

		@Override
		public WorkCache createCache(Config config, DependencyResolver dependencies) {
			return cache = new RawToMgfCache();
		}
	}

	public static final class Ui implements ServiceUiFactory {
		private static final String DEFAULT_CACHE = "var/cache/mgf";

		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder
					.property(CacheConfig.CACHE_FOLDER, ".mgf cache folder", "When a .RAW file gets converted to .mgf, the result is stored in this folder. Subsequent conversions of the same file use the cached value."
							+ "<p>Ideally, this folder would be on a fast, potentially less reliable storage.</p>")
					.required()
					.defaultValue(DEFAULT_CACHE)

					.property(CacheConfig.SERVICE, "Raw To MGF Convertor", "The module that will do the conversion. The cache just caches the results.")
					.reference("raw2mgf", UiBuilder.NONE_TYPE);
		}
	}
}
