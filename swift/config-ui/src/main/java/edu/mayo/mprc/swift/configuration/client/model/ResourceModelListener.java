package edu.mayo.mprc.swift.configuration.client.model;

/**
 * Notifies listeners about changes of the resource.
 */
public interface ResourceModelListener {
	/**
	 * Called when the model is freshly initialized (call of setProperty).
	 */
	void initialized(ResourceModel model);

	/**
	 * Called whenever the resource name changes.
	 */
	void nameChanged(ResourceModel model);

	/**
	 * Called whenever a new child is added.
	 */
	void childAdded(ResourceModel child, ResourceModel addedTo);

	/**
	 * Called whenever a child is removed.
	 */
	void childRemoved(ResourceModel child, ResourceModel removedFrom);

	/**
	 * Called whenever any property changes.
	 */
	void propertyChanged(ResourceModel model, String propertyName, String newValue);
}
