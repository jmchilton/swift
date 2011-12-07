package edu.mayo.mprc.config.ui;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.utilities.exceptions.ExceptionUtilities;

/**
 * Utility listener that changes executable name based on which platform the particular daemon runs on.
 * When the user switches the daemon from windows to linux or vice versa, the default executable name is switched as well.
 */
public final class ExecutableSwitching implements PropertyChangeListener {
	private final ResourceConfig moduleConfig;
	private final String executablePropertyName;
	private final String windowsExecutable;
	private final String linuxExecutable;

	public ExecutableSwitching(ResourceConfig moduleConfig, String executablePropertyName, String windowsExecutable, String linuxExecutable) {
		this.moduleConfig = moduleConfig;
		this.executablePropertyName = executablePropertyName;
		this.windowsExecutable = windowsExecutable;
		this.linuxExecutable = linuxExecutable;
	}

	@Override
	public void propertyChanged(ResourceConfig config, String propertyName, String newValue, UiResponse response, boolean validationRequested) {
		if (config instanceof DaemonConfig) {
			final DaemonConfig daemon = (DaemonConfig) config;
			String executable;
			if (daemon.isWindows()) {
				executable = windowsExecutable;
			} else if (daemon.isLinux()) {
				executable = linuxExecutable;
			} else {
				executable = "";
			}
			response.setProperty(moduleConfig, executablePropertyName, executable);
		} else {
			ExceptionUtilities.throwCastException(config, DaemonConfig.class);
		}
	}

	@Override
	public void fixError(ResourceConfig config, String propertyName, String action) {
		// We never need to fix anything as we do not report errors
	}
}
