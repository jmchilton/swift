package edu.mayo.mprc.database;

import com.google.common.collect.LinkedHashMultiset;

import java.util.Collection;
import java.util.Iterator;

/**
 * Base for classes that are nothing but a list to be persisted.
 * The list should be reasonably small for this to work well.
 *
 * @author Roman Zenka
 */
public abstract class PersistableListBase<T extends PersistableBase> extends PersistableBase implements Collection<T> {
    private Collection<T> list;

    public PersistableListBase() {
        list = LinkedHashMultiset.create();
    }

    public PersistableListBase(int initialCapacity) {
        list = LinkedHashMultiset.create(initialCapacity);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean addAll(Collection<? extends T> ts) {
        return list.addAll(ts);
    }

    /**
     * Create a list prefilled with a given collection.
     *
     * @param items Items to add to this list.
     */
    public PersistableListBase(Collection<T> items) {
        this(items.size());
        list.addAll(items);
    }

    public Collection<T> getList() {
        return list;
    }

    public void setList(Collection<T> list) {
        this.list = list;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return list.add(t);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        PersistableListBase that = (PersistableListBase) o;

        LinkedHashMultiset<T> me = makeMultiset(this.getList());
        LinkedHashMultiset<T> other = makeMultiset(that.getList());
        return !(me != null ? !me.equals(other) : other != null);

    }

    private LinkedHashMultiset<T> makeMultiset(Collection collection) {
        if (collection == null) {
            return null;
        }
        if (collection instanceof LinkedHashMultiset) {
            return (LinkedHashMultiset<T>) collection;
        }
        return LinkedHashMultiset.create(collection);
    }

    @Override
    public int hashCode() {
        return getList() != null ? makeMultiset(this.getList()).hashCode() : 0;
    }
}
