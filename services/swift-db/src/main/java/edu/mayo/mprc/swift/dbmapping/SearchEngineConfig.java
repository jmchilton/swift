package edu.mayo.mprc.swift.dbmapping;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.database.EvolvableBase;

/**
 * User-editable information about a supported search engine.
 * Right now we do not store any additional information, the class is used only to store sets of engines to perform
 * searches with.
 */
public class SearchEngineConfig extends EvolvableBase {

	/**
	 * Unique text identifier for the engine (e.g. <c>MASCOT</c>).
	 */
	private String code;

	public SearchEngineConfig() {
	}

	public SearchEngineConfig(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		if (code == null) {
			throw new MprcException("Search engine code cannot be null");
		}
		this.code = code;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof SearchEngineConfig)) {
			return false;
		}

		SearchEngineConfig that = (SearchEngineConfig) o;

		if (getCode() != null ? !getCode().equals(that.getCode()) : that.getCode() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getCode() != null ? getCode().hashCode() : 0;
	}
}
