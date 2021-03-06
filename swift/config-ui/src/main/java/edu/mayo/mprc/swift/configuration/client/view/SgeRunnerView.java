package edu.mayo.mprc.swift.configuration.client.view;

import com.google.gwt.user.client.ui.SimplePanel;
import edu.mayo.mprc.swift.configuration.client.model.ResourceModel;

import java.util.HashMap;

public final class SgeRunnerView extends SimplePanel {

	private PropertyList propertyList;
	private ResourceModel resourceModel;

	public static final String DEFAULT_SHARED_LOG_DIR = "var/log";

	private static final String QUEUE_NAME = "queueName";
	private static final String MEMORY_REQUIREMENT = "memoryRequirement";
	private static final String NATIVE_SPECIFICATION = "nativeSpecification";
	private static final String SHARED_WORKING_FOLDER = "sharedWorkingFolder";
	private static final String SHARED_TEMP_FOLDER = "sharedTempFolder";
	private static final String SHARED_LOG_FOLDER = "sharedLogFolder";
	private static final String WRAPPER_SCRIPT = "wrapperScript";

	public SgeRunnerView(Context context, ResourceModel model) {
		GwtUiBuilder builder = new GwtUiBuilder(context, model);

		builder.start()
				.property(QUEUE_NAME, "Queue Name", "SGE queue name.<p>There is usually a queue called <tt>all.q</tt> that sends requests to any queue available.").required().defaultValue("all.q")
				.property(SHARED_WORKING_FOLDER, "Shared Working Folder", "This is a shared folder within the SGE environment. The scripts are executed within this directory. Has to be shared among all the nodes. Important! Any file with a relative path will be relative to this directory at the time of execution.").defaultValue(".").required()
				.property(SHARED_TEMP_FOLDER, "Shared Temp Folder", "This is a shared folder within the SGE environment. Some files may be transferred from remote systems. These transferred files are stored within this directory.").defaultValue("var/tmp").required()
				.property(SHARED_LOG_FOLDER, "Shared Log Folder", "This is a shared folder within the SGE environment. Output folder where standard out log file and error out log files are stored.").defaultValue(DEFAULT_SHARED_LOG_DIR).required()
				.property(WRAPPER_SCRIPT, "Wrapper Script", "The command is executed through this script that servers as a wrapper. We typically use the wrapper to set umask or produce some log messages. Empty field means the command will be executed directly, with no wrapping.").defaultValue("bin/util/sgeWrapper.sh")
				.property(NATIVE_SPECIFICATION, "Native Specification", "SGE native specification, for example, -p for running task in pvm.");
		propertyList = builder.end();

		this.add(propertyList);
		setModel(model);
	}

	public void fireValidations() {
		propertyList.fireValidations();
	}

	public ResourceModel getModel() {
		HashMap<String, String> properties = propertyList.saveUI();
		resourceModel.setProperties(properties);
		return resourceModel;
	}

	public void setModel(ResourceModel model) {
		resourceModel = model;
		if (resourceModel.getProperty(SHARED_LOG_FOLDER) == null || resourceModel.getProperty(SHARED_LOG_FOLDER).length() == 0) {
			resourceModel.setProperty(SHARED_LOG_FOLDER, DEFAULT_SHARED_LOG_DIR);
		}
		propertyList.loadUI(resourceModel.getProperties());
	}

}
