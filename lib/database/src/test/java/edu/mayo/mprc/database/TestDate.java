package edu.mayo.mprc.database;

import org.joda.time.DateTime;

/**
 * A date holder.
 *
 * @author Roman Zenka
 */
public class TestDate extends PersistableBase {
	private DateTime value1;
	private DateTime value2;

	public TestDate() {
	}

	public TestDate(DateTime value1, DateTime value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	public DateTime getValue1() {
		return value1;
	}

	public void setValue1(DateTime value1) {
		this.value1 = value1;
	}

	public DateTime getValue2() {
		return value2;
	}

	public void setValue2(DateTime value2) {
		this.value2 = value2;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TestDate testDate = (TestDate) o;

		if (value1 != null ? !value1.equals(testDate.value1) : testDate.value1 != null) return false;
		if (value2 != null ? !value2.equals(testDate.value2) : testDate.value2 != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = value1 != null ? value1.hashCode() : 0;
		result = 31 * result + (value2 != null ? value2.hashCode() : 0);
		return result;
	}
}