package edu.mayo.mprc.utilities;

import java.io.Serializable;

public final class Tuple<S extends Comparable<S> & Serializable, T extends Comparable<T> & Serializable> implements Comparable<Tuple<S, T>>, Serializable {
	private static final long serialVersionUID = 20100912L;
	private final S first;
	private final T second;

	public Tuple(S first, T second) {
		this.first = first;
		this.second = second;
	}

	public S getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Tuple)) {
			return false;
		}

		Tuple tuple = (Tuple) o;

		if (first != null ? !first.equals(tuple.first) : tuple.first != null) {
			return false;
		}
		return !(second != null ? !second.equals(tuple.second) : tuple.second != null);
	}

	public int hashCode() {
		int result;
		result = (first != null ? first.hashCode() : 0);
		result = 31 * result + (second != null ? second.hashCode() : 0);
		return result;
	}


	/**
	 * Compare two objects, handle nulls correctly.
	 */
	public int compareWithNulls(Comparable o1, Object o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			} else {
				return -1;
			}
		}
		if (o2 == null) {
			return 1;
		}
		// o1 and o2 are not null
		return o1.compareTo(o2);
	}

	public int compareTo(Tuple<S, T> o) {
		int c1 = compareWithNulls(this.first, o.first);
		if (c1 == 0) {
			return compareWithNulls(this.second, o.second);
		}
		return c1;
	}
}
