package edu.mayo.mprc.database;

import edu.mayo.mprc.MprcException;

/**
 * For objects that are persistable into the database.
 */
public abstract class PersistableBase {
	/**
	 * Id for hibernate.
	 */
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		if (this.id != null && id == null) {
			throw new MprcException("The id was set to null - this should not happen ever");
		}
		this.id = id;
	}
}
