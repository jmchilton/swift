package edu.mayo.mprc.daemon;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Simple wrapper around the Java's executor allowing easy creation from Spring.
 */
public final class SimpleThreadPoolExecutor extends java.util.concurrent.ThreadPoolExecutor {

	public SimpleThreadPoolExecutor(int numThreads, String threadName) {
		super(numThreads, numThreads, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

		final String name = threadName == null ? "worker" : threadName;

		setThreadFactory(new ThreadFactoryBuilder()
				.setDaemon(false)
				.setNameFormat(name + "-%d")
				.build());
	}
}
