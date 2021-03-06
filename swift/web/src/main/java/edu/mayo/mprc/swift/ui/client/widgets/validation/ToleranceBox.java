package edu.mayo.mprc.swift.ui.client.widgets.validation;

import edu.mayo.mprc.swift.ui.client.rpc.ClientTolerance;
import edu.mayo.mprc.swift.ui.client.rpc.ClientValue;

import java.util.List;

/**
 * Displays/edits a Tolerance.
 */
public final class ToleranceBox extends ValidatableTextBox {

	public ToleranceBox(String param) {
		super(param);
		setVisibleLength(8);
	}

	protected ClientValue getValueFromString(String value) {
		if ((value == null) || (value.length() == 0)) {
			return null;
		}
		return new ClientTolerance(value);

	}

	protected String setValueAsString(ClientValue object) {
		ClientTolerance du = (ClientTolerance) object;
		return du.getValue();
	}

	public void updateIfHasFocus() {
		// ignore.
	}

	public void setAllowedValues(List<? extends ClientValue> values) {
		// ignore.
	}

	public String getAllowedValuesParam() {
		return null; // no allowed values.
	}
}
