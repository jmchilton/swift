package edu.mayo.mprc.scaffoldparser;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * We ignore the "id" values as our current saver can mess those up. This means that we could accidentally
 * consider two files similar while they differ in how the nodes are referenced. In practice, this should never
 * be the case.
 * <p/>
 * Floating point values that are within {@link #DOUBLE_PRECISION} are considered identical.
 * <p/>
 * The .xml file version, the date when it was created, and the experiment names are ignored as well.
 *
 * @author Eric Winter
 */
public final class ScaffoldXMLDifferenceListener implements DifferenceListener {
	private static final double DOUBLE_PRECISION = 0.001;
	private static final Pattern FLOATING_POINT = Pattern.compile("[-+]?\\d*(?:\\.\\d*)?(?:[eE][+-]?\\d+)?");

	/**
	 * Determines if a difference that has been detected is an actual difference or if it should be ignored.
	 *
	 * @param difference XMLUnit difference between two XML files.
	 * @return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR if the difference was not significent else RETURN_ACCEPT_DIFFERENCE
	 */
	public int differenceFound(final Difference difference) {
		if (isIgnoredDifference(difference)) {
			return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
		} else {
			return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
		}
	}

	public void skippedComparison(final Node node, final Node node1) {
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
	private boolean isIgnoredDifference(final Difference difference) {
		try {
			return isIdDifference(difference)
					|| isAnalysisDateDifference(difference)
					|| isAcceptableFloatingPointDifference(difference)
					|| isExperimentNameDifference(difference)
					|| isCompatibleVersionDifference(difference)
					|| isIdMissing(difference);
		} catch (Exception ignore) {
			//SWALLOWED there was something in there that was null so will just say there was a difference.
			return false;
		}
	}

	private boolean isIdDifference(final Difference difference) {
		final String nodeName = difference.getControlNodeDetail().getNode().getNodeName();
		return "id".equals(nodeName) || "reference".equals(nodeName);
	}

	private boolean isIdMissing(final Difference difference) {
		final Node id1 = difference.getControlNodeDetail().getNode().getAttributes().getNamedItem("id");
		final Node id2 = difference.getTestNodeDetail().getNode().getAttributes().getNamedItem("id");
		return (id1 == null) != (id2 == null);
	}

	private boolean isAnalysisDateDifference(final Difference difference) {
		return "analysisDate".equals(difference.getControlNodeDetail().getNode().getNodeName());
	}

	protected boolean isCompatibleVersionDifference(final Difference difference) {
		return "version".equals(difference.getControlNodeDetail().getNode().getNodeName());
	}

	protected boolean isExperimentNameDifference(final Difference difference) {
		return "experimentName".equals(difference.getControlNodeDetail().getNode().getNodeName());
	}

	private boolean isAcceptableFloatingPointDifference(final Difference difference) {
		final Matcher matcher = FLOATING_POINT.matcher(difference.getControlNodeDetail().getValue());
		try {
			if (matcher.matches()) {
				final String value1 = difference.getControlNodeDetail().getValue();
				final String value2 = difference.getTestNodeDetail().getValue();

				final double double1 = Double.valueOf(value1);
				final double double2 = Double.valueOf(value2);

				return similarNumbers(double1, double2);
			}
		} catch (Exception ignore) {
			// SWALLOWED - we could not parse, consider it a difference
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
	private static boolean similarNumbers(final double d1, final double d2) {
		final double difference = Math.abs(d1 - d2);
		return (difference < DOUBLE_PRECISION);
	}
}
