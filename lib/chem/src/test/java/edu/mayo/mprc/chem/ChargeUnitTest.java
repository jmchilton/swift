package edu.mayo.mprc.chem;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class ChargeUnitTest {
	private static double PRECISION = 1E-7;

	@Test
	public void shouldCalculateCorrectMass() {
		PeriodicTable pt = PeriodicTableFactory.getTestPeriodicTable();
		ChargeUnit cu = ChargeUnit.getProtonChargeUnit(pt);
		Assert.assertEquals(cu.neutralToCharged(0, 1), pt.getProtonMass(), PRECISION, "The charge must add mass of one proton (with " + PRECISION + " Da precision)");
		Element carbon = pt.getElementBySymbol("C");
		Assert.assertEquals(cu.chargedToNeutral(
				(carbon.getMonoisotopicMass() + pt.getProtonMass() * 3) / 3, 3),
				carbon.getMonoisotopicMass(), PRECISION, "Triply charged carbon mass should convert to proper neutral mass (with " + PRECISION + " Da precision)");
	}

	@Test
	public void shouldSupportToString() {
		PeriodicTable pt = PeriodicTableFactory.getTestPeriodicTable();
		ChargeUnit cu = ChargeUnit.getProtonChargeUnit(pt);

		Assert.assertEquals(cu.toString(1), "[M+1H+]1+", "toString failure");
		Assert.assertEquals(cu.toString(-1), "[M-1H+]1-", "toString failure");
		Assert.assertEquals(cu.toString(0), "[M+0H+]0+", "toString failure");

		Assert.assertEquals(cu.toShortString(1), "[M 1+]", "toShortString failure");
		Assert.assertEquals(cu.toShortString(-51), "[M 51-]", "toShortString failure");

		Assert.assertEquals(cu.getChargeCarrier().getSymbol(), "H", "Hydrogen must be carrying the H+ charge");
	}

	@Test
	public void shouldSupportDifferentCarriers() {
		PeriodicTable pt = PeriodicTableFactory.getTestPeriodicTable();
		Element e = pt.getElementBySymbol("He");
		ChargeUnit cu = ChargeUnit.getChargeUnit(e, pt);

		Assert.assertEquals(cu.toShortString(1), "[M 1He+]", "toShortString failure");
		Assert.assertEquals(cu.toShortString(-51), "[M 51He-]", "toShortString failure");
	}
}
