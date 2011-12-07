package edu.mayo.mprc.utilities;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Eric Winter
 */
public final class ScaffoldXmlComparerTest {
	/**
	 * tests two xml files that have the data changed only so this should be ignored
	 */
	@Test(groups = {"fast", "unit"})
	// ToDO:
	public void testDifferentDateXML() throws IOException {
		File xmlOne = TestingUtilities.getTempFileFromResource(this.getClass(), "/edu/mayo/mprc/utilities/testing/xmlDifference/testInputRAW20070427.xml", true, null);
		File xmlTwo = TestingUtilities.getTempFileFromResource(this.getClass(), "/edu/mayo/mprc/utilities/testing/xmlDifference/testInputRAW20070427DateDiff.xml", true, null);

		Assert.assertTrue(new ScaffoldXmlComparer().areSimilarScaffoldXMLFiles(xmlOne, xmlTwo));
	}
}
