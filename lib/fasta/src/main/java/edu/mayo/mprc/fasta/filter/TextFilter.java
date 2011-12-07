package edu.mayo.mprc.fasta.filter;

/**
 * Basically an abstraction around different ways of testing a String to see if it contains something.
 *
 * @author Eric J. Winter Date: Apr 6, 2007 Time: 1:45:33 PM
 */
public interface TextFilter {

	/**
	 * used to indicate that the text filter is a valid filter (used for checking)
	 */
	String VALID = "Valid";

	/**
	 * An enumeration for indicating the mode of the filter. ALL - all "words" must be found in the header ANY - any of
	 * the "words" must be found in the header NONE - none of the "words" must be found in the header
	 */


	/**
	 * A method to see if toMatch contains text that matches this filters criteria.
	 *
	 * @param toMatch the String that you want to test
	 * @return true if this filter matches at least one segment of the toMatch String
	 */
	boolean matches(String toMatch);


	/**
	 * sets the mode that should be used when running this filter such as ALL, ANY, or NONE
	 *
	 * @param mode
	 */
	void setMatchMode(MatchMode mode);

	/**
	 * Tests to see if the currently set criteria is a valid search expression
	 *
	 * @return if the criteria is valid the VALID String is returned else a short string indicating where the problem
	 *         may be will be given
	 */
	String testCriteria();

}
