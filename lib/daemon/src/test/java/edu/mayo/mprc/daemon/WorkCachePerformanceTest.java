package edu.mayo.mprc.daemon;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfigInfo;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.daemon.progress.ProgressListener;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.messaging.Service;
import edu.mayo.mprc.messaging.ServiceFactory;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test that the work cache boosts performance - running 10 identical tasks should take roughly the same time as
 * running just one of them.
 */
public final class WorkCachePerformanceTest {
	private static final Logger LOGGER = Logger.getLogger(WorkCachePerformanceTest.class);
	// How long for the entire run
	private static final int RUN_TIME = 2000;

	// How much will each task take
	private static final int TASK_RUNTIME = 500;
	private static final int TOTAL_MESSAGES = 10;
	private final Object workSuccess = new Object();
	private int workSuccessCount = 0;

	@Test
	public void shouldAccelerateWithCache() throws URISyntaxException, InterruptedException {
		File logFolder = FileUtilities.createTempFolder();
		File cacheFolder = FileUtilities.createTempFolder();
		File cacheLogFolder = new File(cacheFolder, "cache_log");
		FileUtilities.ensureFolderExists(cacheLogFolder);

		DaemonConfigInfo daemonConfigInfo = new DaemonConfigInfo("test", FileUtilities.getDefaultTempDirectory().getAbsolutePath());
		FileTokenFactory fileTokenFactory = new FileTokenFactory(daemonConfigInfo);

		final TestWorker worker = new TestWorker();

		SimpleRunner runner = wrapWithRunner(worker, "simpleTestQueue", logFolder, fileTokenFactory);
		runner.start();

		TestWorkCache cache = new TestWorkCache();
		cache.setCacheFolder(cacheFolder);

		cache.setDaemon(runner.getDaemonConnection());

		final SimpleRunner cacheRunner = wrapWithRunner(cache, "simpleTestCache", cacheLogFolder, fileTokenFactory);
		cacheRunner.start();

		DaemonConnection connection = cacheRunner.getDaemonConnection();

		final MyProgressListener listener = new MyProgressListener();

		for (int i = 0; i < TOTAL_MESSAGES; i++) {
			final SimpleTestWorkPacket workPacket = new SimpleTestWorkPacket("task1-test", false);
			workPacket.setResultFile(new File("text.txt"));
			connection.sendWork(workPacket, listener);
		}

		long currentTime = System.currentTimeMillis();
		while (true) {
			long time = System.currentTimeMillis();
			synchronized (workSuccess) {
				if (workSuccessCount == TOTAL_MESSAGES || time > currentTime + RUN_TIME) {
					break;
				} else {
					workSuccess.wait(RUN_TIME / 10);
				}
			}
		}

		long timeElapsed = System.currentTimeMillis() - currentTime;
		runner.stop();
		cacheRunner.stop();
		synchronized (workSuccess) {
			Assert.assertEquals(workSuccessCount, TOTAL_MESSAGES, "Wrong amount of successfully processed work packets");
		}

		Assert.assertEquals(listener.getProgressInfos().size(), TOTAL_MESSAGES);

		FileUtilities.cleanupTempFile(logFolder);
		FileUtilities.cleanupTempFile(cacheFolder);

		Assert.assertTrue(timeElapsed < 3 * TASK_RUNTIME, "The total running time is expected to be " + TASK_RUNTIME + "ms but was " + timeElapsed + "ms");
	}

	private SimpleRunner wrapWithRunner(Worker worker, String queueName, File logFolder, FileTokenFactory fileTokenFactory) throws URISyntaxException {
		Service service = ServiceFactory.createJmsQueue(new URI("jms.vm://test?broker.useJmx=false&broker.persistent=false&simplequeue=" + queueName));
		DirectDaemonConnection directConnection = new DirectDaemonConnection(service, fileTokenFactory);
		SimpleRunner runner = new SimpleRunner();
		runner.setLogOutputFolder(logFolder);
		runner.setWorker(worker);
		runner.setDaemonConnection(directConnection);
		return runner;
	}

	private class TestWorkCache extends WorkCache<SimpleTestWorkPacket> {
	}

	private class TestWorker implements Worker {
		@Override
		public void processRequest(WorkPacket workPacket, ProgressReporter progressReporter) {
			progressReporter.reportStart();
			try {
				Thread.sleep(TASK_RUNTIME);
				LOGGER.debug("Request completed");
				SimpleTestWorkPacket testWorkPacket = (SimpleTestWorkPacket) workPacket;
				FileUtilities.ensureFileExists(testWorkPacket.getResultFile());
			} catch (InterruptedException e) {
				throw new MprcException(e);
			}
			progressReporter.reportSuccess();
		}
	}

	private class MyProgressListener implements ProgressListener {
		private List<ProgressInfo> progressInfos = new ArrayList<ProgressInfo>();

		@Override
		public void requestEnqueued(String hostString) {
		}

		@Override
		public void requestProcessingStarted() {
		}

		@Override
		public void requestProcessingFinished() {
			synchronized (workSuccess) {
				workSuccessCount++;
				if (workSuccessCount > TOTAL_MESSAGES) {
					Assert.assertEquals(workSuccessCount, TOTAL_MESSAGES, "Too many messages delivered");
				}
				workSuccess.notifyAll();
			}
		}

		@Override
		public void requestTerminated(DaemonException e) {
			Assert.fail("Unexpected exception", e);
		}

		@Override
		public void userProgressInformation(ProgressInfo progressInfo) {
			progressInfos.add(progressInfo);
		}

		public List<ProgressInfo> getProgressInfos() {
			return progressInfos;
		}
	}
}
