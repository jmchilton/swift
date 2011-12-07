package edu.mayo.mprc.utilities;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

@Test(sequential = true)
public final class TestingUtilitiesTest {
	private static final Logger LOGGER = Logger.getLogger(TestingUtilitiesTest.class);

	@Test(groups = {"fast"})
	public void test_getTempFileFromResource() {
		try {
			File tempFile = TestingUtilities.getTempFileFromResource(this.getClass(), "/SimpleFile.txt", true, null);
			Assert.assertTrue(tempFile.exists(), "The file wasn't created.");
			if (tempFile.exists()) {
				LOGGER.debug("Temp file created: " + tempFile.getAbsolutePath());
			}
		} catch (IOException e) {
			Assert.fail("Could not create a temporary file from a local resource.", e);
		}
	}

	@Test(groups = {"fast", "unit"})
	public void FileComparisonTest() throws IOException {
		File f1 = TestingUtilities.getTempFileFromResource(this.getClass(), "/edu/mayo/mprc/utilities/testing/simpleFileA.txt", true, null);
		File f2 = TestingUtilities.getTempFileFromResource(this.getClass(), "/edu/mayo/mprc/utilities/testing/simpleFileACopy.txt", true, null);
		LOGGER.debug(f2.getAbsolutePath());
		Assert.assertTrue(f2.exists());
		Assert.assertEquals(TestingUtilities.compareFilesByLine(f1, f2), null);
	}

	@Test(groups = {"fast", "unit"})
	public void DifferentFileComparisonTest() throws IOException {
		File f1 = TestingUtilities.getTempFileFromResource(this.getClass(), "/edu/mayo/mprc/utilities/testing/simpleFileA.txt", true, null);
		File f2 = TestingUtilities.getTempFileFromResource(this.getClass(), "/edu/mayo/mprc/utilities/testing/simpleFileB.txt", true, null);
		Assert.assertEquals(TestingUtilities.compareFilesByLine(f1, f2), "First file line and second file line differences:\n" +
				"[A simple file to compare.]\n" +
				"[A second simple file to compare]");
	}


}
