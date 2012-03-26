package edu.mayo.mprc.swift.dbmapping;


/**
 * Holds information about current DB version.
 */
public class SwiftDBVersion {
	private static final int DEFAULT_ID = 1;
	private Integer id = DEFAULT_ID;
	private Integer version;

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(final Integer version) {
		this.version = version;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SwiftDBVersion)) {
			return false;
		}

		final SwiftDBVersion that = (SwiftDBVersion) o;

		if (getVersion() != null ? !getVersion().equals(that.getVersion()) : that.getVersion() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getVersion() != null ? getVersion().hashCode() : 0;
	}
}
