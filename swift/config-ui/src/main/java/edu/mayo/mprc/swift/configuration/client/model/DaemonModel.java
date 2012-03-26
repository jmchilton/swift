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
	private static final long serialVersionUID = -3470019100042087509L;

	public DaemonModel() {
		super(null, "daemon");
	}

	public String getHostName() {
		return getProperty(HOST_NAME);
	}

	public void setHostName(final String hostName) {
		setProperty(HOST_NAME, hostName);
	}

	public String getOsArch() {
		return getProperty(OS_ARCH);
	}

	public void setOsArch(final String osArch) {
		setProperty(OS_ARCH, osArch);
	}

	public String getOsName() {
		return getProperty(OS_NAME);
	}

	public void setOsName(final String osName) {
		setProperty(OS_NAME, osName);
	}

	public String getSharedFileSpacePath() {
		return getProperty(SHARED_FILE_SPACE_PATH);
	}

	public void setSharedFileSpacePath(final String sharedFileSpacePath) {
		setProperty(SHARED_FILE_SPACE_PATH, sharedFileSpacePath);
	}

	public String getTempFolderPath() {
		return getProperty(TEMP_FOLDER_PATH);
	}

	public void setTempFolderPath(final String tempFolderPath) {
		setProperty(TEMP_FOLDER_PATH, tempFolderPath);
	}

	/**
	 * Helper method that can create a new module and add it to the model, given only type.
	 *
	 * @param type Type of the module to create.
	 */
	public void addNewResource(final String type, final NewModuleCreatedCallback callback, final Context errorDisplay) {
		ConfigurationService.App.getInstance().createChild(this.getId(), type, new AsyncCallback<ResourceModel>() {
			public void onFailure(final Throwable throwable) {
				errorDisplay.displayErrorMessage("Could not add new resource", throwable);
			}

			public void onSuccess(final ResourceModel model) {
				// By adding to the model, we get a notification that will create the UI
				addChild(model);

				if (callback != null) {
					callback.newModuleCreated(model);
				}
			}
		});
	}
}
