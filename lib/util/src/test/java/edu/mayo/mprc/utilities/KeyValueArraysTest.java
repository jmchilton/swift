package edu.mayo.mprc.utilities;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

public final class KeyValueArraysTest {

	@Test
	public void shouldGetIndexSimpleCase() {
		final float[] kv = new float[]{
				0, 0,
				1, 1,
				2, 2,
				3, 3};
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, -1), -1, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 0), 0, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 0.5f), 0, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 1), 1, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 1.5f), 1, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 2), 2, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 2.5f), 2, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 3), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 3.5f), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 4), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 4.5f), 3, "");
	}

	@Test
	public void shouldGetIndexConstantArray() {
		final float[] kv = new float[]{
				0, 0,
				0, 1,
				0, 2,
				0, 3};
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, -1), -1, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 0), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 0.5f), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 1), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 1.5f), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 2), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 2.5f), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 3), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 3.5f), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 4), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 4.5f), 3, "");
	}

	@Test
	public void shouldGetIndexMultipleSameValues() {
		final float[] kv = new float[]{
				0, 0,
				1, 1,
				1, 2,
				2, 3};
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, -1), -1, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 0), 0, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 0.5f), 0, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 1), 2, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 1.5f), 2, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 2), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 2.5f), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 3), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 3.5f), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 4), 3, "");
		Assert.assertEquals(KeyValueArrays.getIndexForKey(kv, 4.5f), 3, "");
	}

	@Test
	public void shouldInterpolate() {
		Assert.assertEquals(KeyValueArrays.interpolate(1f, 2f, 1.5f, 3f, 4f), 3.5f);
		Assert.assertEquals(KeyValueArrays.interpolate(1.0, 2.0, 1.5, 3.0, 4.0), 3.5);

		final float[] kv = new float[]{
				0, 0,
				1, 1,
				1, 2,
				2, 3};
		Assert.assertEquals(KeyValueArrays.getInterpolatedValue(kv, -1), 0f);
		Assert.assertEquals(KeyValueArrays.getInterpolatedValue(kv, 0), 0f);
		Assert.assertEquals(KeyValueArrays.getInterpolatedValue(kv, 0.5f), 0.5f);
		Assert.assertEquals(KeyValueArrays.getInterpolatedValue(kv, 1), 2f);
		Assert.assertEquals(KeyValueArrays.getInterpolatedValue(kv, 1.5f), 2.5f);
		Assert.assertEquals(KeyValueArrays.getInterpolatedValue(kv, 2), 3f);
		Assert.assertEquals(KeyValueArrays.getInterpolatedValue(kv, 2.1f), 0f);
	}

	@Test
	public void shouldFilterKeyDomain() {
		Assert.assertTrue(Arrays.equals(KeyValueArrays.filterKeyDomain(new float[]{1, 10, 2, 11, 3, 12, 4, 13, 5, 14}, 3, 4), new float[]{3, 12, 4, 13}));
		Assert.assertTrue(Arrays.equals(KeyValueArrays.filterKeyDomain(new float[]{1, 2, 3, 4}, 3, 3), new float[]{3, 4}));
		Assert.assertTrue(Arrays.equals(KeyValueArrays.filterKeyDomain(new float[]{1, 2, 3, 4}, 4, 3), new float[]{}));
		Assert.assertTrue(Arrays.equals(KeyValueArrays.filterKeyDomain(new float[]{}, 0, 5), new float[]{}));
	}

	@Test
	public void shouldGetMaximumKeyInRange() {
		Assert.assertEquals(KeyValueArrays.getMaximumValueInKeyRange(new float[]{1, 10, 2, 11, 3, 12, 4, 13, 5, 14}, 3, 4),
				13.0f);
		Assert.assertEquals(KeyValueArrays.getMaximumValueInKeyRange(new float[]{1, 10, 2, 11, 3, 12, 4, 13, 5, 14}, 3, 3.5f),
				12.0f);
		Assert.assertEquals(KeyValueArrays.getMaximumValueInKeyRange(new float[]{1, 10, 2, 11, 3, 12, 4, 13, 5, 14}, 4, 3.5f),
				Float.MIN_VALUE);
		Assert.assertEquals(KeyValueArrays.getMaximumValueInKeyRange(new float[]{1, 10, 2, 11, 3, 12, 4, 13, 5, 14}, 0, 5),
				14.0f);
	}

}
