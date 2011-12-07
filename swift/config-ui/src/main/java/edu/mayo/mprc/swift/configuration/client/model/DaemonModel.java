package edu.mayo.mprc.swift.configuration.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.mayo.mprc.swift.configuration.client.ConfigurationService;
import edu.mayo.mprc.swift.configuration.client.view.Context;
import edu.mayo.mprc.swift.configuration.client.view.NewModuleCreatedCallback;

public final class DaemonModel extends ResourceModel {
	public static final String HOST_NAME = "hostName";
	public static final String OS_ARCH = "osArch";
	public static final String OS_NAME = "osName";
	public static final String SHARED_FILE_SPACE_PATH = "sharedFileSpacePath";
	public static final String TEMP_FOLDER_PATH = "tempFolderPath";

	public DaemonModel() {
		super(null, "daemon");
	}

	public String getHostName() {
		return getProperty(HOST_NAME);
	}

	public void setHostName(String hostName) {
		setProperty(HOST_NAME, hostName);
	}

	public String getOsArch() {
		return getProperty(OS_ARCH);
	}

	public void setOsArch(String osArch) {
		setProperty(OS_ARCH, osArch);
	}

	public String getOsName() {
		return getProperty(OS_NAME);
	}

	public void setOsName(String osName) {
		setProperty(OS_NAME, osName);
	}

	public String getSharedFileSpacePath() {
		return getProperty(SHARED_FILE_SPACE_PATH);
	}

	public void setSharedFileSpacePath(String sharedFileSpacePath) {
		setProperty(SHARED_FILE_SPACE_PATH, sharedFileSpacePath);
	}

	public String getTempFolderPath() {
		return getProperty(TEMP_FOLDER_PATH);
	}

	public void setTempFolderPath(String tempFolderPath) {
		setProperty(TEMP_FOLDER_PATH, tempFolderPath);
	}

	/**
	 * Helper method that can create a new module and add it to the model, given only type.
	 *
	 * @param type Type of the module to create.
	 */
	public void addNewResource(String type, final NewModuleCreatedCallback callback, final Context errorDisplay) {
		ConfigurationService.App.getInstance().createChild(this.getId(), type, new AsyncCallback<ResourceModel>() {
			public void onFailure(Throwable throwable) {
				errorDisplay.displayErrorMessage("Could not add new resource", throwable);
			}

			public void onSuccess(ResourceModel model) {
				// By adding to the model, we get a notification that will create the UI
				addChild(model);

				if (callback != null) {
					callback.newModuleCreated(model);
				}
			}
		});
	}
}
