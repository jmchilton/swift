package edu.mayo.mprc.config;

import java.util.Map;

/**
 * Initializes a module before it is being run.
 * The initialization consists of a check whether the work needs to be done. If that is the case,
 * the initialization itself is invoked.
 */
public interface RuntimeInitializer {
	/**
	 * Check that all the work needed for a module to run is done.
	 *
	 * @param params Parameters for the checking.
	 * @return <ul>
	 *         <li>non-null user message - calling of the {@link #initialize(java.util.Map)} is needed</li>
	 *         <li>null - everything is in place and ok</li>
	 *         </ul>
	 */
	String check(Map<String, String> params);

	/**
	 * Ensures that all the work needed for a module to run is done. This is called only if {@link #check(Map)}
	 * returns that work is needed.
	 * This is usually done when initializing the database, initial directory layout, etc.
	 *
	 * @param params Parameters for the initialization.
	 */
	void initialize(Map<String, String> params);
}
