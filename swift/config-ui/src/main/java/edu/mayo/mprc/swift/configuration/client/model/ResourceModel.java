package edu.mayo.mprc.swift.configuration.client.model;

import edu.mayo.mprc.swift.configuration.client.view.UiBuilderReplayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ResourceModel implements Serializable {
	private static int count = 0;
	public static final String NAME = "_name";
	public static final String TYPE = "_type";
	private static final long serialVersionUID = -3201869140244160793L;
	private String id;
	private HashMap<String, String> properties = new HashMap<String, String>();
	private ArrayList<ResourceModel> children = new ArrayList<ResourceModel>();
	private ResourceModel parent;
	private UiBuilderReplayer replayer;

	private ArrayList<ResourceModelListener> modelListeners = new ArrayList<ResourceModelListener>();

	public ResourceModel() {
	}

	public ResourceModel(String name, String type) {
		this.properties.put(NAME, name);
		this.properties.put(TYPE, type);
		count++;
		this.id = type + count;
	}

	public ResourceModel(String id, String name, String type) {
		this.id = id;
		this.properties.put(NAME, name);
		this.properties.put(TYPE, type);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return getProperty(NAME);
	}

	public void setName(String name) {
		setProperty(NAME, name);
		fireNameChanged();
	}

	public String getType() {
		return getProperty(TYPE);
	}

	public void setType(String type) {
		setProperty(TYPE, type);
	}

	public HashMap<String, String> getProperties() {
		return properties;
	}

	public void addListener(ResourceModelListener listener) {
		modelListeners.add(listener);
	}

	public void removeListener(ResourceModelListener listener) {
		modelListeners.remove(listener);
	}

	/**
	 * Bulk set of all properties. Does not fire property change events.
	 *
	 * @param properties New set of properties.
	 */
	public void setProperties(HashMap<String, String> properties) {
		final String name = getName();
		final String type = getType();
		this.properties = properties;
		this.properties.put(NAME, name);
		this.properties.put(TYPE, type);
		fireInitialized();
	}

	public String getProperty(String name) {
		return this.properties.get(name);
	}

	public void setProperty(String name, String value) {
		String oldValue = this.properties.get(name);
		this.properties.put(name, value);

		if (!valuesEqual(value, oldValue)) {
			firePropertyChange(name, value);
		}
	}

	private static boolean valuesEqual(String value, String oldValue) {
		return oldValue == value || (oldValue != null && oldValue.equals(value));
	}

	public UiBuilderReplayer getReplayer() {
		return replayer;
	}

	public void setReplayer(UiBuilderReplayer replayer) {
		this.replayer = replayer;
	}

	public ResourceModel getParent() {
		return parent;
	}

	public void setParent(ResourceModel parent) {
		this.parent = parent;
	}

	public ArrayList<ResourceModel> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<ResourceModel> children) {
		this.children = children;
	}

	public void addChild(ResourceModel resourceModel) {
		resourceModel.setParent(this);
		children.add(resourceModel);
		fireChildAdded(resourceModel);
	}

	public void removeChild(ResourceModel resourceModel) {
		children.remove(resourceModel);
		resourceModel.setParent(null);
		fireChildRemoved(resourceModel);
	}

	public void fireInitialized() {
		ArrayList<ResourceModelListener> modelListenersCopy = new ArrayList<ResourceModelListener>(modelListeners);
		for (ResourceModelListener listener : modelListenersCopy) {
			listener.initialized(this);
		}
	}

	public void fireNameChanged() {
		ArrayList<ResourceModelListener> modelListenersCopy = new ArrayList<ResourceModelListener>(modelListeners);
		for (ResourceModelListener listener : modelListenersCopy) {
			listener.nameChanged(this);
		}
	}

	public void fireChildAdded(ResourceModel model) {
		ArrayList<ResourceModelListener> modelListenersCopy = new ArrayList<ResourceModelListener>(modelListeners);
		for (ResourceModelListener listener : modelListenersCopy) {
			listener.childAdded(model, this);
		}
	}

	public void fireChildRemoved(ResourceModel model) {
		ArrayList<ResourceModelListener> modelListenersCopy = new ArrayList<ResourceModelListener>(modelListeners);
		for (ResourceModelListener listener : modelListenersCopy) {
			try {
				listener.childRemoved(model, this);
			} catch (Exception ignore) {
				// Keep going
			}
		}
	}

	public void firePropertyChange(String propertyName, String newValue) {
		ArrayList<ResourceModelListener> modelListenersCopy = new ArrayList<ResourceModelListener>(modelListeners);
		for (ResourceModelListener listener : modelListenersCopy) {
			listener.propertyChanged(this, propertyName, newValue);
		}
	}


}
