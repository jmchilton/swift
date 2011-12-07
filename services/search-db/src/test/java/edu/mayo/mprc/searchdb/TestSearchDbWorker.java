package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.searchdb.dao.MockSearchDbDao;
import edu.mayo.mprc.searchdb.dao.SearchDbDao;
import org.testng.annotations.Test;

public final class TestSearchDbWorker {

	@Test
	public static void shouldStore() {
		final SearchDbDao dbDao = new MockSearchDbDao();

		SearchDbWorker worker = new SearchDbWorker(dbDao);
		SearchDbWorkPacket workPacket = new SearchDbWorkPacket(0, "test", false);

		worker.processRequest(workPacket, new ProgressReporter() {
			@Override
			public void reportStart() {
			}

			@Override
			public void reportProgress(ProgressInfo progressInfo) {
			}

			@Override
			public void reportSuccess() {
			}

			@Override
			public void reportFailure(Throwable t) {
			}
		});
	}
}
