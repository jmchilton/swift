package edu.mayo.mprc.database;

import com.google.common.base.Preconditions;
import edu.mayo.mprc.MprcException;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Base for all DAO objects.
 * <p/>
 * TODO: This class is more of a DAO factory. We should clean this up by introducing actual DAO factories.
 */
public abstract class DaoBase implements Dao {
	// The hibernate field that stores the deletion change.
	public static final String DELETION_FIELD = "deletion";

	private DatabasePlaceholder databasePlaceholder;

	public DaoBase() {
	}

	public DaoBase(final DatabasePlaceholder databasePlaceholder) {
		this.databasePlaceholder = databasePlaceholder;
	}

	public void setDatabasePlaceholder(final DatabasePlaceholder databasePlaceholder) {
		this.databasePlaceholder = databasePlaceholder;
	}

	public DatabasePlaceholder getDatabasePlaceholder() {
		return databasePlaceholder;
	}

	protected Session getSession() {
		return databasePlaceholder.getSession();
	}

	@Override
	public void begin() {
		databasePlaceholder.begin();
	}

	@Override
	public void commit() {
		databasePlaceholder.commit();
	}

	@Override
	public void rollback() {
		databasePlaceholder.rollback();
	}

	/**
	 * Provides a list of all hibernate mapping files (.hbm.xml) that are needed for this Dao to function.
	 *
	 * @return List of hibernate mapping files to be used.
	 */
	public abstract Collection<String> getHibernateMappings();

	/**
	 * Return criteria ready to list all instances of given class that were not deleted before.
	 *
	 * @param clazz
	 * @return
	 */
	protected Criteria allCriteria(final Class<? extends Evolvable> clazz) {
		return getSession().createCriteria(clazz).add(Restrictions.isNull(DELETION_FIELD));
	}

	/**
	 * List all instances of given class. This skips the instances that were previously deleted.
	 * The list is collection to read-only, otherwise Hibernate would check modifications to all returned objects
	 * on each flush. Do not modify the objects from this list!
	 *
	 * @param clazz Class instances to list.
	 * @return A list of all instances.
	 */
	protected List<?> listAll(final Class<? extends Evolvable> clazz) {
		try {
			return (List<?>) allCriteria(clazz).setReadOnly(true).list();
		} catch (Exception t) {
			throw new MprcException("Cannot list all items of type " + clazz.getSimpleName(), t);
		}
	}

	/**
	 * Determine how many instances of a given class are in the database. Only the non-deleted ones are counted.
	 * For total count of all records, including deleted ones, use {@link #rowCount}.
	 *
	 * @param clazz Type of the class
	 * @return How many instances are in the database.
	 */
	public long countAll(final Class<? extends Evolvable> clazz) {
		final Object o = allCriteria(clazz)
				.setProjection(Projections.rowCount())
				.uniqueResult();
		if (o instanceof Number) {
			return ((Number) o).longValue();
		} else {
			throw new MprcException("Counting records of " + clazz.getSimpleName() + " did not return a number.");
		}
	}

	/**
	 * Counts all rows in a table corresponding to provided class. Unlike {@link #countAll}, this ignores
	 * deleted evolvable objects.
	 *
	 * @param clazz Class to count instances of.
	 * @return Total count of rows in the database for the given class.
	 */
	public long rowCount(final Class<?> clazz) {
		final Object o = getSession().createCriteria(clazz)
				.setProjection(Projections.rowCount())
				.uniqueResult();
		if (o instanceof Number) {
			return ((Number) o).longValue();
		} else {
			throw new MprcException("Counting rows of of " + clazz.getSimpleName() + " did not return a number.");
		}
	}

	/**
	 * Get a single instance that matches given equality criteria.
	 *
	 * @param clazz            Class to find.
	 * @param equalityCriteria Criteria for object equality.
	 * @return A single instance of a matching object, <c>null</c> if nothing matches the criteria.
	 */
	protected <T extends Evolvable> T get(final Class<? extends Evolvable> clazz, final Criterion equalityCriteria) {
		try {
			return (T) allCriteria(clazz)
					.add(equalityCriteria)
					.uniqueResult();
		} catch (Exception t) {
			throw new MprcException("Cannot get " + clazz.getSimpleName() + " matching specified criteria", t);
		}
	}

