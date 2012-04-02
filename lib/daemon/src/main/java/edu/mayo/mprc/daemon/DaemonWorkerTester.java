package edu.mayo.mprc.daemon;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfigInfo;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.messaging.Service;
import edu.mayo.mprc.messaging.ServiceFactory;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.progress.ProgressInfo;
import edu.mayo.mprc.utilities.progress.ProgressListener;
import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test harness for {@link edu.mayo.mprc.daemon.Worker} classes. Wrap a daemon worker
 */
public final class DaemonWorkerTester {
	private static final Logger LOGGER = Logger.getLogger(DaemonWorkerTester.class);
	private SimpleRunner runner;
	private Service service;
	private DaemonConnection daemonConnection;
	private static AtomicInteger testId = new AtomicInteger(0);

	/**
	 * Creates a test runner that runs given worker in a single thread. Automatically starts a thread.
	 *
	 * @param worker Worker to run
	 */
	public DaemonWorkerTester(final Worker worker) {
		try {
			final URI uri = new URI("jms.vm://broker1?simplequeue=test_" + String.valueOf(testId.incrementAndGet()));
			initializeFromUri(uri);
			this.runner = new SimpleRunner();
			this.runner.setWorker(worker);
			this.runner.setDaemonConnection(this.daemonConnection);
			this.runner.setEnabled(true);
			this.runner.setLogOutputFolder(FileUtilities.createTempFolder());
		} catch (URISyntaxException e) {
			throw new MprcException(e);
		}
		this.runner.start();
		waitUntilReady(this.runner);
	}

	private void initializeFromUri(final URI uri) {
		this.service = new ServiceFactory().createService(uri);
		final FileTokenFactory fileTokenFactory = new FileTokenFactory();
		fileTokenFactory.setDaemonConfigInfo(new DaemonConfigInfo("daemon1", "shared"));
		this.daemonConnection = new DirectDaemonConnection(service, fileTokenFactory);
	}


	/**
	 * Creates a test runner that runs given worker. Automatically starts running.
	 *
	 * @param workerFactory    Creates workers to run
	 * @param numWorkerThreads How many threads does the worker run in
	 */
	public DaemonWorkerTester(final WorkerFactory workerFactory, final int numWorkerThreads) {
		try {
			final URI uri = new URI("jms.vm://broker1?simplequeue=test_" + String.valueOf(testId.incrementAndGet()));
			initializeFromUri(uri);
			this.runner = new SimpleRunner();
			this.runner.setFactory(workerFactory);
			this.runner.setExecutorService(new SimpleThreadPoolExecutor(numWorkerThreads, "test", true));
			this.runner.setDaemonConnection(this.daemonConnection);
			this.runner.setEnabled(true);
			this.runner.setLogOutputFolder(FileUtilities.createTempFolder());
		} catch (URISyntaxException e) {
			throw new MprcException(e);
		}
		this.runner.start();
		waitUntilReady(this.runner);
	}

	/**
	 * Sends a work packet to the tested worker.
	 *
	 * @param workPacket Data to be processed.
	 * @return Token for this work packet. The token is an opaque object to be used by {@link #isDone}, {@link #isSuccess} and {@link #getLastError}
	 *         methods.
	 */
	public Object sendWork(final WorkPacket workPacket) {
		final TestProgressListener listener = new TestProgressListener();
		daemonConnection.sendWork(workPacket, listener);
		return listener;
	}

	/**
	 * Sends a work packet to the tested worker.
	 *
	 * @param workPacket   Data to be processed.
	 * @param userListener Listener to be called as the worker progresses.
	 * @return Token for this work packet. The token is an opaque object to be used by {@link #isDone}, {@link #isSuccess} and {@link #getLastError}
	 *         methods.
	 */
	public Object sendWork(final WorkPacket workPacket, final ProgressListener userListener) {
		final TestProgressListener listener = new TestProgressListener(userListener);
		daemonConnection.sendWork(workPacket, listener);
		return listener;
	}

	/**
	 * Stop the execution of the daemon as soon as possible (the thread does not quit immediatelly, so
	 * there may still be some progress info being sent back).
	 */
	public void stop() {
		this.runner.stop();
	}

	/**
	 * @param workToken Token for work sent through {@link #sendWork}.
	 * @return true if there was a search running that is done now.
	 */
	public boolean isDone(final Object workToken) {
		final TestProgressListener listener = (TestProgressListener) workToken;
		return listener != null && listener.isDone();
	}

	/**
	 * @param workToken Token for work sent through {@link #sendWork}.
	 * @return true if there was a search running which succeeded
	 */
	public boolean isSuccess(final Object workToken) {
		final TestProgressListener listener = (TestProgressListener) workToken;
		return listener != null && listener.isSuccess();
	}

	/**
	 * @param workToken Token for work sent through {@link #sendWork}.
	 * @return The last error in case there was any, null otherwise.
	 */
	public Throwable getLastError(final Object workToken) {
		final TestProgressListener listener = (TestProgressListener) workToken;
		return listener.getLastError();
	}

	public DaemonConnection getDaemonConnection() {
		return daemonConnection;
	}

	/**
	 * Wait until the daemon signalizes it has started up. This is important for unit tests.
	 *
	 * @param runner
	 */
	public static void waitUntilReady(final AbstractRunner runner) {
		if (!runner.isEnabled()) {
			return;
		}
		while (!runner.isOperational()) {
			try {
				// Yield
				Thread.sleep(0);
			} catch (InterruptedException e) {
				throw new MprcException("Daemon worker startup interrupted", e);
			}
		}
		LOGGER.info("The runner for " + runner.toString() + " is up and ready.");
	}

	private static final class TestProgressListener implements ProgressListener {
		private boolean success = false;
		private boolean done = false;
		private Throwable lastError = null;
		private ProgressListener userListener = null;

		TestProgressListener() {
		}

		private TestProgressListener(final ProgressListener userListener) {
			this.userListener = userListener;
		}

		public boolean isSuccess() {
			return success;
		}

		public boolean isDone() {
			return done;
		}

		public Throwable getLastError() {
			return lastError;
		}

		public void requestEnqueued(final String hostString) {
			if (null != userListener) {
				userListener.requestEnqueued(hostString);
			}
		}

		public void requestProcessingStarted() {
			LOGGER.debug("Starting work");
			if (null != userListener) {
				userListener.requestProcessingStarted();
			}
		}

		public void requestProcessingFinished() {
			success = true;
			done = true;
			LOGGER.debug("Work successful");
			if (null != userListener) {
				userListener.requestProcessingFinished();
			}
		}

		public void requestTerminated(final Exception e) {
			LOGGER.error("Work failed", e);
			lastError = e;
			done = true;
			if (null != userListener) {
				userListener.requestTerminated(e);
			}
		}

		public void userProgressInformation(final ProgressInfo progressInfo) {
			LOGGER.debug("Work progress: " + progressInfo.toString());
			if (null != userListener) {
				userListener.userProgressInformation(progressInfo);
			}
		}
	}

}
