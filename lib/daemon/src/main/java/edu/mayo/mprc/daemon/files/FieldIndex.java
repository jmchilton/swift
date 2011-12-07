package edu.mayo.mprc.daemon.files;

import java.io.Serializable;

final class FieldIndex implements Serializable {
	private static final long serialVersionUID = 20110406L;
	private final String field;
	private final Serializable index;

	FieldIndex(String field, Serializable index) {
		this.field = field;
		this.index = index;
	}

	public String getField() {
		return field;
	}

	public Serializable getIndex() {
		return index;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof FieldIndex)) {
			return false;
		}

		FieldIndex that = (FieldIndex) o;

		if (field != null ? !field.equals(that.field) : that.field != null) {
			return false;
		}
		if (index != null ? !index.equals(that.index) : that.index != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = field != null ? field.hashCode() : 0;
		result = 31 * result + (index != null ? index.hashCode() : 0);
		return result;
	}
}
