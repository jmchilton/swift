package edu.mayo.mprc.swift.configuration.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.mayo.mprc.swift.configuration.client.model.ApplicationModel;
import edu.mayo.mprc.swift.configuration.client.model.ResourceModel;
import edu.mayo.mprc.swift.configuration.client.model.UiChangesReplayer;

public interface ConfigurationServiceAsync {
	void saveConfiguration(AsyncCallback<UiChangesReplayer> async);

	void loadConfiguration(AsyncCallback<ApplicationModel> async);

	void createChild(String parentId, String type, AsyncCallback<ResourceModel> async);

	void removeChild(String childId, AsyncCallback<Void> async);

	void propertyChanged(String modelId, String propertyName, String newValue, boolean onDemand, AsyncCallback<UiChangesReplayer> async);

	void fix(String moduleId, String propertyName, String action, AsyncCallback<Void> async);

	void changeRunner(String serviceId, String newRunnerType, AsyncCallback<Void> async);
}
