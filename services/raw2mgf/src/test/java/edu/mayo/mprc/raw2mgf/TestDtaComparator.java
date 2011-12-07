package edu.mayo.mprc.raw2mgf;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public final class TestDtaComparator {

	private static final DtaComparator DTA_COMPARATOR = new DtaComparator();

	@Test
	public void shouldSortProperDtas() {
		assertComparison("test1.10.10.3.dta", "test1.11.11.2.dta", -1);
		assertComparison("test1.11.11.2.dta", "test1.10.10.3.dta", 1);

		assertComparison("test1.10.11.2.dta", "test1.10.12.1.dta", -1);
		assertComparison("test1.10.11.2.dta", "test1.10.11.1.dta", 1);

		assertComparison("test4.10.11.2.dta", "test3.10.11.2.dta", 1);
	}

	@Test
	public void shouldSupportEquality() {
		assertComparison("test3.10.11.2.dta", "test3.10.11.2.dta", 0);
	}

	private void assertComparison(String dta1, String dta2, int result) {
		File f1 = new File(dta1);
		File f2 = new File(dta2);
		Assert.assertEquals(DTA_COMPARATOR.compare(f1, f2), result, dta1 + " compared to " + dta2 + " should yield " + result);
		Assert.assertEquals(DTA_COMPARATOR.compare(f2, f1), -result, dta1 + " compared to " + dta2 + " should yield " + (-result));
	}
}
