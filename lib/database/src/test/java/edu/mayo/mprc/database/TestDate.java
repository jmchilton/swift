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
}
