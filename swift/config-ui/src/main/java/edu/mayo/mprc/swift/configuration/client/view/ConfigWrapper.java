package edu.mayo.mprc.swift.configuration.client.view;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.swift.configuration.client.ConfigurationService;
import edu.mayo.mprc.swift.configuration.client.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Main panel holding all the configuration for all daemons and modules.
 */
public final class ConfigWrapper extends SimplePanel {
	private ApplicationModel model;
	private PushButton newDaemonButton;
	private Map<DaemonModel, DaemonUi> daemonMap = new HashMap<DaemonModel, DaemonUi>();
	private Tree configTree;
	private TreeItem daemonsItem;
	private SimplePanel uiPanel;
	private SimplePanel runnerPanel;
	private TreeItem lastItemSelected = null;
	private Context context;

	public ConfigWrapper(final Context context) {
		this.context = context;
		FlowPanel mainPanel = new FlowPanel();
		configTree = new Tree();
		configTree.setStyleName("config-tree");

		HorizontalPanel daemonsPanel = new HorizontalPanel();
		daemonsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		daemonsPanel.add(new Label("Daemons"));

		this.newDaemonButton = new PushButton("Add new");
		this.newDaemonButton.addStyleName("tree-item-pusbutton");
		this.newDaemonButton.addClickListener(new ClickListener() {
			public void onClick(Widget widget) {
				ConfigurationService.App.getInstance().createChild(model.getId(), "daemon", new AsyncCallback<ResourceModel>() {
					public void onFailure(Throwable throwable) {
						context.displayErrorMessage("Cannot create new daemon", throwable);
					}

					public void onSuccess(ResourceModel model) {
						if (model instanceof DaemonModel) {
							ConfigWrapper.this.context.getApplicationModel().addDaemon((DaemonModel) model);
						} else {
							context.displayErrorMessage("Wrong type returned when adding new daemon");
						}
					}
				});
			}
		});
		daemonsPanel.add(newDaemonButton);

		daemonsItem = configTree.addItem(daemonsPanel);

		HorizontalPanel treeAndItems = new HorizontalPanel();
		this.model = context.getApplicationModel();
		this.model.addListener(new MyApplicationModelListener());
		initFromData();

		configTree.addTreeListener(new TreeListener() {
			public void onTreeItemSelected(TreeItem item) {
				if (item != null) {
					if (isDaemonItem(item)) {
						uiPanel.setWidget((DaemonWrapper) item.getUserObject());
						runnerPanel.clear();
					} else if (isDaemonResource(item)) {
						// Parent of module is a daemon
						final DaemonWrapper wrapper = (DaemonWrapper) item.getParentItem().getUserObject();
						final ResourceModel resourceModel = (ResourceModel) item.getUserObject();
						uiPanel.setWidget(wrapper.getUiForResource(resourceModel));
						if (resourceModel instanceof ModuleModel) {
							runnerPanel.setWidget(wrapper.getRunnerUiForModule((ModuleModel) resourceModel));
						} else {
							runnerPanel.clear();
						}
					} else {
						uiPanel.clear();
						runnerPanel.clear();
					}
					lastItemSelected = item;
				}
			}

			public void onTreeItemStateChanged(TreeItem item) {
			}
		});

		uiPanel = new SimplePanel();
		runnerPanel = new SimplePanel();
		treeAndItems.add(configTree);
		treeAndItems.add(uiPanel);
		treeAndItems.add(runnerPanel);

		mainPanel.add(treeAndItems);
		this.setWidget(mainPanel);

		// Open the daemons item
		daemonsItem.setState(true);
	}

	private boolean isDaemonResource(TreeItem item) {
		return item.getUserObject() instanceof ResourceModel;
	}

	private boolean isDaemonItem(TreeItem item) {
		if (item.getParentItem() == null) {
			return false;
		}
		return item.getParentItem().equals(daemonsItem);
	}

	private void initFromData() {
		for (DaemonModel daemon : model.getDaemons()) {
			addDaemonUi(daemon);
		}
	}

	private static final class DaemonUi {
		public DaemonUi(DaemonModel model, TreeItem treeItem, DaemonWrapper wrapper) {
			this.model = model;
			this.treeItem = treeItem;
			this.wrapper = wrapper;
		}

		public DaemonModel model;
		public TreeItem treeItem;
		public DaemonWrapper wrapper;
	}

	private class MyApplicationModelListener implements ResourceModelListener {

		public void initialized(ResourceModel model) {
		}

		public void nameChanged(ResourceModel model) {
		}

		public void childAdded(ResourceModel child, ResourceModel addedTo) {
			addDaemonUi((DaemonModel) child);
		}

