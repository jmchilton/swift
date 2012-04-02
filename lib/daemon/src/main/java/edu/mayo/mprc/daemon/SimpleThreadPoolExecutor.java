package edu.mayo.mprc.daemon;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A thread pool executor that will potentially block if too much work is asked of it.
 */
public final class SimpleThreadPoolExecutor extends ThreadPoolExecutor {

	public SimpleThreadPoolExecutor(final int numThreads, final String threadName, boolean blockIfFull) {
		super(numThreads, numThreads, 1, TimeUnit.SECONDS, blockIfFull ? new SynchronousQueue<Runnable> () : new LinkedBlockingQueue<Runnable>(numThreads));

		final String name = threadName == null ? "worker" : threadName;

		setThreadFactory(new ThreadFactoryBuilder()
				.setDaemon(false)
				.setNameFormat(name + "-%d")
				.build());
	}
}
