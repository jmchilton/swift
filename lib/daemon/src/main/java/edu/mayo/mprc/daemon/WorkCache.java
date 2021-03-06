package edu.mayo.mprc.daemon;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ServiceConfig;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.daemon.progress.ProgressListener;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.StringUtilities;
import edu.mayo.mprc.utilities.exceptions.ExceptionUtilities;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for implementing caches. A cache remembers previous work and can provide results fast.
 * To do so, it needs to store a file describing the previous task.
 * <p/>
 * The cache understands {@link CachableWorkPacket#isPublishResultFiles()} - if this
 * feature is enabled, the cache will copy the cached result to the originally requested target directory.
 * <p/>
 * When the cache detects a particular work is currently being performed, the new request is queued and response duplicated
 * from the currently running task. E.g. two users running the same search will both
 * have an illusion that a separate search runs for them, although only one search is running.
 *
 * @param <T>
 */
public abstract class WorkCache<T extends WorkPacket> implements NoLoggingWorker {
	private static final Logger LOGGER = Logger.getLogger(WorkCache.class);
	private File cacheFolder;
	private DaemonConnection daemon;
	private final Map<String, CacheProgressReporter> workInProgress = new HashMap<String, CacheProgressReporter>(10);

	public WorkCache() {
	}

	public final File getCacheFolder() {
		return cacheFolder;
	}

	public final void setCacheFolder(File cacheFolder) {
		this.cacheFolder = cacheFolder;
	}

	public final DaemonConnection getDaemon() {
		return daemon;
	}

	public final void setDaemon(DaemonConnection daemon) {
		this.daemon = daemon;
	}

	public void userProgressInformation(File wipFolder, ProgressInfo progressInfo) {
		// Do nothing
	}

	@Override
	public final void processRequest(WorkPacket workPacket, ProgressReporter progressReporter) {
		try {
			process(workPacket, progressReporter);
			workPacket.synchronizeFileTokensOnReceiver();
		} catch (Exception t) {
			progressReporter.reportFailure(t);
		}
	}

	/**
	 * By default returns folder based on the hash code of the task description.
	 * Your cache can override this implementation.
	 *
	 * @param taskDescription Description of the task.
	 * @return Relative path to the cache folder to store the task results in.
	 */
	protected String getFolderForTaskDescription(final String taskDescription) {
		int code = taskDescription.hashCode();
		return "" +
				StringUtilities.toHex(code >> 28) +
				StringUtilities.toHex(code >> 24) +
				"/" +
				StringUtilities.toHex(code >> 20) +
				StringUtilities.toHex(code >> 16) +
				"/" +
				StringUtilities.toHex(code >> 12) +
				StringUtilities.toHex(code >> 8) +
				"/" +
				StringUtilities.toHex(code >> 4) +
				StringUtilities.toHex(code);
	}

	private void process(WorkPacket workPacket, ProgressReporter progressReporter) {
		T typedWorkPacket = (T) workPacket;
		final CachableWorkPacket cachableWorkPacket;
		if (workPacket instanceof CachableWorkPacket) {
			cachableWorkPacket = (CachableWorkPacket) workPacket;
		} else {
			ExceptionUtilities.throwCastException(workPacket, CachableWorkPacket.class);
			return;
		}

		// A string describing the request. If two descriptions are the same,
		// the tasks are the same
		final String taskDescription = cachableWorkPacket.getStringDescriptionOfTask();

		// The file that will store the task input parameters
		final String taskDescriptionFileName = "_task_description";

		// Obtain a list of files we expect as a result of this task
		final List<String> outputFiles = cachableWorkPacket.getOutputFiles();

		// Request hashcode
		// Folder is derived from the hash code
		File targetCacheFolder = new File(cacheFolder, getFolderForTaskDescription(taskDescription));
		// We make sure the target folder can be created - fail early
		FileUtilities.ensureFolderExists(targetCacheFolder);

		// Now we check the cache.
		// There can be multiple files of the same name in the same cache bucket.
		// We pick the one which has a corresponding file that matches our params
		// We go through all subfolders of the output folder
		final File[] files = targetCacheFolder.listFiles();
		for (File subFolder : files) {
			final File taskDescriptionFile = new File(subFolder, taskDescriptionFileName);
			if (allFilesExist(subFolder, outputFiles) && taskDescriptionFile.exists()) {
				// We found an output file with matching file name and a params file!
				// Check the params file
				String cachedTaskDescription = null;
				try {
					cachedTaskDescription = Files.toString(taskDescriptionFile, Charsets.UTF_8);
				} catch (Exception t) {
					LOGGER.error("Cache cannot read request file " + taskDescriptionFile.getAbsolutePath(), t);
					continue;
				}
				if (taskDescription.equals(cachedTaskDescription)) {
					// We found a match. Shall we use it? We must not want to process from scratch, and we must not
					// have stale cache entry.
					if (!typedWorkPacket.isFromScratch() && !cachableWorkPacket.cacheIsStale(subFolder, outputFiles)) {
						// The output was created after our input file, thus it is useable
						LOGGER.info("Using cached values from: " + subFolder.getAbsolutePath());
						progressReporter.reportStart();
						cachableWorkPacket.reportCachedResult(progressReporter, subFolder, outputFiles);
						publishResultFiles(cachableWorkPacket, subFolder, outputFiles);
						progressReporter.reportSuccess();
						return;
					} else {
						// The output is older than the source.
						// Wipe the cache for the file, continue searching.
						LOGGER.info("Cache deleting stale entry " +
								(typedWorkPacket.isFromScratch() ? "(user requested rerun from scratch)" : "(input is of newer date than the output)") + ": " + subFolder.getAbsolutePath());
						FileUtilities.deleteNow(subFolder);
					}
				}
			}
		}

		// We have not found a suitable cache entry
		// But maybe someone is working on this right now
		final CacheProgressReporter cacheProgressReporter;
		final CacheProgressReporter newReporter = new CacheProgressReporter();
		synchronized (workInProgress) {
			cacheProgressReporter = workInProgress.get(taskDescription);
			if (cacheProgressReporter == null) {
				workInProgress.put(taskDescription, newReporter);
				newReporter.addProgressReporter(progressReporter);
			} else {
				cacheProgressReporter.addProgressReporter(progressReporter);
			}
		}

		if (cacheProgressReporter == null) {
			// Make a work-in-progress folder
			final File wipBase = new File(cacheFolder, "wip");
			File wipFolder = FileUtilities.createTempFolder(wipBase, "wip", true);

			WorkPacket modifiedWorkPacket = cachableWorkPacket.translateToWorkInProgressPacket(wipFolder);

			final MyProgressListener listener = new MyProgressListener(cachableWorkPacket, wipFolder, targetCacheFolder, outputFiles, taskDescriptionFileName, taskDescription, newReporter);
			daemon.sendWork(modifiedWorkPacket, listener);
		}
	}

	/**
	 * Checks whether the work packet requested publishing the intermediate files.
	 * If so, copy the intermediate files to the originally requested target.
	 */
	private void publishResultFiles(CachableWorkPacket workPacket, File outputFolder, List<String> outputFiles) {
		if (workPacket.isPublishResultFiles()) {
			final File targetFolder = workPacket.getOutputFile().getParentFile();
			FileUtilities.ensureFolderExists(targetFolder);
			for (String outputFile : outputFiles) {
				FileUtilities.copyFile(new File(outputFolder, outputFile), new File(targetFolder, outputFile), true);
			}
		}
	}

	/**
	 * @param folder      Base folder
	 * @param outputFiles List of file names to check
	 * @return True if all files exist.
	 */
	private boolean allFilesExist(File folder, List<String> outputFiles) {
		for (String file : outputFiles) {
			if (!new File(folder, file).exists()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * For test writing purposes. Access to class internals.
	 */
	boolean isWorkInProgress() {
		return workInProgress.size() != 0;
	}

	private final class MyProgressListener implements ProgressListener {
		private CachableWorkPacket workPacket;
		private File wipFolder;
		private List<String> outputFiles;
		private String taskDescriptionFile;
		private String taskDescription;
		private File targetFolder;
		private ProgressReporter reporter;
		// How long do we wait for a file to appear after the worker claims success (ms)
		public static final int FILE_WAIT_TIMEOUT = 2 * 60 * 1000;

		private MyProgressListener(CachableWorkPacket workPacket, File wipFolder, File targetCacheFolder, List<String> outputFiles, String taskDescriptionFile, String taskDescription, ProgressReporter reporter) {
			this.workPacket = workPacket;
			this.wipFolder = wipFolder;
			this.targetFolder = targetCacheFolder;
			this.outputFiles = outputFiles;
			this.taskDescriptionFile = taskDescriptionFile;
			this.taskDescription = taskDescription;
			this.reporter = reporter;
		}

		@Override
		public void requestEnqueued(String hostString) {
		}

		@Override
		public void requestProcessingStarted() {
			reporter.reportStart();
		}

		@Override
		public void requestProcessingFinished() {
			try {
				int i = 1;
				while (true) {
					File newFolder = new File(targetFolder, String.valueOf(i));
					if (!newFolder.exists()) {
						FileUtilities.ensureFolderExists(newFolder);
						break;
					}
					i = i + 1;
					if (i > 1000 * 10) {
						throw new MprcException("Too many cached folders in " + targetFolder.getAbsolutePath() + ": " + i);
					}
				}
				File target = new File(targetFolder, String.valueOf(i));
				for (String outputFile : outputFiles) {
					// Move the work in progress folder to its final location
					final File wipFile = new File(wipFolder, outputFile);
					final File resultingOutputFile = new File(target, outputFile);

					FileUtilities.waitForFile(wipFile, FILE_WAIT_TIMEOUT);
					LOGGER.info("Caching output file: " + resultingOutputFile.getAbsolutePath());

					// We move the output file
					FileUtilities.rename(wipFile, resultingOutputFile);
				}

				// We write out the parameters used for creating the output file
				FileUtilities.writeStringToFile(new File(target, taskDescriptionFile), taskDescription, true);
				// And the wip folder is no longer needed
				FileUtilities.deleteNow(wipFolder);

				// Now we only need to notify the requestor that the output file was produced elsewhere
				workPacket.reportCachedResult(reporter, target, outputFiles);
				publishResultFiles(workPacket, target, outputFiles);
			} catch (Exception t) {
				reporter.reportFailure(t);
				return;
			} finally {
				synchronized (workInProgress) {
					workInProgress.remove(taskDescription);
				}
			}
			reporter.reportSuccess();
		}

		@Override
		public void requestTerminated(DaemonException e) {
			try {
				// The work in progress folder can be scratched
				FileUtilities.deleteNow(wipFolder);
				reporter.reportFailure(e);
			} finally {
				synchronized (workInProgress) {
					workInProgress.remove(taskDescription);
				}
			}
		}

		@Override
		public void userProgressInformation(ProgressInfo progressInfo) {
			// Let the cache know what happened
			WorkCache.this.userProgressInformation(wipFolder, progressInfo);
			reporter.reportProgress(progressInfo);
		}
	}

	/**
	 * The cache factory creates singletons - there is only one cache for all requests, unlike the workers
	 * that are created for each task separately.
	 *
	 * @param <S> Configuration the cache takes to set itself up.
	 */
	public abstract static class Factory<S extends CacheConfig> extends WorkerFactoryBase<S> {
		public abstract WorkCache getCache();

		@Override
		public synchronized Worker create(S config, DependencyResolver dependencies) {
			WorkCache cache = getCache();
			if (cache == null) {
				cache = createCache(config, dependencies);
				cache.setCacheFolder(new File(config.getCacheFolder()).getAbsoluteFile());
				cache.setDaemon((DaemonConnection) dependencies.createSingleton(config.getService()));
			}
			return cache;
		}

		public abstract WorkCache createCache(S config, DependencyResolver dependencies);
	}

	/**
	 * Generic work cache config. A work cache knows its folder and the service whose output it is caching.
	 */
	public static class CacheConfig implements ResourceConfig {
		public static final String CACHE_FOLDER = "cacheFolder";
		public static final String SERVICE = "service";
		private String cacheFolder;
		private ServiceConfig service;

		public String getCacheFolder() {
			return cacheFolder;
		}

		public ServiceConfig getService() {
			return service;
		}

		@Override
		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("cacheFolder", cacheFolder);
			map.put("service", resolver.getIdFromConfig(service));
			return map;
		}

		@Override
		public void load(Map<String, String> values, DependencyResolver resolver) {
			cacheFolder = values.get("cacheFolder");
			service = (ServiceConfig) resolver.getConfigFromId(values.get("service"));
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}
}
