package edu.mayo.mprc.database;

import java.util.*;

/**
 * Base for classes that are nothing but a list to be persisted.
 * The list should be reasonably small for this to work well.
 *
 * @author Roman Zenka
 */
public abstract class PersistableListBase<T extends PersistableBase> extends PersistableBase implements List<T> {
    private List<T> list;

    public PersistableListBase() {
        list = new ArrayList<T>();
    }

    public PersistableListBase(int initialCapacity) {
        list = new ArrayList<T>(initialCapacity);
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

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
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
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
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
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
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
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public T set(int index, T element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        list.add(index, element);
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return list.addAll(index, c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersistableListBase that = (PersistableListBase) o;

        if (getList() != null ? !getList().equals(that.getList()) : that.getList() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getList() != null ? getList().hashCode() : 0;
    }
}
