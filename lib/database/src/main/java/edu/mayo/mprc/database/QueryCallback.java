package edu.mayo.mprc.database;

/**
 * A callback from the scroll query.
 * @author Roman Zenka
 */
public interface QueryCallback {
	/**
	 * @param data Data from the query.
	 */
	void process(Object[] data);
}
