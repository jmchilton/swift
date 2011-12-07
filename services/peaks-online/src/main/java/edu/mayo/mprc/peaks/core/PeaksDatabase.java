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
	public PeaksDatabase(String databaseId, String databaseName, String databaseFilePath, String databaseFormat, boolean estDatabase) {
		this.databaseId = databaseId;
		this.databaseName = databaseName;
		this.databaseFilePath = databaseFilePath;
		this.databaseFormat = databaseFormat;
		this.estDatabase = estDatabase;
	}

	public PeaksDatabase(String databaseName, String databaseFilePath, String databaseFormat, boolean estDatabase) {
		this(null, databaseName, databaseFilePath, databaseFormat, estDatabase);
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getDatabaseFilePath() {
		return databaseFilePath;
	}

	public void setDatabaseFilePath(String databaseFilePath) {
		this.databaseFilePath = databaseFilePath;
	}

	public String getDatabaseFormat() {
		return databaseFormat;
	}

	public void setDatabaseFormat(String databaseFormat) {
		this.databaseFormat = databaseFormat;
	}

	public boolean isEstDatabase() {
		return estDatabase;
	}

	public void setEstDatabase(boolean estDatabase) {
		this.estDatabase = estDatabase;
	}

	public String getDatabaseId() {
		return databaseId;
	}

	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("Database Id: ").append(databaseId).append("\n");
		builder.append("Database Name: ").append(databaseName).append("\n");
		builder.append("Database File Path: ").append(databaseFilePath).append("\n");
		builder.append("Database Format: ").append(databaseFormat).append("\n");
		builder.append("EST Database: ").append(estDatabase);

		return builder.toString();
	}
}
