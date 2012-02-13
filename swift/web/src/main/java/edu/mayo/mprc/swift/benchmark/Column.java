package edu.mayo.mprc.swift.benchmark;

import java.util.ArrayList;

final class Column implements Comparable<Column> {

	private String title;
	private ArrayList<String> data = new ArrayList<String>(10);

	public Column(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void addData(String data) {
		this.data.add(data);
	}

	public String getData(int index) {
		if (index < data.size()) {
			return data.get(index);
		} else {
			return "";
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Column column = (Column) o;

		if (title != null ? !title.equals(column.title) : column.title != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return title != null ? title.hashCode() : 0;
	}

	@Override
	public int compareTo(Column o) {
		return this.getTitle().compareTo(o.getTitle());
	}
}
