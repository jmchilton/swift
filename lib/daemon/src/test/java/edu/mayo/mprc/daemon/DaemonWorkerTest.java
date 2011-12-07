package edu.mayo.mprc.daemon;

import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the daemon workers.
 */
public final class DaemonWorkerTest {

	@Test
	public void shouldDoSimpleWork() throws InterruptedException {
		DaemonWorkerTester tester = new DaemonWorkerTester(createSimpleWorker());
		runTest(tester, 2);
	}

	@Test
	public void shouldDoSimpleWorkInThreadPool() throws InterruptedException {
		DaemonWorkerTester tester = new DaemonWorkerTester(new WorkerFactory() {
			public Worker createWorker() {
				return createSimpleWorker();
			}

			public String getDescription() {
				return "DaemonWorkerTester";
			}
		}, 3);
		runTest(tester, 6);
	}

	private static final class StringWorkPacket extends WorkPacketBase {
		private static final long serialVersionUID = 20101221L;
		private String value;

		private StringWorkPacket(String value) {
			super(value, false);
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	private static Worker createSimpleWorker() {
		return new Worker() {
			private AtomicInteger concurrentRequests = new AtomicInteger(0);

			public void processRequest(WorkPacket workPacket, ProgressReporter progressReporter) {
				try {
					progressReporter.reportStart();
					process(workPacket);
					workPacket.synchronizeFileTokensOnReceiver();
					progressReporter.reportSuccess();
				} catch (Exception t) {
					progressReporter.reportFailure(t);
				}
			}

			private void process(WorkPacket wp) {
				Assert.assertEquals(concurrentRequests.incrementAndGet(), 1, "The amount of requests must start at 1. The worker calls are not serialized.");
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					throw new DaemonException(e);
				}
				Assert.assertEquals(concurrentRequests.decrementAndGet(), 0, "The amount of requests must end at 0. The worker calls are not serialized.");
			}
		};
	}

	private static void runTest(DaemonWorkerTester tester, int iterations) throws InterruptedException {
		Object[] token = new Object[iterations];
		for (int i = 0; i < iterations; i++) {
			token[i] = tester.sendWork(new StringWorkPacket("hello #" + String.valueOf(i)), null);
		}
		/**
		 * Give the search at most 10 seconds.
		 */
		for (int i = 0; i < 10000; i++) {
			boolean allDone = true;
			for (int j = 0; j < iterations; j++) {
				if (!tester.isDone(token[j])) {
					allDone = false;
					break;
				}
			}
			if (allDone) {
				break;
			}
			Thread.sleep(10);
			i += 10;
		}
		tester.stop();
		for (int i = 0; i < iterations; i++) {
			Assert.assertTrue(tester.isDone(token[i]), "Work is not done");
			if (!tester.isSuccess(token[i])) {
				throw new DaemonException(tester.getLastError(token[i]));
			}
		}
	}
}