	/**
	 * Adds a simple collection object, making sure we do not store the same collection twice. A collection may contain only its elements,
	 * two sets are considered equal if all their elements are equal.
	 *
	 * @param owner      The owner object to be added to the database.
	 * @param collection The collection of elements the owner object contains. Two owners are considered identical if they contain
	 *                   the same collection.
	 * @param setField   Name of the field under which is the collection stored to the database.
	 */
	protected <T extends PersistableBase, S extends PersistableBase> T updateCollection(final T owner, final Collection<S> collection, final String setField) {
		final Session session = getSession();
		if (owner == null) {
			throw new MprcException("The owner of the collection must not be null");
		}
		if (owner.getId() != null) {
			throw new MprcException("The collection is already saved in the database.");
		}
		final T existing;
		final String className = owner.getClass().getName();

		if (collection.size() > 0) {
			existing = (T) getMatchingCollection(collection, setField, className);
		} else {
			existing = (T) getMatchingEmptyCollection(setField, className);
		}

		if (existing != null) {
			if (owner.equals(existing)) {
				// Item equals the saved object, bring forth the additional parameters that do not participate in equality.
				return updateSavedItem(existing, owner, session);
			} else {
				// If this happens, your equals operator is probably broken. Two collection objects must be equal
				// iff their elements are all equal
				throw new MprcException("Two collections with same elements are not considered equal: " + className);
			}
		}

		session.save(owner);
		return owner;
	}

	/**
	 * Adds a hashed set object, making sure we do not store the same cbag twice.
	 * An additional field is used for storing a hash key for the collection. This is used to optimize the equality checking.
	 *
	 * @param bag The set to update.
	 */
	protected <T extends PersistableHashedBagBase> T updateHashedBag(T bag) {
		final Session session = getSession();

		final long hash = calculateHash(bag.getList());
		bag.setHash(hash);

		final T existing = (T) getMatchingCollection(bag, "list", "hash", bag.getClass().getName(), hash);

		if (existing != null) {
			// Item equals the saved object, bring forth the additional parameters that do not participate in equality.
			return updateSavedItem(existing, bag, session);
		}

		session.save(bag);
		return bag;
	}

	/**
	 * Adds a hashed set object, making sure we do not store the same cbag twice.
	 * An additional field is used for storing a hash key for the collection. This is used to optimize the equality checking.
	 *
	 * @param set The set to update.
	 */
	protected <T extends PersistableHashedSetBase> T updateHashedSet(T set) {
		final Session session = getSession();

		final long hash = calculateHash(set.getList());
		set.setHash(hash);

		final T existing = (T) getMatchingCollection(set, "list", "hash", set.getClass().getName(), hash);

		if (existing != null) {
			// Item equals the saved object, bring forth the additional parameters that do not participate in equality.
			return updateSavedItem(existing, set, session);
		}

		session.save(set);
		return set;
	}

	private PersistableBase getMatchingEmptyCollection(final String setField, final String className) {
		final List ts = getSession().createQuery(
				"select s from " + className + " as s where s." + setField + ".size = 0")
				.list();
		if (1 < ts.size()) {
			throw new MprcException("Empty collection exists in two instances, database is probably corrupted");
		} else if (1 == ts.size()) {
			return (PersistableBase) ts.iterator().next();
		}
		return null;
	}

	private <S extends PersistableBase> PersistableBase getMatchingCollection(final Collection<S> collection, final String setField, final String className) {
		final Session session = getSession();
		final Integer[] ids = DatabaseUtilities.getIdList(collection);

		final List ts = session.createQuery(
				"select s from " + className + " as s join s." + setField + " as m where m.id in (:ids) and s.id in ("
						+ "select s.id from " + className + " where s." + setField + ".size = :ids_count "
						+ ") group by s.id having count(m)=:ids_count")
				.setParameterList("ids", ids)
				.setParameter("ids_count", ids.length)
				.list();
		if (1 < ts.size()) {
			throw new MprcException("There are " + ts.size() + " identical collections in the database");
		} else if (1 == ts.size()) {
			return (PersistableBase) ts.iterator().next();
		}
		return null;
	}

