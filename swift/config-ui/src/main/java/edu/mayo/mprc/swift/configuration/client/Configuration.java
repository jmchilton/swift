package edu.mayo.mprc.swift.configuration.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import edu.mayo.mprc.swift.configuration.client.model.ApplicationModel;
import edu.mayo.mprc.swift.configuration.client.model.UiChanges;
import edu.mayo.mprc.swift.configuration.client.model.UiChangesReplayer;
import edu.mayo.mprc.swift.configuration.client.view.ConfigWrapper;
import edu.mayo.mprc.swift.configuration.client.view.Context;

public final class Configuration implements EntryPoint, Context {

	private RootPanel configurationPanel;
	private RootPanel progressPanel;
	private RootPanel errorPanel;
	private VerticalPanel multiErrorPanel;
	private Button saveConfigurationButton;
	private ApplicationModel model = new ApplicationModel();
	private ConfigWrapper configWrapper;

	public void onModuleLoad() {
		progressPanel = RootPanel.get("progress");

		displayProgressMessage("Loading...");
		ConfigurationService.App.getInstance().loadConfiguration(new AsyncCallback<ApplicationModel>() {
			public void onFailure(Throwable throwable) {
				displayErrorMessage(throwable.getMessage());
			}

			public void onSuccess(ApplicationModel applicationModel) {
				displayProgressMessage(null);
				model = applicationModel;
				configWrapper = new ConfigWrapper(Configuration.this);
				configurationPanel.add(configWrapper);
				RootPanel.get("saveButtonPlaceholder").add(saveConfigurationButton);
			}

		});
		configurationPanel = RootPanel.get("main");
		errorPanel = RootPanel.get("error");
		errorPanel.add(multiErrorPanel = new VerticalPanel());

		saveConfigurationButton = new Button("Save configuration");
		saveConfigurationButton.addClickListener(new ClickListener() {
			public void onClick(Widget widget) {
				clearErrorMessages();
				displayProgressMessage("Saving...");
				save();
			}
		});


		// displayProgressMessage("Initializing");
	}

	/**
	 * Hide all error messages
	 */
	private void clearErrorMessages() {
		multiErrorPanel.clear();
	}

	private void save() {
		ConfigurationService.App.getInstance().saveConfiguration(new AsyncCallback<UiChangesReplayer>() {
			public void onFailure(Throwable throwable) {
				displayProgressMessage(null);
				displayErrorMessage(throwable.getMessage());
			}

			public void onSuccess(UiChangesReplayer changes) {
				displayProgressMessage(null);
				final StringBuilder message = new StringBuilder();

				changes.replay(new UiChanges() {
					private static final long serialVersionUID = -5054481989230026680L;

					public void setProperty(String resourceId, String propertyName, String newValue) {
					}

					public void displayPropertyError(String resourceId, String propertyName, String error) {
						if (error == null) {
							message.setLength(0);
						} else {
							message.append("<li>")
									.append(error)
									.append("</li>");
						}
					}
				});
				if (message.length() > 0) {
					displayErrorMessage("<ul>" + message.toString() + "</ul>");
				}
			}
		});
	}

	private void displayProgressMessage(final String message) {
		if (message != null) {
			progressPanel.clear();
			progressPanel.add(new HTML(message));
			progressPanel.setVisible(true);
		} else {
			progressPanel.setVisible(false);
		}
	}

	public ApplicationModel getApplicationModel() {
		return model;
	}

	public void displayErrorMessage(final String message) {
		if (message != null) {
			final Panel panel = new FlowPanel();

			HTML errorMessage = new HTML(message);
			errorMessage.addStyleName("error");
			panel.add(errorMessage);

			Button clearErrorButton = new Button("Clear Error");
			clearErrorButton.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					multiErrorPanel.remove(panel);
				}
			});
			panel.add(clearErrorButton);

			multiErrorPanel.add(panel);
		}
	}

	public void displayErrorMessage(String message, Throwable t) {
		displayErrorMessage(message + ": " + t.getMessage());
	}
}
