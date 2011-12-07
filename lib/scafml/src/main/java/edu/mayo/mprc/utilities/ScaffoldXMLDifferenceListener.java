package edu.mayo.mprc.utilities;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public final class ScaffoldXMLDifferenceListener implements DifferenceListener {
	private static final double DOUBLE_PRECISION = 0.001;

	/**
	 * Determines if a difference that has been detected is an actual difference or if it should be ignored.
	 *
	 * @param difference
	 * @return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR if the difference was not significent else RETURN_ACCEPT_DIFFERENCE
	 */
	public int differenceFound(Difference difference) {
		if (isIgnoredDifference(difference)) {
			return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
		} else {
			return RETURN_ACCEPT_DIFFERENCE;
		}
	}

	public void skippedComparison(Node node, Node node1) {
		//null implementation to satisify interface
	}

	/**
	 * Can this difference be ignored?
	 * Currently we ignore these differences:
	 * <ul>
	 * <li>floating point numbers differing for less than {@link #DOUBLE_PRECISION}</li>
	 * <li>experiment name</li>
	 * <li>date when the analysis was taken</li>
	 * <li>Scaffold version difference</li>
	 *
	 * @param difference Detected difference
	 * @return Whether this deference is to be ignored.
	 */
	private boolean isIgnoredDifference(Difference difference) {
		try {
			return isAnalysisDateDifference(difference)
					|| isAcceptableFloatingPointDifference(difference)
					|| isExperimentNameDifference(difference)
					|| isCompatibleVersionDifference(difference);
		} catch (Exception ignore) {
			//SWALLOWED there was something in there that was null so will just say there was a difference.
			return false;
		}
	}

	private boolean isAnalysisDateDifference(Difference difference) {
		return "analysisDate".equals(difference.getControlNodeDetail().getNode().getNodeName());
	}

	protected boolean isCompatibleVersionDifference(Difference difference) {
		return "version".equals(difference.getControlNodeDetail().getNode().getNodeName());
	}

	protected boolean isExperimentNameDifference(Difference difference) {
		return "experimentName".equals(difference.getControlNodeDetail().getNode().getNodeName());
	}

	private boolean isAcceptableFloatingPointDifference(Difference difference) {
		Pattern p = Pattern.compile("[-+]?\\d*(?:\\.\\d*)?(?:[eE][+-]?\\d+)?");
		Matcher m = p.matcher(difference.getControlNodeDetail().getValue());
		try {
			if (m.matches()) {
				String value1 = difference.getControlNodeDetail().getValue();
				String value2 = difference.getTestNodeDetail().getValue();

				double double1 = Double.valueOf(value1);
				double double2 = Double.valueOf(value2);

				return similarNumbers(double1, double2);
			}
		} catch (Exception e) {
			return false;
		}

		return false;
	}


	/**
	 * takes two numbers as doubles and determines if they are similar to a given precision. <br>
	 *
	 * @param d1 the first number to compare
	 * @param d2 the second number to compare
	 * @return true if the difference between the values is less than indicated by the precision
	 */
	private static boolean similarNumbers(double d1, double d2) {
		double difference = Math.abs(d1 - d2);
		return (difference < DOUBLE_PRECISION);
	}
}
