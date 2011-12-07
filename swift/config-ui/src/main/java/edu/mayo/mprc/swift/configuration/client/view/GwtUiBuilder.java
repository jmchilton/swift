package edu.mayo.mprc.swift.configuration.client.view;

import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.swift.configuration.client.model.*;
import edu.mayo.mprc.swift.configuration.client.validation.local.IntegerValidator;
import edu.mayo.mprc.swift.configuration.client.validation.local.RequiredFieldValidator;
import edu.mayo.mprc.swift.configuration.client.validation.local.Validator;

import java.util.Arrays;
import java.util.EventListener;

/**
 * A library of methods that create various UI components to be shared for the configuration.
 * <p/>
 * The key method is {@link #property}. Other methods modify the function of the call that is to come (e.g. required().property())
 * or they modify the property that was defined before (e.g. property().existing). The state is therefore split into two parts
 * - preceding (pertaining to the preceding property call) and future (pertaining to the property call that is to come).
 * <p/>
 * GwtUiBuilder makes a {@link PropertyList} control.
 * All controls are set up to send any change to {@link edu.mayo.mprc.swift.configuration.client.ConfigurationService#propertyChanged}.
 */
public final class GwtUiBuilder implements UiBuilderClient {
	private final ResourceModel model;
	private PropertyList propertyList;
	private int row;

	// Information about property that is currently being built.
	private String name;
	private String displayName;
	private Widget editor;
	private String description;
	private MultiValidator validator; // Validator for the current "property" call. E.g. property().fileExists()
	private String defaultValue;

	private ApplicationModel applicationModel;
	private AvailableModules configUIs;
	private Widget nativeInterface;

	private Context context;

	public GwtUiBuilder(Context context, ResourceModel model) {
		this.model = model;
		this.applicationModel = context.getApplicationModel();
		this.configUIs = context.getApplicationModel().getAvailableModules();
		this.context = context;

		this.name = null;
		this.displayName = null;
		this.editor = null;
		this.description = null;
		this.validator = null;
		this.propertyList = null;
		row = 0;
	}

	public GwtUiBuilder start() {
		propertyList = new PropertyList();
		propertyList.getColumnFormatter().addStyleName(1, "editor-column");
		editor = null;
		row = 0;
		validator = null;
		return this;
	}

	public Widget getNativeInterface() {
		return nativeInterface;
	}

	public PropertyList end() {
		if (editor != null) {
			endPropertyUi();
		}
		return propertyList;
	}

	public PropertyList getPropertyList() {
		return propertyList;
	}

	public GwtUiBuilder nativeInterface(String className) {
		if ("database" .equals(className)) {
			nativeInterface = new DatabaseView(this, model);
		} else {
			throw new RuntimeException("Unsupported native inteface: " + className);
		}
		return this;
	}

	/**
	 * A property method triggers output of the UI for the previous property (which is by now complete).
	 *
	 * @param name        New property name.
	 * @param displayName Property display name (user-friendly text)
	 * @param description Property description.
	 */
	public GwtUiBuilder property(String name, String displayName, String description) {
		if (editor != null) {
			endPropertyUi();
		}

		startPropertyUi(name, displayName, description);
		makeTextBoxEditor();

		return this;
	}

	private void makeTextBoxEditor() {
		this.editor = new TextBox();
		this.editor.addStyleName("property-text-box");
	}

	/**
	 * Starts creation of a new property UI.
	 *
	 * @param name        New property name.
	 * @param displayName New property display name.
	 * @param description New property description.
	 */
	private void startPropertyUi(String name, String displayName, String description) {
		validator = new MultiValidator(model, name);

		this.name = name;
		this.displayName = displayName;
		this.description = description;
	}

