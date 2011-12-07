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

	public ValidatableTextBox(String param) {
		super();
		this.param = param;
		listeners = new ChangeListenerCollection();
		this.addKeyboardListener(
				new KeyboardListenerAdapter() {
					public void onKeyUp(Widget widget, char c, int i) {
						if (c == KEY_ENTER) {
							listeners.fireChange(widget);
						}
					}
				}
		);
		super.addChangeListener(new ChangeListener() {
			public void onChange(Widget widget) {
				listeners.fireChange(widget);
			}
		});
		addFocusListener(new FocusListener() {
			public void onFocus(Widget widget) {

			}

			public void onLostFocus(Widget widget) {
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

	public void setValue(ClientValue value) {
		if (value == null) {
			return;
		}
		setText(setValueAsString(value));
	}

	protected abstract String setValueAsString(ClientValue object);

	public void focus() {
		setFocus(true);
	}

	public void addChangeListener(ChangeListener changeListener) {
		listeners.add(changeListener);
	}

	public void removeChangeListener(ChangeListener changeListener) {
		listeners.remove(changeListener);
	}

	public void setValidationSeverity(int validationSeverity) {
		ValidationController.setValidationSeverity(validationSeverity, this);
	}
}
