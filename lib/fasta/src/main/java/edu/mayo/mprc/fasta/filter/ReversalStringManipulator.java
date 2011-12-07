package edu.mayo.mprc.fasta.filter;

/**
 * A string manipulator that takes a string and reverses it and then returns the reversed string
 *
 * @author Eric Winter
 */
public final class ReversalStringManipulator implements StringManipulator {
	public String manipulateString(String toManipulate) {
		StringBuilder manipulatedString = new StringBuilder(toManipulate);
		manipulatedString.reverse();
		return manipulatedString.toString();
	}

	public String getDescription() {
		return "Reversed";
	}
}
