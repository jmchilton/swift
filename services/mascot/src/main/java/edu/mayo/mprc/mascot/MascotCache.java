package edu.mayo.mprc.mascot;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkCache;
import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.utilities.FileUtilities;

import java.io.File;

public final class MascotCache extends WorkCache<MascotWorkPacket> {
	public static final String TYPE = "mascotCache";
	public static final String NAME = "Mascot Cache";
	public static final String DESC = "Caches previous Mascot search results. <p>Speeds up consecutive Mascot searches if the same file with same parameters is processed multiple times.</p>";


	@Override
	public void userProgressInformation(File wipFolder, ProgressInfo progressInfo) {
		// We store the extra Mascot URL as a special file so we can report it to the user later
		if (progressInfo instanceof MascotResultUrl) {
			MascotResultUrl mascotResultUrl = (MascotResultUrl) progressInfo;
			FileUtilities.writeStringToFile(new File(wipFolder, MascotWorkPacket.MASCOT_URL_FILENAME), mascotResultUrl.getMascotUrl(), true);
		}
	}

	public static final class Config extends CacheConfig {
		public Config() {
		}
	}

	public static final class Factory extends WorkCache.Factory<Config> {
		private static MascotCache cache;

		@Override
		public WorkCache getCache() {
			return cache;
		}

		@Override
		public WorkCache createCache(Config config, DependencyResolver dependencies) {
			cache = new MascotCache();
			return cache;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		private static final String DEFAULT_CACHE = "var/cache/mascot";

		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder.property(CacheConfig.CACHE_FOLDER, "Mascot cache folder", "When a file gets searched by Mascot, the result is stored in this folder. Subsequent searches of the same file with same parameters use the cached value."
					+ "<p>Ideally, this folder would be on a fast, potentially less reliable storage.</p>")
					.required()
					.defaultValue(DEFAULT_CACHE)

					.property(CacheConfig.SERVICE, "Mascot Search Engine", "The Mascot engine that will do the search. The cache just caches the results.")
					.reference(MascotWorker.TYPE, UiBuilder.NONE_TYPE);
		}
	}
}
