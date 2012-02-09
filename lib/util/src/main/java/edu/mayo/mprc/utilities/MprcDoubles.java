package edu.mayo.mprc.utilities;

/**
 * Missing functionality in Guava Doubles that will be hopefully added soon.
 *
 * @author Roman Zenka
 */
public class MprcDoubles {

    /**
     * Check that a given double value is within tolerance from expected.
     *
     * @param expected  Value we expect to see.
     * @param actual    Actual value.
     * @param tolerance Actual has to be within &lt;tolerance-expected, tolerance+expected&gt;
     * @return True if the actual value is within tolerance from expected.
     */
    public static boolean within(double expected, double actual, double tolerance) {
        return expected - tolerance <= actual && actual <= expected + tolerance;
    }
}
