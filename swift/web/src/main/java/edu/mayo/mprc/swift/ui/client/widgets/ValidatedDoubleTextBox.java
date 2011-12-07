package edu.mayo.mprc.swift.ui.client.widgets;

public final class ValidatedDoubleTextBox extends ValidatedTextBox {
	private static class DoubleValidator extends TextBoxValidator {
		private double low;
		private double high;

		public DoubleValidator(double low, double high) {
			this.low = low;
			this.high = high;
		}

		@Override
		public boolean isValueValid(String data) {
			double val;

			try {
				val = Double.parseDouble(data);
			} catch (NumberFormatException e) {
				setLastError("Value must be a number.");
				return false;
			}

			if (val < low || val > high) {
				setLastError("Value must be from " + low + " to " + high + ".");
				return false;
			} else {
				setLastError(null);
				return true;
			}
		}
	}

	private double low;
	private double high;
	private double defaultValue;

	public ValidatedDoubleTextBox(double low, double high, double defaultValue) {
		super(new DoubleValidator(low, high));
		this.low = low;
		this.high = high;
		this.defaultValue = defaultValue;
	}

	/**
	 * Get the lowest value this input will accept
	 *
	 * @return The lowest value this input will accept
	 */
	public final double getLow() {
		return low;
	}

	/**
	 * Get the highest value this input will accept
	 *
	 * @return The highest value this input will accept
	 */
	public final double getHigh() {
		return high;
	}

	public final double getDoubleValue() {
		try {
			return Double.valueOf(getText());
		} catch (NumberFormatException ignore) {
			// SWALLOWED
			return defaultValue;
		}
	}
}