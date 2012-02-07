package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import org.testng.annotations.Test;

public final class TestSearchDbWorker {

    @Test
    public static void shouldStore() {
        SearchDbWorker worker = new SearchDbWorker(null);
        SearchDbWorkPacket workPacket = new SearchDbWorkPacket("task0", false, 0, null);

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