	/**
	 * Adds UI for one property. Generates a validation panel just above the property.
	 * Hooks the editor to fire a validation on change, which in turns sends the new value to the server.
	 */
	private void endPropertyUi() {
		// Add validation panel
		final ValidationPanel validationPanel = addValidationPanel(propertyList, row);
		row++;

		// Add aditor and validator (+asterisk) and editor
		String asteriskIfRequired = validator.isRequiredField() ? "<span class=\"required\">*</span>" : "";
		String displayNameHardSpaces = displayName.replaceAll(" ", "&nbsp;");
		final HTML html = new HTML(displayNameHardSpaces + asteriskIfRequired);
		html.addStyleName("property-caption");
		propertyList.setWidget(row, 0, html);
		validator.setValidationPanel(validationPanel);

		if (validator.hasOnDemandValidation()) {
			HorizontalPanel panel = createOnDemandValidationPanel(editor, validator, validationPanel);
			propertyList.setWidget(row, 1, panel);
		} else {
			propertyList.setWidget(row, 1, editor);
		}

		// Set the default value if specified - this is mostly used for the special DatabaseView module.
		if (defaultValue != null) {
			if (editor instanceof TextBox) {
				((TextBox) editor).setText(defaultValue);
			} else if (editor instanceof CheckBox) {
				((CheckBox) editor).setChecked("true" .equals(defaultValue));
			}
			defaultValue = null;
		}

		propertyList.getFlexCellFormatter().addStyleName(row, 1, "property-editor");

		addEditorValidator(editor, validator, validationPanel);
		row++;

		// Add property description under
		propertyList.getFlexCellFormatter().setColSpan(row, 0, 2);
		propertyList.setWidget(row, 0, new HTML(description));
		propertyList.getFlexCellFormatter().addStyleName(row, 0, "property-description");
		row++;

		// Register the new property with the property list
		propertyList.registerProperty(name, editor, validator);
	}

