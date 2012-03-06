package edu.mayo.mprc.utilities;

/**
 * Missing functionality in Guava Doubles that will be hopefully added soon.
 *
 * @author Roman Zenka
 */
public final class MprcDoubles {
	private MprcDoubles() {
	}

	/**
	 * Check that a given double value is within tolerance from expected.
	 * <p/>
	 * <b>WARNING</b>: This is used to compare doubles for identity,
	 * relaxing the precision. This is why {@code within(Double.NaN, Double.NaN, whatever)} is true - we assume that
	 * NaN means "value missing" and if it is missing on both sides, it is within the range. Other special values (infinities)
	 * are not supported.
	 *
	 * @param expected  Value we expect to see.
	 * @param actual    Actual value.
	 * @param tolerance Actual has to be within &lt;tolerance-expected, tolerance+expected&gt;
	 * @return True if the actual value is within tolerance from expected.
	 */
	public static boolean within(final double expected, final double actual, final double tolerance) {
		return expected - tolerance <= actual && actual <= expected + tolerance
				||
				Double.isNaN(expected) && Double.isNaN(actual);
	}
}
