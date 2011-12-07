package edu.mayo.mprc.fasta.filter;

/**
 * An enumeration for different match types.  This is it own independant class for some odd Hibernate reasons.
 */
public enum MatchMode {
	/**
	 * When we should require that all criteria be matched to provide a true result
	 */
	ALL,

	/**
	 * When only one of the fields needs to be present (but multiple can be)
	 */
	ANY,

	/**
	 * When none of the criteria should be a included result (opposite of ANY)
	 */
	NONE
}
