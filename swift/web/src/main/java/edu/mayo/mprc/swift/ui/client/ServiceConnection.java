package edu.mayo.mprc.swift.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public final class ServiceConnection {
	private static ServiceAsync browsingService;

	private ServiceConnection() {
	}

	public static synchronized ServiceAsync instance() {
		if (browsingService == null) {
			browsingService = (ServiceAsync) GWT.create(Service.class);

			ServiceDefTarget endpoint = (ServiceDefTarget) browsingService;
			String moduleRelativeURL = GWT.getModuleBaseURL() + "Service";
			endpoint.setServiceEntryPoint(moduleRelativeURL);
		}
		return browsingService;
	}
}
