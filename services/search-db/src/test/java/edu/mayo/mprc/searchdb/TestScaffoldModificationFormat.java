package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.searchdb.dao.LocalizedModification;
import edu.mayo.mprc.unimod.IndexedModSet;
import edu.mayo.mprc.unimod.MockUnimodDao;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Check that we can correctly parse typical mods from Scaffold reports.
 *
 * @author Roman Zenka
 */
public final class TestScaffoldModificationFormat {
	private ScaffoldModificationFormat format;
	private static final double EPSILON = 0.05;

	@BeforeClass
	public void setup() {
		MockUnimodDao unimodDao = new MockUnimodDao();
		IndexedModSet unimod = unimodDao.load();
		format = new ScaffoldModificationFormat(unimod);
	}

	/**
	 * No mods specified - no mods returned
	 */
	@Test
	public void shouldParseEmptyString() {
		final List<LocalizedModification> mods = format.parseModifications("EDEEESLNEVGYDDIGGCR", "", "");
		Assert.assertEquals(mods.size(), 0, "No mods");
	}

	/**
	 * Single mods parsing, no trouble with ordering.
	 */
	@Test
	public void shouldParseSingleMods() {
		checkSingleMod(
				format.parseModifications("EDEEESLNEVGYDDIGGCR", "c18: Carbamidomethyl (+57.02)", ""),
				"Carbamidomethyl", 57.02, 17, 'C');

		checkSingleMod(
				format.parseModifications("MFLSFPTTK", "m1: Oxidation (+15.99)", ""),
				"Oxidation", 15.99, 0, 'M');

		checkSingleMod(
				format.parseModifications("NmQDMVEDYR", "m2: Oxidation (+15.99)", ""),
				"Oxidation", 15.99, 1, 'M');

		checkSingleMod(
				format.parseModifications("IQVRLGEHNIDVLEGNEQFINAAKIITHPNFNGNTLDNDImLIKLSSPATLNSR", "m41: Oxidation (+15.99)", ""),
				"Oxidation", 15.99, 40, 'M');
	}

	/**
	 * Should notice that the residue reported does not match the sequence.
	 */
	@Test(expectedExceptions = MprcException.class)
	public void shouldCatchResidueMismatch() {
		format.parseModifications("EDEEESLNEVGYDDIGGZR", "c18: Carbamidomethyl (+57.02)", "");
	}

	/**
	 * Should notice non-existing modification, even if the mass matches
	 */
	@Test(expectedExceptions = MprcException.class)
	public void shouldCatchNonExistingMod() {
		format.parseModifications("EDEEESLNEVGYDDIGGCR", "c18: Blablamidomethyl (+57.02)", "");
	}

	/**
	 * Should notice extra garbage in the parsed string
	 */
	@Test
	public void shouldCatchGarbage() {
		try {
			format.parseModifications("EDEEESLNEVGYDDIGGCR", "c18: Carbamidomethyl (+57.02), GARBAGE!", "");
			Assert.fail("Expected exception");
		} catch (MprcException e) {
			Assert.assertTrue(e.getMessage().contains("[GARBAGE!]"), "The garbage should be properly reported");
		}

		try {
			format.parseModifications("EDEEESLNEVGYDDIGGCR", "GARBAGE2!", "");
			Assert.fail("Expected exception");
		} catch (MprcException e) {
			Assert.assertTrue(e.getMessage().contains("[GARBAGE2!]"), "The garbage should be properly reported");
		}
	}

	private void checkSingleMod(List<LocalizedModification> mods, String name, double expectedMass, int position, char residue) {
		Assert.assertEquals(mods.size(), 1, "one mod expected");
		final LocalizedModification localizedModification = mods.get(0);
		Assert.assertEquals(localizedModification.getModSpecificity().getModification().getTitle(), name);
		assertNear(localizedModification.getModSpecificity().getModification().getMassMono(), expectedMass, "Mass should match");
		Assert.assertEquals(localizedModification.getPosition(), position, "Mod position should match");
		Assert.assertEquals(localizedModification.getResidue(), residue, "Mod residue should match");
	}


	public static void assertNear(double d1, double d2, String message) {
		final double delta = d1 - d2;
		Assert.assertTrue(-EPSILON < delta && delta < EPSILON, message);
	}
}
