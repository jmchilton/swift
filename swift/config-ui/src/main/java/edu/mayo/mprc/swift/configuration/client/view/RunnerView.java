package edu.mayo.mprc.swift.configuration.client.view;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import edu.mayo.mprc.swift.configuration.client.ConfigurationService;
import edu.mayo.mprc.swift.configuration.client.model.ModuleModel;
import edu.mayo.mprc.swift.configuration.client.model.ResourceModel;

import java.util.HashMap;
import java.util.Map;

public final class RunnerView extends SimplePanel {
	private TabPanel runnerType;
	private LocalRunnerView localRunnerView;
	private SgeRunnerView sgeRunnerView;

	public RunnerView(Context context, final ResourceModel model) {
		runnerType = new TabPanel();
		runnerType.addTabListener(new TabListener() {
			public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
				return true;
			}

			public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
				String newType = runnerType.getTabBar().getSelectedTab() == 0 ? "localRunner" : "sgeRunner";
				if (newType.equals(model.getType())) {
					return;
				}

				// TODO: This is a hack, clean this up with proper model-view-controller
				ModuleModel module = (ModuleModel) model.getParent();
				ConfigurationService.App.getInstance().changeRunner(module.getService().getId(), newType,
						new AsyncCallback<Void>() {
							public void onFailure(Throwable caught) {
							}

							public void onSuccess(Void result) {
							}
						});
				model.setType(newType);
				model.setName(newType);

				if ("localRunner".equals(newType)) {
					updateProperties(model, localRunnerView.getModel().getProperties());
					localRunnerView.fireValidations();
				} else {
					updateProperties(model, sgeRunnerView.getModel().getProperties());
					sgeRunnerView.fireValidations();
				}
			}
		});
		localRunnerView = new LocalRunnerView(context, model);
		runnerType.add(localRunnerView, "Run locally");
		sgeRunnerView = new SgeRunnerView(context, model);
		runnerType.add(sgeRunnerView, "Run in grid");
		this.add(runnerType);

		setModel(model);
	}

	private void updateProperties(ResourceModel model, HashMap<String, String> properties) {
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			model.setProperty(entry.getKey(), entry.getValue());
		}
	}

	public void setModel(ResourceModel model) {
		if ("localRunner".equals(model.getType())) {
			localRunnerView.setModel(model);
			runnerType.selectTab(0);
		} else if ("sgeRunner".equals(model.getType())) {
			sgeRunnerView.setModel(model);
			runnerType.selectTab(1);
		} else {
			throw new RuntimeException("Unsupported runner " + model.getClass().getName());
		}
	}
}
