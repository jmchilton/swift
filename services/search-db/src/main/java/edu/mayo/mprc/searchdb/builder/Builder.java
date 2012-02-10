package edu.mayo.mprc.searchdb.builder;

/**
 * For building immutable objects and their proper hashCode and equals handling while
 * they are in flux.
 *
 * @author Roman Zenka
 */
public interface Builder<T> {
    /**
     * @return An instance of T the builder created. Should be called only once.
     */
    T build();
}
