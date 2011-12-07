package edu.mayo.mprc.dbcurator.model;

import edu.mayo.mprc.dbcurator.CurationExecutorTest;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.utilities.FileUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;


public final class SourceDatabaseArchiveTest {

	/**
	 * This test has trouble when the database already exists. Make sure it can run by setting an unique test name?
	 *
	 * @throws IOException
	 */
	@Test(enabled = true)
	public void testDownload() throws IOException {
		File curatorArchiveFolder = null;
		CurationDao curationDao = CurationExecutorTest.getTestCurationDao();
		URL testURL = new URL("ftp://node029.mprc.mayo.edu/pub/ShortTest.fasta.gz");
		Assert.assertNotNull(curationDao);

		curationDao.begin();

		curatorArchiveFolder = FileUtilities.createTempFolder();

		SourceDatabaseArchive archive = new SourceDatabaseArchive();
		archive.createArchive(testURL.toString(), null, curatorArchiveFolder, curationDao);
		curationDao.commit();

		FileUtilities.cleanupTempFile(curatorArchiveFolder);
	}
}
