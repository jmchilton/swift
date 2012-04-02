package edu.mayo.mprc.daemon;

import org.testng.annotations.Test;

/**
 * @author Roman Zenka
 */
public final class TestThreadPoolExecutor {
	@Test
	public void shouldBlock() {
		SimpleThreadPoolExecutor executor = new SimpleThreadPoolExecutor(1, "test thread", true);
		executor.execute(new MyRunnable());
		executor.execute(new MyRunnable());
		// This would break here if the executor did not block on the second execute call
	}

	private static class MyRunnable implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// SWALLOWED
			}
		}
	}
}
