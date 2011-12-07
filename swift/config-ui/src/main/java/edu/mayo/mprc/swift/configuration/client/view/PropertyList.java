package edu.mayo.mprc.swift.configuration.client.view;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import edu.mayo.mprc.swift.configuration.client.validation.local.Validator;

import java.util.HashMap;
import java.util.Map;

public final class PropertyList extends FlexTable implements ModuleView {
	private Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();

	public void loadUI(Map<String, String> values) {
		if (values == null) {
			return;
		}
		for (PropertyDefinition prop : properties.values()) {
			String value = values.get(prop.name);
			if (value != null) {
				setEditorValue(prop.editor, value);
			}
		}
	}

	public static String getEditorValue(Widget editor) {
		String value;
		if (editor instanceof TextBox) {
			value = ((TextBox) editor).getText().trim();
		} else if (editor instanceof CheckBox) {
			value = ((CheckBox) editor).isChecked() ? "true" : "false";
		} else if (editor instanceof ReferenceListBox) {
			value = ((ReferenceListBox) editor).getValue();
		} else {
			throw new RuntimeException("Unsupported editor type " + editor.getClass().getName());
		}
		return value;
	}

	public static void setEditorValue(Widget editor, String value) {
		if (editor instanceof TextBox) {
			((TextBox) editor).setText(value);
		} else if (editor instanceof CheckBox) {
			((CheckBox) editor).setChecked("true".equals(value));
		} else if (editor instanceof ReferenceListBox) {
			((ReferenceListBox) editor).setValue(value);
		} else {
			throw new RuntimeException("Unsupported editor type " + editor.getClass().getName());
		}
	}

	public HashMap<String, String> saveUI() {
		HashMap<String, String> map = new HashMap<String, String>();
		for (PropertyDefinition prop : properties.values()) {
			String value = getEditorValue(prop.editor);
			map.put(prop.name, value);
		}
		return map;
	}

	public void registerProperty(String name, Widget editor, Validator validator) {
		properties.put(name, new PropertyDefinition(name, editor, validator));
	}

	/**
	 * @param name Name of property.
	 * @return The editor for this property.
	 */
	public Widget getWidgetForName(String name) {
		final PropertyDefinition propertyDefinition = properties.get(name);
		return propertyDefinition != null ? propertyDefinition.editor : null;
	}

	public void fireValidations() {
		for (PropertyDefinition definition : properties.values()) {
			definition.validator.validate(getPropertyValue(definition.name));
		}
	}

	public void setPropertyValue(String propertyName, String value) {
		final Widget editor = getWidgetForName(propertyName);
		setEditorValue(editor, value);
	}

	public String getPropertyValue(String propertyName) {
		final Widget editor = getWidgetForName(propertyName);
		return getEditorValue(editor);
	}

	public Widget getModuleWidget() {
		return this;
	}

	private static final class PropertyDefinition {
		private PropertyDefinition(String name, Widget editor, Validator validator) {
			this.name = name;
			this.editor = editor;
			this.validator = validator;
		}

		public final String name;
		public final Widget editor;
		public final Validator validator;
	}
}
