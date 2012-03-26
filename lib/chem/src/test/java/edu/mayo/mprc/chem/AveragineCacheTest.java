package edu.mayo.mprc.chem;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class AveragineCacheTest {
	private static PeriodicTable pt = PeriodicTableFactory.getTestPeriodicTable();

	@Test
	public void shouldProvideCorrectDistributions() {
		final AveragineCache cache = new AveragineCache(10, pt);
		final IsotopicDistribution d1 = cache.getDistribution(1000, 2);
		final IsotopicDistribution d2 = cache.getDistribution(1001, 2);
		Assert.assertEquals(d1, d2, "Isotopic distribution for 1000 and 1001 should be the same for precision 10");
		final IsotopicDistribution d3 = cache.getDistribution(1010, 2);
		Assert.assertNotSame(d1, d3, "Isotopic distribution for 1000 and 1010 should be different for precision 10");

		Assert.assertEquals(d1.getMostAbundantIsotope(), 0, "Most abundant isotope for 1000 Da is the first one");
		final IsotopicDistribution d4 = cache.getDistribution(1785, 2);
		Assert.assertEquals(d4.getMostAbundantIsotope(), 0, "Most abundant isotope for 1785 Da is the first one");
		final IsotopicDistribution d5 = cache.getDistribution(1900, 2);
		Assert.assertEquals(d5.getMostAbundantIsotope(), 1, "Most abundant isotope for 1900 Da is the second one");
	}
}
