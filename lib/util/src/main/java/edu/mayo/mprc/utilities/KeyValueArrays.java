package edu.mayo.mprc.utilities;

public final class KeyValueArrays {

	private KeyValueArrays() {
	}

	/**
	 * Return the index of the last key-value pair whose key is <= given key.
	 * Modified java's Array.binarySearch version.
	 */
	public static int getIndexForKey(float[] keyValueArray, float key) {
		int low = 0;
		int high = keyValueArray.length / 2 - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			float midVal = keyValueArray[mid * 2];
			if (midVal <= key) {
				low = mid + 1;
			} else {
				high = mid - 1;
			}
		}
		return low - 1;
	}

	/**
	 * @param keyValueArray Array of key-value pairs.
	 * @param key           Key to obtain value for.
	 * @return Linearly interpolated value for given key or zero if out of boundaries.
	 */
	public static float getInterpolatedValue(final float[] keyValueArray, final float key) {
		final int index = getIndexForKey(keyValueArray, key);
		final int numValues = keyValueArray.length / 2;
		if (index < 0) {
			return 0f;
		}
		if (index < numValues - 1) {
			return interpolate(keyValueArray[index * 2], keyValueArray[index * 2 + 2], key, keyValueArray[index * 2 + 1], keyValueArray[index * 2 + 3]);
		} else {
			if (index == numValues - 1 && key <= keyValueArray[index * 2]) { // The test is actually for equality
				return keyValueArray[index * 2 + 1];
			} else {
				return 0f;
			}
		}
	}

	public static double interpolate(double key1, double key2, double key, double value1, double value2) {
		double t = (key - key1) / (key2 - key1);
		return value1 * (1 - t) + value2 * t;
	}

	public static float interpolate(float key1, float key2, float key, float value1, float value2) {
		float t = (key - key1) / (key2 - key1);
		return value1 * (1 - t) + value2 * t;
	}

	/**
	 * @param keyValueArray The array to filter
	 * @param minKey        Minimum key to retain (inclusive)
	 * @param maxKey        Maximum key to retain (inclusive)
	 * @return Filtered array with only those key-value pairs where the key is in &lt;minKey, maxKey&gt; interval.
	 */
	public static float[] filterKeyDomain(final float[] keyValueArray, final float minKey, final float maxKey) {
		// First count how many are there to be filtered out
		int count = 0;
		for (int i = 0; i < keyValueArray.length; i += 2) {
			final float key = keyValueArray[i];
			if (key >= minKey && keyValueArray[i] <= maxKey) {
				count++;
			}
		}

		// Fill result array
		float result[] = new float[count * 2];
		if (count > 0) {
			int out = 0;
			for (int i = 0; i < keyValueArray.length; i += 2) {
				final float key = keyValueArray[i];
				if (key >= minKey && keyValueArray[i] <= maxKey) {
					result[out++] = keyValueArray[i];
					result[out++] = keyValueArray[i + 1];
				}
			}
		}

		return result;
	}

	/**
	 * @param minKey Minimum key (inclusive)
	 * @param maxKey Maximum key (inclusive)
	 * @return Maximum value attained somewhere between minimum and maximum keys.
	 */
	public static float getMaximumValueInKeyRange(final float[] keyValueArray, final float minKey, final float maxKey) {
		float maxValue = Float.MIN_VALUE;
		for (int i = 0; i < keyValueArray.length; i += 2) {
			final float key = keyValueArray[i];
			if (key >= minKey && keyValueArray[i] <= maxKey) {
				float value = keyValueArray[i + 1];
				if (value > maxValue) {
					maxValue = value;
				}
			}
		}
		return maxValue;
	}
}
