package edu.mayo.mprc.raw2mgf;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class TestDtaName {

	@Test
	public void shouldParseCommonDtaName() {
		final DtaName dtaName = new DtaName("file.15.16.2.dta");
		Assert.assertTrue(dtaName.matches());
		Assert.assertEquals(dtaName.getSearchName(), "file");
		Assert.assertEquals(dtaName.getFirstScan(), "15");
		Assert.assertEquals(dtaName.getSecondScan(), "16");
		Assert.assertEquals(dtaName.getCharge(), "2");
		Assert.assertEquals(dtaName.getExtras(), null);
	}

	@Test
	public void shouldParseDtaNameWithExtras() {
		final DtaName dtaName = new DtaName(".123.1023.1050.2.4.dta");
		Assert.assertTrue(dtaName.matches());
		Assert.assertEquals(dtaName.getSearchName(), ".123");
		Assert.assertEquals(dtaName.getFirstScan(), "1023");
		Assert.assertEquals(dtaName.getSecondScan(), "1050");
		Assert.assertEquals(dtaName.getCharge(), "2");
		Assert.assertEquals(dtaName.getExtras(), "4");
	}

	@Test
	public void shouldRejectMalformedName() {
		final DtaName dtaName = new DtaName("test.dta");
		Assert.assertFalse(dtaName.matches());
	}
}
