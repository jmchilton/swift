package edu.mayo.mprc.swift.configuration.client.view;

import java.util.HashMap;
import java.util.Map;

/**
 * UI elements that can save and load their values implement this.
 */
public interface UiSerialization {
	/**
	 * Load the config into the UI after a change
	 */
	void loadUI(Map<String, String> values);

	/**
	 * Save the current config from UI.
	 */
	HashMap<String, String> saveUI();

}
