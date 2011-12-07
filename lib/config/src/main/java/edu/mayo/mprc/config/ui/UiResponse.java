package edu.mayo.mprc.config.ui;

import edu.mayo.mprc.config.ResourceConfig;

/**
 * Allows {@link PropertyChangeListener} to respond to property change. The response can either display an error
 * in case of validation failures, or change any property's value.
 */
public interface UiResponse {
	/**
	 * Display an error message for given property. The error may contain links that fix the problem when
	 * the user requests so. These links are embedded into the string using {@link FixTag}
	 *
	 * @param config       The resource to display the error for.
	 * @param propertyName Name of the property to display the error for.
	 * @param error        Error message. If null, the error message is cleared.
	 */
	void displayPropertyError(ResourceConfig config, String propertyName, String error);

	/**
	 * Change the property value for given config.
	 *
	 * @param config       Configuration to change property for.
	 * @param propertyName Name of the property.
	 * @param newValue     New value.
	 */
	void setProperty(ResourceConfig config, String propertyName, String newValue);
}
