package edu.mayo.mprc.swift.configuration.client.model;

import java.io.Serializable;

/**
 * Description of how to modify the UI.
 */
public interface UiChanges extends Serializable {
	void setProperty(String resourceId, String propertyName, String newValue);

	/**
	 * If error is null, all property errors are cleared.
	 *
	 * @param resourceId
	 * @param propertyName
	 * @param error
	 */
	void displayPropertyError(String resourceId, String propertyName, String error);
}
