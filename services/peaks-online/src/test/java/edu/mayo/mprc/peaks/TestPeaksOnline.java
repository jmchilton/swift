package edu.mayo.mprc.peaks;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.peaks.core.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public final class TestPeaksOnline {

	private static final Object monitor = new Object();

	private static PeaksURIs peaksOnlineURIs;
	private Peaks peaksOnline;
	private String databaseId;
	private PeaksSearchStatusEvent event;

	static {
		try {
			peaksOnlineURIs = new PeaksURIs(new URI("http://peaks.mayo.edu:8080/peaksonline"));
		} catch (URISyntaxException e) {
			throw new MprcException("Error creating PeaksURIs object.");
		}
	}

	@Test(enabled = false)
	public void TestInstanciatePeaksOnlineClient() throws IOException {
		peaksOnline = new Peaks(peaksOnlineURIs, "admin", "peaksonline");
	}

//	public void submitPeaksOnlineSearch() throws IOException {
//		PeaksSearch peaksOnlineSearch = peaksOnline.getPeaksOnlineSearch();
//		peaksOnlineSearch.submitSearch(new PeaksSearchParameters());
//	}

	@Test(enabled = false, dependsOnMethods = {"TestInstanciatePeaksOnlineClient"})
	public void waitForPeaksOnlineSearchCompletion() throws IOException, InterruptedException {
		synchronized (monitor) {
			final String searchId = "7879555498205498641";

			final PeaksSearchMonitor peaksOnlineSearchMonitor = new PeaksSearchMonitor(searchId, peaksOnline.getPeaksOnlineResult(0));

			peaksOnlineSearchMonitor.addPeaksOnlineMonitorListener(new PeaksMonitorListener() {

				public void searchCompleted(final PeaksSearchStatusEvent event) {
					synchronized (monitor) {
						TestPeaksOnline.this.event = event;
						peaksOnlineSearchMonitor.stop();
						monitor.notify();
					}
				}

				public void searchRunning(final PeaksSearchStatusEvent event) {
					TestPeaksOnline.this.event = event;
				}

				public void searchWaiting(final PeaksSearchStatusEvent event) {
					TestPeaksOnline.this.event = event;
				}

				public void searchNotFound(final PeaksSearchStatusEvent event) {
					synchronized (monitor) {
						TestPeaksOnline.this.event = event;

						peaksOnlineSearchMonitor.stop();
						monitor.notify();
					}
				}
			});

			peaksOnlineSearchMonitor.start(5000);

			monitor.wait();

			Assert.assertTrue(event.getSearchId().equals(searchId), "Search monitor could not find seach.");
			Assert.assertTrue(event.getStatus().equals(PeaksResult.SEARCH_COMPLETED_STATUS), "Search monitor could not find seach.");
		}
	}

	@Test(enabled = false, dependsOnMethods = {"TestInstanciatePeaksOnlineClient"})
	public void addDatabaseToPeaksOnline() throws IOException, InterruptedException {

		final PeaksAdmin peaksOnlineAdmin = peaksOnline.getPeaksOnlineAdmin();

		final String databaseName = Long.toString(System.currentTimeMillis());
		peaksOnlineAdmin.addDatabase(databaseName, "/mnt/raid1/test/yeast17080912A.fasta", PeaksAdmin.SWISSPROT_DB_FORMAT, true);

		databaseId = null;

		for (final PeaksDatabase peaksOnlineDatabase : peaksOnlineAdmin.getAllDatabases()) {
			if (peaksOnlineDatabase.getDatabaseName().equals(databaseName)) {
				databaseId = peaksOnlineDatabase.getDatabaseId();
			}
		}

		Assert.assertNotNull(databaseId, "Faile to create Peaks database.");
	}

	@Test(enabled = false, dependsOnMethods = {"addDatabaseToPeaksOnline"})
	public void removeDatabaseFromPeaksOnline() throws IOException, InterruptedException {

		final PeaksAdmin peaksOnlineAdmin = peaksOnline.getPeaksOnlineAdmin();

		Assert.assertTrue(peaksOnlineAdmin.removeDatabase(databaseId), "Database could not be deleted.");
	}
}
