package edu.mayo.mprc.swift.configuration.client.view;

import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.swift.configuration.client.model.*;

import java.util.HashMap;
import java.util.Map;

public final class DaemonWrapper extends SimplePanel {
	private final DaemonModel daemonModel;
	private final ListBox newModulePicker;
	private Button newModuleButton;
	private final AvailableModules availableModules;
	private Map<ResourceModel, ModuleConfigUis> uis = new HashMap<ResourceModel, ModuleConfigUis>();
	private DaemonView daemonConfigUI;
	private Context context;

	private class ModuleConfigUis {
		private ModuleConfigUis(ModuleWrapper moduleWrapper, RunnerView runner) {
			this.ui = moduleWrapper;
			this.runner = runner;
		}

		public ModuleWrapper ui;
		public RunnerView runner;
	}

	public DaemonWrapper(DaemonModel daemonModel, final Context context) {
		this.context = context;
		this.addStyleName("module-wrapper");
		FlowPanel panel = new FlowPanel();
		panel.addStyleName("module");
		final Label label = new Label("Daemon");
		label.addStyleName("module-label");
		panel.add(label);
		final HTML desc = new HTML("A daemon is a process that a provides portion of Swift functionality. " +
				"<ul><li>A daemon consists of multiple modules.</li>" +
				"<li>Communication between daemons is done using a message broker, " +
				"shared filesystem and for the UI and main searcher, also using a database. All these means of communication are available and configurable as modules.</li>" +
				"<li>Typically, you would like to have one daemon per physical machine, but that is not a requirement</li>" +
				"</ul>" +
				"<p>The properties below are mostly used for simplifying the configuration process. The only mandatory fields are daemon name and host name, in case you want to run multiple daemons, you must also set up the shared file space.</p>");
		desc.addStyleName("module-description");
		panel.add(desc);

		this.daemonModel = daemonModel;
		this.availableModules = context.getApplicationModel().getAvailableModules();

		this.daemonModel.addListener(new MyDaemonModelListener());

		newModulePicker = new ListBox(false);
		for (AvailableModules.Info info : availableModules.getModuleInfos()) {
			newModulePicker.addItem(info.getName(), info.getType());
		}

		newModuleButton = new Button("Add new module");
		newModuleButton.addStyleName("new-module-button");
		newModuleButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				final int index = newModulePicker.getSelectedIndex();
				final String type = newModulePicker.getValue(index);
				DaemonWrapper.this.daemonModel.addNewResource(type, null, context);
			}
		});

		daemonConfigUI = new DaemonView();
		daemonConfigUI.initializeUi(this.daemonModel, this.context);
		panel.add(daemonConfigUI.getWidget());

		Panel moduleAdding = new HorizontalPanel();
		moduleAdding.add(newModulePicker);
		moduleAdding.add(newModuleButton);
		panel.add(moduleAdding);

		for (ResourceModel resource : daemonModel.getChildren()) {
			addUiForResource(resource);
		}
		this.setWidget(panel);
	}

	public ModuleWrapper getUiForResource(ResourceModel resourceModel) {
		final DaemonWrapper.ModuleConfigUis configUis = uis.get(resourceModel);
		return configUis == null ? null : configUis.ui;
	}

	public RunnerView getRunnerUiForModule(ModuleModel moduleModel) {
		return uis.get(moduleModel).runner;
	}

	private class MyDaemonModelListener implements ResourceModelListener {
		public void initialized(ResourceModel model) {
		}

		public void nameChanged(ResourceModel model) {
		}

		public void childAdded(ResourceModel child, ResourceModel addedTo) {
			addUiForResource(child);
		}

		public void childRemoved(ResourceModel child, ResourceModel removedFrom) {
			removeUiForResource(child);
		}

		public void propertyChanged(ResourceModel model, String propertyName, String newValue) {
			daemonConfigUI.loadUI((DaemonModel) model);
		}
	}

	/**
	 * The model is already in the list. Just add the ui.
	 */
	private void addUiForResource(ResourceModel resource) {
		ModuleWrapper ui = createNewModuleConfigUI(resource);
		ui.getModule().loadUI(resource.getProperties());

		RunnerView runner = null;
		if (resource instanceof ModuleModel) {
			runner = createNewRunnerUi(((ModuleModel) resource).getRunner());
		}

		ModuleConfigUis t = new ModuleConfigUis(ui, runner);
		uis.put(resource, t);
	}

	private ModuleWrapper createNewModuleConfigUI(final ResourceModel module) {
		String type = module.getType();
		ModuleView ui = null;
		if ("database" .equals(type)) {
			// Special case. Database UI is currently not supported by UiBuilder, so it is created manually
			GwtUiBuilder builder = new GwtUiBuilder(context, module);

			final DatabaseView databaseView = new DatabaseView(builder, module);
			return new ModuleWrapper(availableModules.getModuleNameForType(type), databaseView, availableModules.getDescriptionForType(type));
		} else {
			GwtUiBuilder builder = new GwtUiBuilder(context, module);
			builder.start();
			module.getReplayer().replay(builder);
			return new ModuleWrapper(availableModules.getModuleNameForType(type), builder.end(), availableModules.getDescriptionForType(type));
		}
	}

	private RunnerView createNewRunnerUi(ResourceModel model) {
		RunnerView runner = new RunnerView(context, model);
		runner.setStyleName("runner-wrapper");
		return runner;
	}

	private void removeUiForResource(ResourceModel module) {
		for (Map.Entry<ResourceModel, ModuleConfigUis> ui : uis.entrySet()) {
			if (ui.getKey().equals(module)) {
				uis.remove(ui.getKey());
				break;
			}
		}
	}

}
