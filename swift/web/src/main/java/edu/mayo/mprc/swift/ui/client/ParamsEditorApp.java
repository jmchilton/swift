package edu.mayo.mprc.swift.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import edu.mayo.mprc.swift.ui.client.rpc.ClientUser;

import java.util.Map;

/**
 * Hosts SimpleParamsEditorPanel for testing.
 * Not an entry point-called manually from SwiftApp.
 */
public final class ParamsEditorApp {

	private static ServiceAsync serviceAsync;
	private static SimpleParamsEditorPanel panel;

	private ParamsEditorApp() {
	}
	//public int changeTimeout = 5000; // Validation delay after modifying text box in msecs.

	private static void initConnection() {
		serviceAsync = (ServiceAsync) GWT.create(Service.class);

		String moduleRelativeURL = GWT.getModuleBaseURL() + "Service";
		((ServiceDefTarget) serviceAsync).setServiceEntryPoint(moduleRelativeURL);
	}


	/**
	 * * This is the entry point method.
	 */
	public static void onModuleLoad(HidesPageContentsWhileLoading contentHiding, Map<String, ClientUser> userInfo) {
		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
			public void onUncaughtException(Throwable throwable) {
				SimpleParamsEditorPanel.handleGlobalError(throwable);
			}
		});

		initConnection();

		panel = new SimpleParamsEditorPanel(serviceAsync, contentHiding, userInfo);

	}

	public static SimpleParamsEditorPanel getPanel() {
		return panel;
	}
}
