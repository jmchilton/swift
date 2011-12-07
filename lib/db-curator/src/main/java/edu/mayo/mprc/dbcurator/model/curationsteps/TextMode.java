package edu.mayo.mprc.dbcurator.model.curationsteps;

/**
 * An enum that is used to signify what type of text mode is used.
 */
public enum TextMode {
	/**
	 * used when the .containsIgnoreCase() can be called on the string passing in the criteria.
	 */
	SIMPLE,

	/**
	 * use when a regular expression type search is allowed.  This is the most powerful search critera.
	 */
	REG_EX
}
