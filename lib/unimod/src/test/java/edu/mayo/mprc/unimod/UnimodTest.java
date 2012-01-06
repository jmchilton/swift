package edu.mayo.mprc.unimod;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;
import edu.mayo.mprc.utilities.TestingUtilities;
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
	 * Make sure the unimod file parses properly and matches expected values.
	 */
	@Test(enabled = true)
	public void testUnimodParsing() throws IOException, SAXException {
		InputStream umodStream = ResourceUtilities.getStream("classpath:testUniMod.xml", this);
		umodSet = new Unimod();
		umodSet.parseUnimodXML(umodStream);
		FileUtilities.closeQuietly(umodStream);
		checkUnimodMatches(umodSet, "/expectedTestUniMod.txt");
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
		final Unimod unimod = getDefaultUnimodSet();
		final Map<String, Double> massMap = unimod.getFullNameToMonoisotopicMassMap();
		Assert.assertEquals(massMap.size(), 1276, "Wrong amount of unimod modifications");
	}

	@Test
	void shouldLoadScaffoldUnimod() throws IOException, SAXException {
		final Unimod unimod = new Unimod();
		unimod.parseUnimodXML(ResourceUtilities.getStream("classpath:edu/mayo/mprc/unimod/scaffold_unimod.xml", UnimodTest.class));
		Assert.assertEquals(unimod.getMajorVersion(), "1");
		Assert.assertEquals(unimod.getAllTitles().size(), 362);
		Assert.assertEquals(unimod.getAllSpecificities(true).size(), 591);
		checkUnimodMatches(unimod, "/edu/mayo/mprc/unimod/expected_scaffold_unimod.txt");
	}

	@Test
	void shouldConvertUnimod1Composition() {
		Assert.assertEquals(Unimod1Handler.convertComposition("H(-1) H2(3) C(2) O"), "H(-1) 2H(3) C(2) O");
		Assert.assertEquals(Unimod1Handler.convertComposition("H(-1) N(-1) O18"), "H(-1) N(-1) 18O");
		Assert.assertEquals(Unimod1Handler.convertComposition("H(24) C(19) N(8) O(15) P(2) S(3) Cu Mo"), "H(24) C(19) N(8) O(15) P(2) S(3) Cu Mo");
		Assert.assertEquals(Unimod1Handler.convertComposition(""), "");
	}

	private void checkUnimodMatches(Unimod unimod, String expectedDumpResource) throws IOException {
		String dump = unimod.debugDump();
		final String expected = Resources.toString(Resources.getResource(UnimodTest.class, expectedDumpResource), Charsets.ISO_8859_1);
		Assert.assertEquals(TestingUtilities.compareStringsByLine(dump, expected, true), null, "The unimod parse does not match");
	}
}