		public void childRemoved(ResourceModel child, ResourceModel removedFrom) {
			final DaemonModel daemon = (DaemonModel) child;
			final DaemonUi ui = daemonMap.get(daemon);
			if (ui.treeItem.isSelected()) {
				uiPanel.clear();
				runnerPanel.clear();
			}

			ui.treeItem.remove();

			for (ResourceModel module : daemon.getChildren()) {
				final ModuleWrapper forModule = ui.wrapper.getUiForResource(module);
				if (forModule.equals(uiPanel.getWidget())) {
					uiPanel.clear();
					runnerPanel.clear();
					return;
				}
			}
		}

		public void propertyChanged(ResourceModel model, String propertyName, String newValue) {
		}
	}

	private void addDaemonUi(final DaemonModel daemon) {
		HorizontalPanel daemonItem = new HorizontalPanel();
		daemonItem.add(new Label(daemon.getName()));
		final DeleteButton removeDaemonButton = new DeleteButton("Do you really want to remove this daemon?");
		removeDaemonButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				ConfigurationService.App.getInstance().removeChild(daemon.getId(), new AsyncCallback<Void>() {
					public void onFailure(Throwable throwable) {
						context.displayErrorMessage("Could not remove daemon " + daemon.getName(), throwable);
					}

					public void onSuccess(Void aVoid) {
						model.removeDaemon(daemon);
					}
				});
			}
		});
		daemonItem.add(removeDaemonButton);

		final TreeItem treeItem = new TreeItem(daemonItem);
		DaemonWrapper wrapper = new DaemonWrapper(daemon, context);

		treeItem.setUserObject(wrapper);
		daemonsItem.addItem(treeItem);
		daemonMap.put(daemon, new DaemonUi(daemon, treeItem, wrapper));

		daemon.addListener(new MyDaemonModelListener(treeItem));

		for (ResourceModel resource : daemon.getChildren()) {
			addTreeItemForResource(daemon, resource, treeItem);
		}

		treeItem.setState(true);
	}

	private class MyDaemonModelListener implements ResourceModelListener {

		private final TreeItem daemonTreeItem;

		MyDaemonModelListener(TreeItem daemonTreeItem) {
			this.daemonTreeItem = daemonTreeItem;
		}

		public void initialized(ResourceModel model) {
		}

		public void nameChanged(ResourceModel model) {
			changeTreeItemLabel(daemonTreeItem, model.getName());
		}

		public void childAdded(ResourceModel child, ResourceModel addedTo) {
			addTreeItemForResource((DaemonModel) addedTo, child, daemonTreeItem);
		}

		public void childRemoved(ResourceModel child, ResourceModel removedFrom) {
			removeTreeItemForResource(child, daemonTreeItem);
			final ModuleWrapper removedModuleWrapper = daemonMap.get((DaemonModel) removedFrom).wrapper.getUiForResource(child);
			if (uiPanel.getWidget().equals(removedModuleWrapper)) {
				uiPanel.clear();
				runnerPanel.clear();
			}
			daemonsItem.getTree().setSelectedItem(daemonsItem);
		}

		public void propertyChanged(ResourceModel model, String propertyName, String newValue) {
		}
	}

	private void addTreeItemForResource(final DaemonModel daemon, final ResourceModel resource, final TreeItem daemonTreeItem) {
		HorizontalPanel panel = new HorizontalPanel();
		panel.add(new Label(resource.getName()));
		final DeleteButton deleteButton = new DeleteButton("Do you really want to remove this module?");
		deleteButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				ConfigurationService.App.getInstance().removeChild(resource.getId(), new AsyncCallback<Void>() {
					public void onFailure(Throwable throwable) {
						context.displayErrorMessage("Could not remove module " + resource.getName(), throwable);
					}

					public void onSuccess(Void aVoid) {
						daemon.removeChild(resource);
					}
				});
			}
		});
		panel.add(deleteButton);
		TreeItem item = new TreeItem(panel);
		item.setState(true);
		item.setUserObject(resource);
		daemonTreeItem.addItem(item);
	}

	private void changeTreeItemLabel(final TreeItem treeItem, String newLabel) {
		final HorizontalPanel itemPanel = (HorizontalPanel) treeItem.getWidget();
		((Label) itemPanel.getWidget(0)).setText(newLabel);
	}

	private void removeTreeItemForResource(ResourceModel resource, TreeItem daemonTreeItem) {
		for (int row = 0; row < daemonTreeItem.getChildCount(); row++) {
			final TreeItem child = daemonTreeItem.getChild(row);
			if (child.getUserObject().equals(resource)) {
				daemonTreeItem.removeItem(child);
				break;
			}
		}
	}

}

