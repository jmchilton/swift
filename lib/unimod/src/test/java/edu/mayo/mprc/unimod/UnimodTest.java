package edu.mayo.mprc.unimod;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;
import edu.mayo.mprc.utilities.TestingUtilities;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * @author Eric Winter
 */
@Test(sequential = true)
public final class UnimodTest {
	private static final Logger LOGGER = Logger.getLogger(UnimodTest.class);

	private Unimod umodSet;

	private static Unimod defaultUnimod;

	/**
	 * @deprecated Let the user configure the unimod file location instead of relying on the embedded one.
	 */
	public static Unimod getDefaultUnimodSet() {
		synchronized (Unimod.class) {
			if (defaultUnimod != null) {
				return defaultUnimod;
			}
			defaultUnimod = new Unimod();
			try {
				defaultUnimod.parseUnimodXML(ResourceUtilities.getStream("classpath:edu/mayo/mprc/unimod/unimod.xml", Unimod.class));
				return defaultUnimod;
			} catch (Exception t) {
				throw new MprcException("Unable to parse default unimod set", t);
			}
		}
	}

	/**
	 *
	 */
	@Test(enabled = true)
	public void testUnimodParsing() throws IOException, SAXException {
		InputStream umodStream = ResourceUtilities.getStream("classpath:testUniMod.xml", this);
		umodSet = new Unimod();
		umodSet.parseUnimodXML(umodStream);
		FileUtilities.closeQuietly(umodStream);
	}

	@Test(dependsOnMethods = {"testUnimodParsing"}, enabled = true)
	// ToDO:
	public void testGetAlternativeNames() {


		Set<String> titleSet = umodSet.getAlternativeNames("Dehydrated");

		Assert.assertTrue(titleSet.contains("didehydroalanine"));
		Assert.assertTrue(titleSet.contains("C-terminal imide"));
		Assert.assertTrue(titleSet.contains("Prompt loss of phosphate from phosphorylated residue"));
		Assert.assertTrue(titleSet.contains("D-Succinimide"));

		Set<String> set1 = umodSet.getAlternativeNames("C-terminal imide");

		Assert.assertEquals(titleSet.size(), set1.size(), "Sets have different sizes");
	}

	@Test(dependsOnMethods = {"testUnimodParsing"}, enabled = true)
	// ToDO:
	public void testGetByName() {
		Mod mod = umodSet.getByTitle("Dehydrated");

		Assert.assertEquals("Dehydration", mod.getFullName());

		Mod modFromAlias = umodSet.getByTitle("didehydroalanine");

		Assert.assertEquals(mod, modFromAlias, "We should have the same object");
	}

	@Test(dependsOnMethods = {"testUnimodParsing"}, enabled = true)
	// ToDO:
	public void testGetAllTitles() {
		Set<String> titles = umodSet.getAllTitles();
		Assert.assertEquals(titles.size(), 532, "We should have gotten 532 titles back");
	}

	@Test(dependsOnMethods = {"testUnimodParsing"}, enabled = true)
	// ToDO:
	public void testFindMatchingMods() {

		//Set<Modification> findMatchingMods(Double minMass, Double maxMass, String site, String position, Boolean hidden)

		Set<ModSpecificity> result1 = umodSet.findMatchingModSpecificities(-18.010566d, -18.010564d, null, null, null, null);

		Assert.assertEquals(result1.size(), 8);

		Set<ModSpecificity> result2 = umodSet.findMatchingModSpecificities(null, null, null, Terminus.Nterm, null, false);

		Assert.assertEquals(result2.size(), 19);

	}

	@Test
	public void testGetDefaultSet() {
		final Unimod unimod = UnimodTest.getDefaultUnimodSet();
		final Map<String, Double> massMap = unimod.getFullNameToMonoisotopicMassMap();
		Assert.assertEquals(massMap.size(), 1276, "Wrong amount of unimod modifications");
	}

	/**
	 * Unimod parse should match precisely the expected report.
	 */
	@Test
	public void shouldMatchExpectedParse() {
		final Unimod unimod = UnimodTest.getDefaultUnimodSet();
		Assert.assertEquals(TestingUtilities.compareStringToResourceByLine(unimod.report(), "edu/mayo/mprc/unimod/unimod_report.txt"), null, "Unimod does not match the expected result");
	}

	/**
	 * When cleaning up comments, newlines are removed as well as excessive tabs and spaces.
	 */
	@Test
	public void shouldCleanupComments() {
		Assert.assertEquals(IndexedModSet.cleanWhitespace("a\nb\r\nc"), "a b c");
		Assert.assertEquals(IndexedModSet.cleanWhitespace("a          b"), "a b");
	}
}
