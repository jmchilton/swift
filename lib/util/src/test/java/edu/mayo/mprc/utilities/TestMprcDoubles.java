package edu.mayo.mprc.utilities;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Check that our double methods work fine.
 *
 * @author Roman Zenka
 */
public class TestMprcDoubles {
	@Test
	public void shouldBeWithinProperRanges() {
		Assert.assertTrue(MprcDoubles.within(1.0, 1.1, 0.2));
		Assert.assertTrue(MprcDoubles.within(-10, -11, 1));
		Assert.assertFalse(MprcDoubles.within(-10, -11, 0.9));
	}

	@Test
	public void shouldHandleNaNProperly() {
		Assert.assertFalse(MprcDoubles.within(Double.NaN, 1.1, 0.2));
		Assert.assertFalse(MprcDoubles.within(-10, Double.NaN, 1));
		Assert.assertTrue(MprcDoubles.within(Double.NaN, Double.NaN, 1E-10), "NaN is within range of another NaN - we use this to relax equality");
	}

}
