package edu.mayo.mprc.swift.configuration.client.view;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public final class ModuleWrapper extends SimplePanel {
	private final ModuleView module;

	public ModuleWrapper(String moduleName, ModuleView module, String description) {
		this.module = module;
		this.addStyleName("module-wrapper");
		FlowPanel panel = new FlowPanel();
		panel.addStyleName("module");
		final Label label = new Label(moduleName);
		label.addStyleName("module-label");
		panel.add(label);
		final HTML desc = new HTML(description);
		desc.addStyleName("module-description");
		panel.add(desc);
		panel.add(module.getModuleWidget());
		this.setWidget(panel);
	}

	public ModuleView getModule() {
		return module;
	}
}
