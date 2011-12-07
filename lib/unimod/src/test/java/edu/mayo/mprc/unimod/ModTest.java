package edu.mayo.mprc.unimod;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class ModTest {

	@Test
	public void shouldParseMascot1() {
		testParse("Phospho (Protein N-term A)", "Phospho", "Protein", "N-term", "A");
	}

	@Test
	public void shouldParseMascot2() {
		testParse("Phospho (A)", "Phospho", "", "", "A");
	}

	@Test
	public void shouldParseMascot3() {
		testParse("Modification B (weird one) (A)", "Modification B (weird one)", "", "", "A");
	}

	@Test
	public void shouldParseMascot4() {
		testParse("Modification B Protein (N-term) (C-term)", "Modification B Protein (N-term)", "", "C-term", "");
	}

	@Test
	public void shouldParseMascot5() {
		testParse("Phospho(PST)", "Phospho", "", "", "PST");
	}

	@Test
	public void shouldParseMascot6() {
		testParse("Phospho (Protein C-term)", "Phospho", "Protein", "C-term", "");
	}

	private void testParse(String input, String title, String protein, String term, String aminoAcids) {
		final IndexedModSet.MascotNameParts parts = IndexedModSet.MascotNameParts.parseMascotName(input);
		Assert.assertNotNull(parts);
		Assert.assertEquals(parts.getTitle(), title);
		Assert.assertEquals(parts.getProtein(), protein);
		Assert.assertEquals(parts.getTerm(), term);
		Assert.assertEquals(parts.getAcids(), aminoAcids);
	}

}
