package edu.mayo.mprc.searchdb;

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
	private MockUnimodDao unimodDao;
	private IndexedModSet unimod;
	private ScaffoldModificationFormat format;

	@BeforeClass
	public void setup() {
		unimodDao = new MockUnimodDao();
		unimod = unimodDao.load();
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
		final List<LocalizedModification> mods = format.parseModifications("EDEEESLNEVGYDDIGGCR", "c18: Carbamidomethyl (+57.02)", "");
		Assert.assertEquals(mods.size(), 1, "one mod");
		final LocalizedModification localizedModification = mods.get(0);
		Assert.assertEquals(localizedModification.getModification().getTitle(), "Carbamidomethyl");
	}
}
