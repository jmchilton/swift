package edu.mayo.mprc.swift.ui.client.widgets.validation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.swift.ui.client.rpc.ClientValue;

/**
 * An abstract check box that can validate the users's input server side in conjuction with a ValidationController
 */
public abstract class ValidatableTextBox extends TextBox implements Validatable {
	private ChangeListenerCollection listeners;
	private String param;

	public ValidatableTextBox(final String param) {
		super();
		this.param = param;
		listeners = new ChangeListenerCollection();
		this.addKeyboardListener(
				new KeyboardListenerAdapter() {
					public void onKeyUp(final Widget widget, final char c, final int i) {
						if (c == KEY_ENTER) {
							listeners.fireChange(widget);
						}
					}
				}
		);
		super.addChangeListener(new ChangeListener() {
			public void onChange(final Widget widget) {
				listeners.fireChange(widget);
			}
		});
		addFocusListener(new FocusListener() {
			public void onFocus(final Widget widget) {

			}

			public void onLostFocus(final Widget widget) {
				GWT.log(getParam() + " lost focus", null);
			}
		});
	}

	public String getParam() {
		return param;
	}

	public ClientValue getClientValue() {
		return getValueFromString(getText());
	}

	protected abstract ClientValue getValueFromString(String value);

	public void setValue(final ClientValue value) {
		if (value == null) {
			return;
		}
		setText(setValueAsString(value));
	}

	protected abstract String setValueAsString(ClientValue object);

	public void focus() {
		setFocus(true);
	}

	public void addChangeListener(final ChangeListener changeListener) {
		listeners.add(changeListener);
	}

	public void removeChangeListener(final ChangeListener changeListener) {
		listeners.remove(changeListener);
	}

	public void setValidationSeverity(final int validationSeverity) {
		ValidationController.setValidationSeverity(validationSeverity, this);
	}
}
