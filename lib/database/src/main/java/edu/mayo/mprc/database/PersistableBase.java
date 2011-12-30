package edu.mayo.mprc.database;

import edu.mayo.mprc.MprcException;

/**
 * For objects that are persistable into the database.
 *
 * @author Roman Zenka
 */
public abstract class PersistableBase {
	/**
	 * Id for hibernate.
	 */
	private Integer id;

	/**
	 * @return Hibernate's ID of the object.
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Set the persistable object id. This is usually called by Hibernate, but sometimes we cheat
	 * and set an ID from a different object to pretend the object was disconnected from the cache and
	 * now it is connecting.
	 * <p/>
	 * Once the id is set to not-null, you should NEVER set it back to null. Id as not null means the
	 * object is persisted by Hibernate - nulling it would cause trouble, so we check for this.
	 *
	 * @param id New id of the object.
	 */
	public void setId(Integer id) {
		if (this.id != null && id == null) {
			throw new MprcException("The id was set to null - this should not happen ever");
		}
		this.id = id;
	}
}