	private <S extends PersistableBase> PersistableBase getMatchingCollection(final Collection<S> collection, final String setField, final String hashField, final String className, long hash) {
		final Session session = getSession();

		final List ts = session.createQuery(
				"select s from " + className + " as s where s." + hashField + "=:hash")
				.setParameter("hash", hash)
				.list();

		PersistableBase result = null;
		for (final Object object : ts) {
			if (object instanceof PersistableBase) {
				if (result == null) {
					if (object.equals(collection)) {
						result = (PersistableBase) object;
					}
				} else {
					throw new MprcException("There is more than one identical collection in the database");
				}
			}
		}
		return result;
	}

	/**
	 * Take any collection that has persisted items. Collect all the ids of all the items, hash them in a defined way.
	 *
	 * @param collection Collection of items.
	 * @param <S>        Type of the collection items.
	 * @return Hash for the collection, ignoring item order.
	 */
	private <S extends PersistableBase> long calculateHash(Collection<S> collection) {
		final Integer[] ids = DatabaseUtilities.getIdList(collection);
		Arrays.sort(ids);
		long hash = 0;
		for (Integer id : ids) {
			hash = hash * 31 + id;
		}
		return hash;
	}

	/**
	 * Save an item. If identical item already exists, update it.
	 *
	 * @param item             The item to create (in database)
	 * @param change           What change is this save related to.
	 * @param equalityCriteria Criteria to find identical, already existing item. Do not need to check for the item class and deletion.
	 * @param createNew        New object must be created.
	 */
	protected <T extends Evolvable> T save(final T item, final Change change, final Criterion equalityCriteria, final boolean createNew) {
		final Session session = getSession();
		final Evolvable existingObject = (Evolvable)
				allCriteria(item.getClass()) // It is not deprecated already
						.add(equalityCriteria)
						.uniqueResult();

		if (existingObject != null) {
			if (createNew) {
				throw new MprcException(item.getClass().getSimpleName() + " already exists");
			} else if (item.equals(existingObject)) {
				// Item equals the saved object, bring forth the additional parameters that do not participate in equality.
				item.setId(existingObject.getId());
				item.setCreation(existingObject.getCreation());
				item.setDeletion(existingObject.getDeletion());
				session.merge(item);
				return (T) item;
			}
		}

		if (change.getId() == null) {
			session.save(change);
		}

		if (existingObject != null) {
			// Deprecate the existing object
			existingObject.setDeletion(change);
		}

		item.setCreation(change);
		item.setId(null);
		session.save(item);
		return item;
	}

	/**
	 * Save an item. If identical item already exists, update the current item.
	 *
	 * @param item             The item to create (in database)
	 * @param equalityCriteria Criteria to find identical, already existing item. Do not need to check for the item class and deletion.
	 * @param createNew        New object must be created.
	 * @return The saved object. This can be either the newly saved item (it did not exist in the database yet), or a result of Hibernate's merge operation.
	 */
	protected <T extends PersistableBase> T save(final T item, final Criterion equalityCriteria, final boolean createNew) {
		final Session session = getSession();
		final T existingObject = (T) session
				.createCriteria(item.getClass())
				.add(equalityCriteria)
				.uniqueResult();

		if (existingObject != null) {
			if (createNew) {
				throw new MprcException(item.getClass().getSimpleName() + " already exists");
			} else if (item.equals(existingObject)) {
				return updateSavedItem(existingObject, item, session);
			}
		}

		item.setId(null);
		session.save(item);
		return item;
	}

