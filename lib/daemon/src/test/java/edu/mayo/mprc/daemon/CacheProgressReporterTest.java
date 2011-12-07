package edu.mayo.mprc.daemon;

import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import org.mockito.InOrder;
import org.testng.annotations.Test;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public final class CacheProgressReporterTest {

	@Test
	public static void shouldDistributeCalls() {
		// Make sure the calls are passed to a reporter that is already established
		ProgressReporter reporter1 = mock(ProgressReporter.class);
		ProgressInfo progressInfo = mock(ProgressInfo.class);

		CacheProgressReporter reporter = new CacheProgressReporter();

		reporter.addProgressReporter(reporter1);

		reporter.reportStart();
		reporter.reportProgress(progressInfo);
		reporter.reportSuccess();

		InOrder order1 = inOrder(reporter1);
		order1.verify(reporter1).reportStart();
		order1.verify(reporter1).reportProgress(progressInfo);
		order1.verify(reporter1).reportSuccess();

		// Make sure the calls are also passed to a reporter that comes along later
		ProgressReporter reporter2 = mock(ProgressReporter.class);
		reporter.addProgressReporter(reporter2);

		InOrder order2 = inOrder(reporter2);
		order2.verify(reporter2).reportStart();
		order2.verify(reporter2).reportProgress(progressInfo);
		order2.verify(reporter2).reportSuccess();
	}
}
