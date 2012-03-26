package edu.mayo.mprc.swift.configuration.client.view;

import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.swift.configuration.client.model.*;

import java.util.List;

/**
 * References another resource.
 * <p/>
 * The user can specify a list of types, only resources of those types will be listed.
 * <p/>
 * The returned value is either an id of the resource, or an id of the service that wraps a module (if the referenced
 * object is a module).
 */
public final class ReferenceListBox extends SimplePanel implements SourcesChangeEvents {
	private List<String> types;
	private ApplicationModel model;
	private ListBox listBox;
	private Button createNew;
	private final HorizontalPanel panel = new HorizontalPanel();
	private final Context errorDisplay;
	private DelegatingChangeListenerCollection changeListeners;

	public ReferenceListBox(final List<String> types, final ApplicationModel model, final Context errorDisplay) {
		this.errorDisplay = errorDisplay;
		this.types = types;
		this.model = model;

		addListBox(types);
		addCreateNewButton();
		attachToModel(model);
		this.add(panel);
	}

	private void addCreateNewButton() {
		this.createNew = new Button("Add new...");
		this.createNew.addClickListener(new ClickListener() {
			public void onClick(final Widget widget) {
				// User wants a new item of this type
				final AddNewModuleDialog dialog = new AddNewModuleDialog(model, types, new NewModuleCreatedCallback() {
					public void newModuleCreated(final ResourceModel model) {
						setValue(model);
						fireChange();
					}
				}, errorDisplay);
				dialog.setPopupPosition(widget.getAbsoluteLeft(), widget.getAbsoluteTop() + widget.getOffsetHeight());

				if (!dialog.skipDisplay()) {
					dialog.show();
				}
			}
		});
		panel.add(createNew);
	}

	private void fireChange() {
		if (changeListeners != null) {
			changeListeners.fireChange(this);
		}
	}

	private void addListBox(final List<String> types) {
		this.listBox = new ListBox();
		panel.add(listBox);

		for (final String type : types) {
			if (type.equals(UiBuilderClient.NONE_TYPE)) {
				listBox.addItem(UiBuilderClient.NONE_TYPE, UiBuilderClient.NONE_TYPE);
				break;
			}
		}
	}

	public String getValue() {
		if (listBox.getSelectedIndex() == -1) {
			return null;
		} else if (UiBuilderClient.NONE_TYPE.equals(listBox.getValue(listBox.getSelectedIndex()))) {
			return null;
		}

		return listBox.getValue(listBox.getSelectedIndex());
	}

	public void setValue(final ResourceModel model) {
		if (model instanceof ModuleModel) {
			final ModuleModel moduleModel = (ModuleModel) model;
			setValue(moduleModel.getService().getId());
		} else {
			setValue(model.getId());
		}
	}

	public void setValue(final String value) {
		for (int i = 0; i < listBox.getItemCount(); i++) {
			final String myValue = listBox.getValue(i);
			if (myValue.equals(value) || (value == null && UiBuilderClient.NONE_TYPE.equals(myValue))) {
				listBox.setSelectedIndex(i);
				break;
			}
		}
	}

	public void attachToModel(final ApplicationModel model) {
		this.model = model;
		model.addListener(new MyApplicationModelListener());
		for (final DaemonModel daemonModel : model.getDaemons()) {
			daemonModel.addListener(daemonModelListener);
			for (final ResourceModel module : daemonModel.getChildren()) {
				addResourceModel(module);
			}
		}
	}

	private static boolean hasType(final String type, final List<String> types) {
		for (final String ty : types) {
			if (ty.equals(type)) {
				return true;
			}
		}
		return false;
	}

	private void addResourceModel(final ResourceModel resource) {
		if (hasType(resource.getType(), types)) {
			if (resource instanceof ModuleModel) {
				final ModuleModel moduleModel = (ModuleModel) resource;
				final String name = getModuleName(moduleModel);
				listBox.addItem(name, moduleModel.getService().getId());
			} else {
				listBox.addItem(resource.getName(), resource.getId());
			}
		}
	}

	public static String getModuleName(final ModuleModel moduleModel) {
		return moduleModel.getName();
	}

	private String getResourceId(final ResourceModel resource) {
		if (resource instanceof ModuleModel) {
			final ModuleModel moduleModel = (ModuleModel) resource;
			return moduleModel.getService().getId();
		}
		return resource.getId();
	}

	private MyDaemonModelListener daemonModelListener = new MyDaemonModelListener();

	public void removeModule(final ResourceModel child) {
		if (hasType(child.getType(), types)) {
			final String resourceId = getResourceId(child);
			for (int i = 0; i < listBox.getItemCount(); i++) {
				if (listBox.getValue(i).equals(resourceId)) {
					listBox.removeItem(i);
					break;
				}
			}
		}
	}

	// Delegate the change listener to the embedded listbox
	public void addChangeListener(final ChangeListener changeListener) {
		if (changeListeners == null) {
			changeListeners = new DelegatingChangeListenerCollection(this, listBox);
		}
		changeListeners.add(changeListener);
	}

	// Delegate the change listener to the embedded listbox
	public void removeChangeListener(final ChangeListener changeListener) {
		if (changeListeners != null) {
			changeListeners.remove(changeListener);
		}
	}

	private class MyApplicationModelListener implements ResourceModelListener {
		public void initialized(final ResourceModel model) {
		}

		public void nameChanged(final ResourceModel model) {
		}

		public void childAdded(final ResourceModel child, final ResourceModel addedTo) {
			addedTo.addListener(daemonModelListener);
		}

		public void childRemoved(final ResourceModel child, final ResourceModel removedFrom) {
			// Remove all children
			for (final ResourceModel model : child.getChildren()) {
				removeModule(model);
			}
			removedFrom.removeListener(daemonModelListener);
		}

		public void propertyChanged(final ResourceModel model, final String propertyName, final String newValue) {
		}
	}

	private class MyDaemonModelListener implements ResourceModelListener {
		public void initialized(final ResourceModel model) {
		}

		public void nameChanged(final ResourceModel model) {
		}

		public void childAdded(final ResourceModel child, final ResourceModel addedTo) {
			addResourceModel(child);
		}

		public void childRemoved(final ResourceModel child, final ResourceModel removedFrom) {
			removeModule(child);
		}

		public void propertyChanged(final ResourceModel model, final String propertyName, final String newValue) {

		}

	}
}
