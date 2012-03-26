package edu.mayo.mprc.scaffoldparser;

import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.TestingUtilities;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author Eric Winter
 */
public final class ScaffoldXmlDiffTest {
	private static final Logger LOGGER = Logger.getLogger(ScaffoldXmlDiffTest.class);

	/**
	 * Identical copy is similar
	 */
	@Test
	public void testCopyXML() {
		assertScaffoldSimilar("testInputRAW20070427.xml", "testInputRAW20070427Copy.xml", true);
	}

	/**
	 * When Scaffold files differ in the Date field, version of the file or id assignment, they are still similar
	 */
	@Test
	public void testDifferentDateVersionIdXML() {
		assertScaffoldSimilar("testInputRAW20070427.xml", "testInputRAW20070427MultiDiff.xml", true);
	}

	/**
	 * When Scaffold files differ in floating point precision, still similar
	 */
	@Test
	public void testFloatPrecisionXML() {
		assertScaffoldSimilar("testInputRAW20070427.xml", "testInputRAW20070427FloatDiff.xml", true);
	}

	/**
	 * A difference in floating point that is larger than
	 */
	@Test
	public void testFloatDifferenceTooBigXML() {
		assertScaffoldSimilar("testInputRAW20070427.xml", "testInputRAW20070427Diff.xml", false);
	}

	private void assertScaffoldSimilar(final String file1, final String file2, final boolean shouldBeSimilar) {
		File xmlOne = null;
		File xmlTwo = null;
		try {
			xmlOne = TestingUtilities.getTempFileFromResource(getClass(), "/edu/mayo/mprc/scaffoldparser/" + file1, true, null);
			xmlTwo = TestingUtilities.getTempFileFromResource(getClass(), "/edu/mayo/mprc/scaffoldparser/" + file2, true, null);

			final ScaffoldXmlDiff scaffoldXmlDiff = new ScaffoldXmlDiff();
			final boolean areSimilar = scaffoldXmlDiff.areSimilarScaffoldXMLFiles(xmlOne, xmlTwo);
			if (shouldBeSimilar != areSimilar) {
				LOGGER.error(scaffoldXmlDiff.getDifferenceString());
				Assert.fail("Two Scaffold XML files should be " + (shouldBeSimilar ? "similar" : "different"));
			}
		} catch (Exception e) {
			Assert.fail("Could not compare scaffold files for identity: [" + file1 + "] and [" + file2 + "]", e);
		} finally {
			FileUtilities.cleanupTempFile(xmlOne);
			FileUtilities.cleanupTempFile(xmlTwo);
		}
	}
}
