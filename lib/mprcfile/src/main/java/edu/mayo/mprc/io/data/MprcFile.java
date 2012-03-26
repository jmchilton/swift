package edu.mayo.mprc.io.data;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;

import java.io.File;
import java.sql.*;

/**
 * Data access object for the generic MPRC data format.
 * This object is used by more specific DAOs that store specific data within the format.
 */
public final class MprcFile {
	private final File databaseFile;
	private Connection connection = null;

	public MprcFile(final File databaseFile) {
		this.databaseFile = databaseFile;
	}

	public synchronized void open() {
		if (connection == null) {
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (Exception t) {
				throw new MprcException("Sql initialization failed", t);
			}

			try {
				// create a database connection
				connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
			} catch (Exception e) {
				// if the error message is "out of memory",
				// it probably means no database file is found
				throw new MprcException("Cannot open database connection to " + databaseFile.getAbsolutePath(), e);
			}
		}
	}

	public synchronized void close() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			// connection close failed.
			throw new MprcException("Closing database " + databaseFile.getAbsolutePath() + " failed.", e);
		} finally {
			connection = null;
		}
	}

	/**
	 * Locked access to the connection object.
	 *
	 * @return Connection.
	 */
	private synchronized Connection getConnection() {
		return connection;
	}

	/**
	 * Processes result set obtained from given sql using a callback method.
	 *
	 * @param sql      The sql command to execute.
	 * @param callback Callback that is passed the recordset resulting from the sql command.
	 * @return The object returned from {@link MprcFileCallback}.
	 */
	public Object processResultSet(final String sql, final MprcFileCallback callback) {
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = getConnection().prepareStatement(sql);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			rs = statement.executeQuery();
			return callback.processResultSet(rs);
		} catch (Exception t) {
			throw new MprcException("Error accessing data from " + databaseFile.getAbsolutePath(), t);
		} finally {
			FileUtilities.closeObjectQuietly(rs);
			closeStatement(statement);
		}
	}

	// Helper function for closing opened statements

	public static void closeStatement(final Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (Exception ignore) {
				// SWALLOWED: We do not care about unclosed statements
			}
		}
	}


}
