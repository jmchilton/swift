package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * A sorted list of modifications. Stores its hash code so it is easy to compare a list against the database for
 * equivalency.
 *
 * @author Roman Zenka
 */
public class LocalizedModificationList extends PersistableBase implements Iterable<LocalizedModification> {
    private TreeSet<LocalizedModification> modifications = new TreeSet<LocalizedModification>();

    public TreeSet<LocalizedModification> getModifications() {
        return modifications;
    }

    public void setModifications(TreeSet<LocalizedModification> modifications) {
        this.modifications = modifications;
    }

    /**
     * Cached hash code for the list. This is used to optimize fetching identical modification lists from
     * the database.
     */
    private int hashCode;

    /**
     * Fix the list of mods - call when you are done adding modifications.
     * Calculates metadata that help matching similar lists.
     */
    public void fix() {
        hashCode = hashCode();
    }

    public void add(LocalizedModification localizedModification) {
        hashCode = 0;
        modifications.add(localizedModification);
    }

    public int size() {
        return modifications.size();
    }

    public int getHashCode() {
        return hashCode;
    }

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    @Override
    public Iterator<LocalizedModification> iterator() {
        return modifications.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalizedModificationList that = (LocalizedModificationList) o;

        if (modifications != null ? !modifications.equals(that.modifications) : that.modifications != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (this.hashCode == 0) {
            int hashCode = 0;
            for (LocalizedModification mod : modifications) {
                hashCode = 31 * hashCode + mod.hashCode();
            }
            this.hashCode = hashCode;
        }
        return this.hashCode;
    }
}
