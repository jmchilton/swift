package edu.mayo.mprc.swift.params2;

import edu.mayo.mprc.MprcException;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Mass tolerance.
 * <p/>
 * Unit is a simple string to keep things simple.
 */
public class Tolerance implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private double value;
	private MassUnit unit;
	private static final DecimalFormat df;

	static {
		df = new DecimalFormat("0.########");
		df.setMinimumFractionDigits(0);
		df.setDecimalSeparatorAlwaysShown(false);
	}

	public Tolerance() {
	}

	public Tolerance(final double value, final MassUnit unit) {
		this.value = value;
		this.unit = unit;
	}

	/**
	 * Create tolerance from its textual representation.
	 *
	 * @param text Textual representation, e.g. "0.8 Da" or "5 ppm"
	 */
	public Tolerance(final String text) {
		final String trimmed = text.trim();
		final String lower = trimmed.toLowerCase(Locale.ENGLISH);
		for (final MassUnit massUnit : MassUnit.values()) {
			if (lower.length() > massUnit.getCode().length() && lower.endsWith(massUnit.getCode().toLowerCase(Locale.ENGLISH))) {
				final String number = trimmed.substring(0, trimmed.length() - massUnit.getCode().length());
				try {
					value = Double.parseDouble(number);
					unit = massUnit;
					return;
				} catch (NumberFormatException e) {
					throw new MprcException("Bad format: '" + number.trim() + "' should be amount of " + massUnit.getDescription(), e);
				}
			}
		}
		throw new MprcException("Unrecognized unit, please use one of " + MassUnit.getOptions());
	}

	void setValue(final double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	void setUnit(final MassUnit unit) {
		this.unit = unit;
	}

	public MassUnit getUnit() {
		return unit;
	}

	@Override
	public String toString() {
		return df.format(value) + " " + unit.getCode();
	}

	public Tolerance copy() {
		return new Tolerance(getValue(), getUnit());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		final Tolerance tolerance = (Tolerance) obj;

		if (Double.compare(tolerance.value, value) != 0) {
			return false;
		}
		return unit == tolerance.unit;
	}

	@Override
	public int hashCode() {
		int result;
		final long temp;
		temp = value == +0.0d ? 0L : Double.doubleToLongBits(value);
		result = (int) (temp ^ (temp >>> 32));
		result = 31 * result + (unit != null ? unit.hashCode() : 0);
		return result;
	}
}

