package edu.mayo.mprc.database;

public interface Dao {
	/**
	 * Session-per-request pattern - start database interaction. Open a session, open a transaction.
	 */
	void begin();

	/**
	 * Session-per-request pattern - commit to database. Flush the session, commit to database, close the session.
	 */
	void commit();

	/**
	 * Rollback the transaction, close the session.
	 */
	void rollback();
}
