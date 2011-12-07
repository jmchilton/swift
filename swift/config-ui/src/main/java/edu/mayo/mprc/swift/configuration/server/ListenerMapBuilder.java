package edu.mayo.mprc.swift.configuration.server;

import edu.mayo.mprc.config.ui.PropertyChangeListener;

/**
 * Each property can have a listener that is notified of the property changes. These listeners are stored
 * in a map, which can be created using this interface.
 */
public interface ListenerMapBuilder {
	/**
	 * Sets a listener for a given property. Only one listener per property is allowed.
	 *
	 * @param propertyName Name of the property.
	 * @param listener     Listener to be called when the property changes.
	 */
	void setListener(String propertyName, PropertyChangeListener listener);

	/**
	 * Sets a listener for the daemon change. Each property belongs to a module, that lives within a daemon.
	 * If the daemon is migrated for instance from Linux to Windows, this change can cause a ripple effect in all modules
	 * (for instance the paths slashes flip to backslashes, executables default to their windows form, etc). A
	 * daemon listener allows this behavior to happen.
	 *
	 * @param listener Listener to be called when parent daemon changes.
	 */
	void setDaemonListener(PropertyChangeListener listener);
}
