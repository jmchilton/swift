package edu.mayo.mprc.chem;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class AminoAcidsTest {
	@Test
	public void testAminoAcidMasses() {
		AminoAcidSet set = AminoAcidSet.DEFAULT;
		Assert.assertEquals(89.047679, set.getMonoisotopicMass("A"), "The mass of the amino acid does not match");
		final AminoAcid gly = set.getForSingleLetterCode("G");
		Chemical glycine = new Chemical(gly.getFormula() + " H2 O1", PeriodicTableFactory.getTestPeriodicTable());
		Assert.assertEquals("Gly", gly.getCode3());
		Assert.assertEquals(set.getMonoisotopicMass(String.valueOf(gly.getCode())), 75.032029);
		// When we calculate the mass from the equation, we get better results
		Assert.assertEquals(glycine.getMonoisotopicMass(), 75.03202840990001);

		Assert.assertEquals(set.getMonoisotopicMass("GDDITMVLILPKPEK"), 1667.916795);
		Assert.assertEquals(set.getMonoisotopicMass("GASPVTCLINDKQEMHFRYW"), 2394.124905);

	}
}
