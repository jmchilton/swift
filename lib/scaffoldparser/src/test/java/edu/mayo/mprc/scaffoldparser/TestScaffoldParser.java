package edu.mayo.mprc.scaffoldparser;

import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;
import edu.mayo.mprc.utilities.TestingUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class TestScaffoldParser {
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(TestScaffoldParser.class);

	@Test
	public void testSpectrumParser() {
		Assert.assertEquals(SpectrumAnalysisIdentification.getSpectrumNumber("test1 scan 92 92 (test1.92.92.2.dta)"),
				92, "The spectrum number is parsed incorrectly");

		Assert.assertEquals(SpectrumAnalysisIdentification.getSpectrumNumber("test1 scan 30 32 (test1.30.32.3.dta)"),
				30, "The spectrum number is parsed incorrectly");

		Assert.assertEquals(SpectrumAnalysisIdentification.getSpectrumName("test1 scan 92 92 (test123.92.92.2.dta)"),
				"test123", "The spectrum number is parsed incorrectly");

		Assert.assertEquals(SpectrumAnalysisIdentification.getSpectrumName("test1 scan 30 32 (test.hello.world.30.32.3.dta)"),
				"test.hello.world", "The spectrum number is parsed incorrectly");

	}

	/**
	 * Load Scaffold .xml file, save it, check that it matches the original.
	 *
	 * @throws IOException  When load fails
	 * @throws SAXException When scaffold parse fails
	 */
	@Test
	public void testRoundtrip() throws IOException, SAXException {
		final Scaffold scaffold = ScaffoldParser.loadScaffoldXml(ResourceUtilities.getStream("classpath:test.xml", TestScaffoldParser.class));
		File expected = TestingUtilities.getTempFileFromResource("/test.xml", false, null);

		final File output = TestingUtilities.getUniqueTempFile(true, null, ".xml");
		try {
			final FileOutputStream outputStream = FileUtilities.getOutputStream(output);
			ScaffoldParser.saveScaffoldXml(scaffold, outputStream);
			outputStream.close();
			ScaffoldXmlDiff diff = new ScaffoldXmlDiff();
			final boolean similar = diff.areSimilarScaffoldXMLFiles(output, expected);
			if (!similar) {
				LOGGER.error("Scaffold files different: " + diff.getDifferenceString());
				Assert.fail();
			}
		} finally {
			FileUtilities.cleanupTempFile(output);
			FileUtilities.cleanupTempFile(expected);
		}
	}

	@Test
	public void testSequenceStripping() {
		Assert.assertEquals("GDDITMVLILPKPEK", PeptideAnalysisIdentification.stripNeighborAminoAcids("(K)GDDITMVLILPKPEK(S)"), "The end amino acids are not stripped properly");
	}

	@Test
	public void testScaffoldParserDrivers() throws IOException {
		File tempFolder = FileUtilities.createTempFolder();

		try {
//			File scaffoldXmlFile = TestingUtilities.getTempFileFromResource(scaffoldXmlResource, false, tempFolder);
//
//			Scaffold scaffold = null;
//
//			LOGGER.info("Parsing scaffold xml using dom driver.");
//			LOGGER.info("Running GC.");
//			Runtime.getRuntime().gc();
//			LOGGER.info("JVM used memory before parsing: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//			scaffold = ScaffoldParser.loadScaffoldXml(new FileInputStream(scaffoldXmlFile), new DomDriver());
//			LOGGER.info("JVM used memory after parsing: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//
//			scaffold = null;
//
//			LOGGER.info("Parsing scaffold xml using xpp driver.");
//			LOGGER.info("Running GC.");
//			Runtime.getRuntime().gc();
//			LOGGER.info("JVM used memory before parsing: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//			scaffold = ScaffoldParser.loadScaffoldXml(new FileInputStream(scaffoldXmlFile));
//			LOGGER.info("JVM used memory after parsing: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//
//			scaffold = null;
//
//			LOGGER.info("Parsing scaffold xml using stax driver.");
//			LOGGER.info("Running GC.");
//			Runtime.getRuntime().gc();
//			LOGGER.info("JVM used memory before parsing: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//			scaffold = ScaffoldParser.loadScaffoldXml(new FileInputStream(scaffoldXmlFile), new StaxDriver());
//			LOGGER.info("JVM used memory after parsing: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		} finally {
			FileUtilities.cleanupTempFile(tempFolder);
		}
	}
}
