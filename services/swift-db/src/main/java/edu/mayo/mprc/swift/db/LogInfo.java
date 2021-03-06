package edu.mayo.mprc.swift.db;


public final class LogInfo {
	private String type;
	private String taggedDatabaseToken;

	public static final String STD_ERR_LOG_TYPE = "STD_ERROR";
	public static final String STD_OUT_LOG_TYPE = "STD_OUT";

	public LogInfo(String type, String taggedDatabaseToken) {
		this.type = type;
		this.taggedDatabaseToken = taggedDatabaseToken;
	}

	public String getType() {
		return type;
	}

	public void setType(String Type) {
		this.type = Type;
	}

	public String getTaggedDatabaseToken() {
		return taggedDatabaseToken;
	}

	public void setTaggedDatabaseToken(String taggedDatabaseToken) {
		this.taggedDatabaseToken = taggedDatabaseToken;
	}
}