	private void addEditorValidator(final Widget editor, final MultiValidator validator, final ValidationPanel validationPanel) {
		if (editor instanceof SourcesChangeEvents) {
			((SourcesChangeEvents) editor).addChangeListener(new ChangeListener() {
				public void onChange(Widget sender) {
					validator.validate(PropertyList.getEditorValue(sender));
				}
			});
		} else if (editor instanceof SourcesClickEvents) {
			((SourcesClickEvents) editor).addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					validator.validate(PropertyList.getEditorValue(sender));
				}
			});
		} else {
			throw new RuntimeException("Unsupported type of editor: " + editor.getClass().getName());
		}
	}

	/**
	 * @param editor          Value editor
	 * @param validator       Validator for the editor
	 * @param validationPanel Validation panel to display validation results
	 * @return Validation panel consisting of the editor and "Test" button that runs the on-demand validation.
	 */
	private HorizontalPanel createOnDemandValidationPanel(final Widget editor, final MultiValidator validator, final ValidationPanel validationPanel) {
		HorizontalPanel panel = new HorizontalPanel();
		panel.add(this.editor);

		final Button test = new Button("Test");
		panel.add(test);

		final HTML testProgress = new HTML("&nbsp;");
		testProgress.setVisible(false);
		testProgress.addStyleName("progress");
		panel.add(testProgress);

		final HTML successIndicator = new HTML("&nbsp;");
		successIndicator.setVisible(false);
		successIndicator.addStyleName("successIndicator");
		panel.add(successIndicator);

		validationPanel.setTestButton(test);
		validationPanel.setTestProgress(testProgress);
		validationPanel.setSuccessIndicator(successIndicator);
		test.addClickListener(new ClickListener() {
			public void onClick(Widget widget) {
				validator.runOnDemandValidation(PropertyList.getEditorValue(editor), validationPanel);
			}
		});
		return panel;
	}

	public Widget getPrecedingEditor() {
		return editor;
	}

	/**
	 * Adds change listener to the preceeding property. If the preceeding property is
	 * represented by a TextBox, the listner must be of the type ChangeListener. If the
	 * preceeding property is represented by a CheckBox, the listner must be of the type ClickListener.
	 */
	public GwtUiBuilder addEventListener(EventListener listener) {
		if (this.editor instanceof TextBox) {
			if (!(listener instanceof ChangeListener)) {
				throw new RuntimeException("The event listener for TextBox must be a change listener");
			}
			((TextBox) this.editor).addChangeListener((ChangeListener) listener);
		} else if (this.editor instanceof CheckBox) {
			if (!(listener instanceof ClickListener)) {
				throw new RuntimeException("The event listener for CheckBox must be a click listener");
			}
			((CheckBox) this.editor).addClickListener((ClickListener) listener);
		}
		return this;
	}

	/**
	 * Removes change listener from the preceeding property. If the preceeding property is
	 * represented by a TextBox, the listner must be of the type ChangeListener. If the
	 * preceeding property is represented by a CheckBox, the listner must be of the type ClickListener.
	 */
	public GwtUiBuilder removeEventListener(EventListener listener) {
		if (this.editor instanceof TextBox) {
			if (!(listener instanceof ChangeListener)) {
				throw new RuntimeException("The event listener for TextBox must be a change listener");
			}
			((TextBox) this.editor).removeChangeListener((ChangeListener) listener);
		} else if (this.editor instanceof CheckBox) {
			if (!(listener instanceof ClickListener)) {
				throw new RuntimeException("The event listener for CheckBox must be a click listener");
			}
			((CheckBox) this.editor).removeClickListener((ClickListener) listener);
		}
		return this;
	}

	public GwtUiBuilder required() {
		this.validator.addValidator(new RequiredFieldValidator());
		return this;
	}

	/**
	 * Default values are displayed in the fields. The user is still responsible
	 * to set the default values in the {@link ResourceModel} objects, otherwise
	 * the default might be overriden on load.
	 *
	 * @param value Default value for the current property.
	 */
	public GwtUiBuilder defaultValue(String value) {
		this.defaultValue = value;
		return this;
	}

	public GwtUiBuilder validateOnDemand() {
		this.validator.addOnDemandValidator();
		return this;
	}

	private ValidationPanel addValidationPanel(FlexTable flexTable, int nextTableRow) {
		ValidationPanel validationMessagePanel = new ValidationPanel();

		flexTable.setWidget(nextTableRow, 0, validationMessagePanel);
		flexTable.getFlexCellFormatter().setColSpan(nextTableRow, 0, 3);
		return validationMessagePanel;
	}


	public GwtUiBuilder validator(Validator validator) {
		this.validator.addValidator(validator);
		return this;
	}

	public GwtUiBuilder boolValue() {
		editor = new CheckBox();
		return this;
	}

	public GwtUiBuilder integerValue(Integer minimum, Integer maximum) {
		IntegerValidator v = new IntegerValidator(minimum, maximum);
		validator.addValidator(v);
		return this;
	}


	public GwtUiBuilder reference(String... type) {
		this.editor = new ReferenceListBox(Arrays.asList(type), applicationModel, context);
		return this;
	}


	public GwtUiBuilder enable(final String propertyName, final boolean synchronous) {
		if (this.editor instanceof CheckBox) {
			final CheckBox checkBox = (CheckBox) editor;
			checkBox.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {

					if (sender instanceof CheckBox) {
						final CheckBox checkBox = (CheckBox) sender;
						boolean enableLink = checkBox.isChecked() == synchronous;
						final Widget editor = propertyList.getWidgetForName(propertyName);
						if (editor != null && editor instanceof FocusWidget) {
							((FocusWidget) editor).setEnabled(enableLink);
						} else {
							throw new RuntimeException("The property " + propertyName + " does not correspond to an editor that can be enabled/disabled.");
						}
					}
				}
			});
		} else {
			throw new RuntimeException("Only a checkbox can be set to enable/disable other properties");
		}
		return this;
	}

}
