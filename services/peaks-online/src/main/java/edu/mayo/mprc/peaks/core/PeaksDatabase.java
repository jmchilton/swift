package edu.mayo.mprc.peaks.core;

import java.io.Serializable;

/**
 * Class represents a peaks online database.
 */
public final class PeaksDatabase implements Serializable {
	private static final long serialVersionUID = 20090324L;
	private String databaseId;
	private String databaseName;
	private String databaseFilePath;
	private String databaseFormat;
	private boolean estDatabase;

	/**
	 * @param databaseId       Id of this database in the PeaksOnline system. If this is a new database that does not
	 *                         exist in the PeaksOnline system, this value should be null.
	 * @param databaseName
	 * @param databaseFilePath
	 * @param databaseFormat
	 * @param estDatabase
	 */
	public PeaksDatabase(final String databaseId, final String databaseName, final String databaseFilePath, final String databaseFormat, final boolean estDatabase) {
		this.databaseId = databaseId;
		this.databaseName = databaseName;
		this.databaseFilePath = databaseFilePath;
		this.databaseFormat = databaseFormat;
		this.estDatabase = estDatabase;
	}

	public PeaksDatabase(final String databaseName, final String databaseFilePath, final String databaseFormat, final boolean estDatabase) {
		this(null, databaseName, databaseFilePath, databaseFormat, estDatabase);
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(final String databaseName) {
		this.databaseName = databaseName;
	}

	public String getDatabaseFilePath() {
		return databaseFilePath;
	}

	public void setDatabaseFilePath(final String databaseFilePath) {
		this.databaseFilePath = databaseFilePath;
	}

	public String getDatabaseFormat() {
		return databaseFormat;
	}

	public void setDatabaseFormat(final String databaseFormat) {
		this.databaseFormat = databaseFormat;
	}

	public boolean isEstDatabase() {
		return estDatabase;
	}

	public void setEstDatabase(final boolean estDatabase) {
		this.estDatabase = estDatabase;
	}

	public String getDatabaseId() {
		return databaseId;
	}

	public String toString() {

		final StringBuilder builder = new StringBuilder();
		builder.append("Database Id: ").append(databaseId).append("\n");
		builder.append("Database Name: ").append(databaseName).append("\n");
		builder.append("Database File Path: ").append(databaseFilePath).append("\n");
		builder.append("Database Format: ").append(databaseFormat).append("\n");
		builder.append("EST Database: ").append(estDatabase);

		return builder.toString();
	}
}
