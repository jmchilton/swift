package edu.mayo.mprc.unimod;

import edu.mayo.mprc.database.Change;
import org.hibernate.Session;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Performs an upgrade, making one set of {@link edu.mayo.mprc.database.Evolvable} to match another set.
 * The original set should be fully persisted, the new set should not.
 */
public final class UnimodUpgrade {
	private int originalItems;
	private int curentItems;
	private int itemsAdded;
	private int itemsModified;
	private int itemsDeleted;
	private static final int FLUSH_FREQUENCY = 100;

	/**
	 * Perform upgrade, making sure the current database contains all the current items.
	 * Since this is a batch operation, we evict all the modifications from Hibernate cache to save memory.
	 *
	 * @param original Original set of items. It is assumed that all the objects are in the managed (persistent) state.
	 * @param current  Current set of items.
	 * @param change   Change description we are performing now.
	 * @param session  Database session.
	 */
	public void upgrade(final Collection<Mod> original, final IndexedModSet current, final Change change, final Session session) {
		session.saveOrUpdate(change);

		originalItems = original.size();
		curentItems = current.size();

		itemsAdded = 0;
		itemsModified = 0;
		itemsDeleted = 0;

		final Set<Mod> toAdd = new HashSet<Mod>();
		toAdd.addAll(current);

		int flushCounter = 0;

		for (final Mod orig : original) {
			final Mod matching = findMatching(orig, current);
			if (matching == null) {
				orig.setDeletion(change);
				session.saveOrUpdate(orig);
				itemsDeleted++;
			} else {
				toAdd.remove(matching);
				session.saveOrUpdate(orig);
				if (!identical(orig, matching)) {
					orig.setDeletion(change);
					session.saveOrUpdate(orig);
					matching.setCreation(change);
					session.saveOrUpdate(matching);
					itemsModified++;
				}
			}

			flushCounter = flushPeriodically(session, flushCounter);
		}

		for (final Mod added : toAdd) {
			added.setCreation(change);
			session.saveOrUpdate(added);
			itemsAdded++;

			flushCounter = flushPeriodically(session, flushCounter);
		}
	}

	/**
	 * Once in a while, flush the session and clear it - unimod is potentially large so the upgrade operation
	 * can use a lot of Hibernate cache.
	 *
	 * @param session      Session to flush
	 * @param flushCounter Counter
	 * @return Incremented counter
	 */
	private int flushPeriodically(final Session session, int flushCounter) {
		flushCounter++;
		if (flushCounter % FLUSH_FREQUENCY == 0) {
			session.flush();
			session.clear();
		}
		return flushCounter;
	}

	public int getOriginalItems() {
		return originalItems;
	}

	public int getCurentItems() {
		return curentItems;
	}

	public int getItemsAdded() {
		return itemsAdded;
	}

	public int getItemsModified() {
		return itemsModified;
	}

	public int getItemsDeleted() {
		return itemsDeleted;
	}

	/**
	 * Find matching element in the current collection. The element does not have to be identical.
	 * If no matching element is found, return 0.
	 */
	protected Mod findMatching(final Mod orig, final IndexedModSet current) {
		return current.getByTitle(orig.getTitle());
	}

	/**
	 * @param orig     Original object.
	 * @param matching Matching new object.
	 * @return True if the new object is identical to the original one.
	 */
	protected boolean identical(final Mod orig, final Mod matching) {
		return orig.equals(matching);
	}

	@Override
	public String toString() {
		return "Unimod upgraded from " + originalItems + " mods to "
				+ curentItems + ": "
				+ itemsAdded + " items added, "
				+ itemsModified + " items modified, "
				+ itemsDeleted + " items deleted.";

	}
}
