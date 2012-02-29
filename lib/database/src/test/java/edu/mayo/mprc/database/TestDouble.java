package edu.mayo.mprc.database;

/**
 * A double holder.
 *
 * @author Roman Zenka
 */
public class TestDouble extends PersistableBase {
	private double value1;
	private double value2;

	public TestDouble() {
	}

	public TestDouble(double value1, double value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	public double getValue1() {
		return value1;
	}

	public void setValue1(double value1) {
		this.value1 = value1;
	}

	public double getValue2() {
		return value2;
	}

	public void setValue2(double value2) {
		this.value2 = value2;
	}
}
