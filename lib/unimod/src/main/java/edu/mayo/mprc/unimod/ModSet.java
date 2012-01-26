package edu.mayo.mprc.unimod;

import com.google.common.base.Objects;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.database.PersistableBase;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A set of {@link ModSpecificity}. Is persistable via Hibernate and can be compared to other modification sets.
 * <p/>
 * Obtain one by creating an empty set, then getting the {@link ModSpecificity} objects using for example
 * {@link Unimod#getSpecificitiesByMascotName(String)}.
 *
 * @author Roman Zenka
 */
public class ModSet extends PersistableBase implements Comparable<ModSet> {
    /**
     * all modification in this set
     */
    protected Set<ModSpecificity> modifications = new TreeSet<ModSpecificity>();

    /**
     * @return A set of all modifications.
     */
    public Set<ModSpecificity> getModifications() {
        return modifications;
    }

    void setModifications(Set<ModSpecificity> modifications) {
        if (getId() != null) {
            throw new MprcException("Modification set is immutable once saved");
        }
        this.modifications = modifications;
    }

    public int size() {
        return modifications.size();
    }

    public boolean contains(ModSpecificity m) {
        return modifications.contains(m);
    }

    public Iterator<ModSpecificity> iterator() {
        return modifications.iterator();
    }

    public boolean add(ModSpecificity mod) {
        if (getId() != null) {
            throw new MprcException("Modification set is immutable once saved");
        }
        return modifications.add(mod);
    }

    public boolean addAll(Collection<ModSpecificity> mods) {
        if (getId() != null) {
            throw new MprcException("Modification set is immutable once saved");
        }
        return modifications.addAll(mods);
    }

    public ModSet copy() {
        ModSet copy = new ModSet();
        for (ModSpecificity specificity : getModifications()) {
            copy.add(specificity.copy());
        }
        copy.setId(getId());
        return copy;
    }

    public int compareTo(ModSet tt) {
        if (this.getModifications().size() < tt.getModifications().size()) {
            return -1;
        }
        if (this.getModifications().size() > tt.getModifications().size()) {
            return 1;
        }
        for (Iterator<ModSpecificity> i = this.getModifications().iterator(), j = tt.getModifications().iterator(); i.hasNext(); ) {
            int ret = i.next().compareTo(j.next());
            if (ret != 0) {
                return ret;
            }
        }
        return 0;
    }

    public boolean equals(Object t) {
        if (!(t instanceof ModSet)) {
            return false;
        }
        ModSet tt = (ModSet) t;
        return Objects.equal(this.getModifications(), tt.getModifications());
    }

    public int hashCode() {
        return getModifications().hashCode();
    }
}