	/**
	 * We quickly save an object using the stateless session. We assume that two objects have to be identical
	 * if their equality criteria suggest they are equal, so once the object is found in the database, no merging is needed.
	 * We also assume that if our object has id assigned, it has been saved already.
	 *
	 * @param session
	 * @param item
	 * @param equalityCriteria
	 * @param createNew
	 * @param <T>
	 * @return
	 */
	protected <T extends PersistableBase> T saveStateless(final StatelessSession session, final T item, final Criterion equalityCriteria, final boolean createNew) {
		if (item.getId() != null) {
			return item;
		}

		final T existingObject = equalityCriteria == null ? null : (T) session
				.createCriteria(item.getClass())
				.add(equalityCriteria)
				.uniqueResult();

		if (existingObject != null) {
			if (createNew) {
				throw new MprcException(item.getClass().getSimpleName() + " already exists");
			} else {
				return existingObject;
			}
		}

		session.insert(item);
		return item;
	}

	/**
	 * Save an item. There can be multiple items that appear to be identical. Pick the one that truly is
	 * (using equals) and merge with it.
	 *
	 * @param item             The item to create (in database)
	 * @param equalityCriteria Criteria to find identical, already existing item. Do not need to check for the item class and deletion.
	 * @param createNew        New object must be created.
	 */
	protected <T extends PersistableBase> T saveLaxEquality(final T item, final Criterion equalityCriteria, final boolean createNew) {
		final Session session = getSession();
		final List<T> existingObjects = (List<T>) session
				.createCriteria(item.getClass())
				.add(equalityCriteria)
				.list();

		if (existingObjects != null && existingObjects.size() > 0) {
			for (final T existingObject : existingObjects) {
				if (existingObject.equals(item)) {
					if (createNew) {
						throw new MprcException(item.getClass().getSimpleName() + " already exists");
					} else {
						return updateSavedItem(existingObject, item, session);
					}
				}
			}
		}

		item.setId(null);
		session.save(item);
		return item;
	}

	/**
	 * Item equals the saved object, bring forth the additional parameters that do not participate in equality.
	 *
	 * @param savedItem  Item that is already in the database, that is supposed to be equal to a provided one.
	 * @param updateWith New item that is equivalent to the previously saved one.
	 * @param session    Database session.
	 * @param <T>        Type of the item.
	 * @return A saved item that is updated with the latest values from {@code updateWith}.
	 */
	private <T extends PersistableBase> T updateSavedItem(final T savedItem, final T updateWith, final Session session) {
		updateWith.setId(savedItem.getId());
		return (T) session.merge(updateWith);
	}


	/**
	 * Delete a previously saved item. Deletion just means the deletion attribute gets collection.
	 *
	 * @param item   The item to create (in database)
	 * @param change What change is this creation related to.
	 */
	protected void delete(final Evolvable item, final Change change) {
		if (item.getDeletion() != null) {
			// Already deleted
			return;
		}
		if (item.getId() == null) {
			throw new MprcException("Not previously saved");
		}

		if (change.getId() == null) {
			getSession().save(change);
		}
		item.setDeletion(change);
	}

	/**
	 * A criterion testing that a  particular property is associated with a particular object.
	 *
	 * @param propertyName Property to test.
	 * @param association  This is what the property has to point to.
	 * @return Query criterion that will check for the particular association.
	 */
	public static Criterion associationEq(final String propertyName, final PersistableBase association) {
		Preconditions.checkArgument(association == null || association.getId() != null, "Association to class %s has to be previously saved", association != null ? association.getClass().getName() : "(null)");
		if (association == null) {
			return Restrictions.isNull(propertyName);
		} else {
			return Restrictions.eq(propertyName + ".id", association.getId());
		}
	}

	public static Criterion nullSafeEq(final String propertyName, final Object value) {
		if (value == null) {
			return Restrictions.isNull(propertyName);
		} else {
			return Restrictions.eq(propertyName, value);
		}
	}

	/**
	 * @param propertyName Name of the property to check.
	 * @param value        Value that has to be within the range of the database entry.
	 * @param tolerance    The value has to be in &lt;value-tolerance, value+tolerance&gt; range.
	 * @return A criterion that checks the given property is in the permissible range.
	 */
	public static Criterion doubleEq(final String propertyName, final double value, final double tolerance) {
		if (Double.isNaN(value)) {
			return Restrictions.isNull(propertyName);
		}
		return new Conjunction()
				.add(Restrictions.ge(propertyName, value - tolerance))
				.add(Restrictions.le(propertyName, value + tolerance));
	}
}
