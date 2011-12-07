package edu.mayo.mprc.config.ui;

import edu.mayo.mprc.config.ResourceConfig;

/**
 * Listens to property changes on a given config element.
 */
public interface PropertyChangeListener {
	/**
	 * A property just changed.
	 *
	 * @param config              This is the resource configuration the property belongs to.
	 * @param propertyName        Name of the property that changed.
	 * @param newValue            New value of the property.
	 * @param response            Should the user interface react somehow to this change?
	 *                            When producing error messages, a special {@link FixTag} can be embedded that
	 *                            allows the user to click a "Fix me" link.
	 *                            This results in calling the {@link #fixError} method.
	 * @param validationRequested The property change was triggered by the user explicitly requesting validation
	 *                            by pressing the "Test" button for this property. Do a more extensive (and potentially slow)
	 *                            validation.
	 */
	void propertyChanged(ResourceConfig config, String propertyName, String newValue, UiResponse response, boolean validationRequested);

	/**
	 * The user wants to fix an error that was previously reported for the property.
	 *
	 * @param config       The resource to fix.
	 * @param propertyName The resource property to fix.
	 * @param action       Action provided to {@link FixTag} in the {@link #propertyChanged} validation step. Allows you
	 *                     to distinguish between different ways of fixing an error.
	 */
	void fixError(ResourceConfig config, String propertyName, String action);
}
