package edu.mayo.mprc.config.ui;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.ResourceConfig;

/**
 * Some executables need to run in a "wrapper". For instance, Windows executables on Linux require Wine.
 * This switcher monitors the daemon configuration and when it changes from Windows to Linux or vice versa,
 * it automatically changes the wrapper file resource property to prevent the config from breaking.
 */
public class WrapperScriptSwitcher implements PropertyChangeListener {
	private final ResourceConfig resource;
	private final DaemonConfig daemon;
	private final String wrapperScriptProperty;

	public WrapperScriptSwitcher(final ResourceConfig resource, final DaemonConfig daemon, final String wrapperScriptProperty) {
		this.resource = resource;
		this.daemon = daemon;
		this.wrapperScriptProperty = wrapperScriptProperty;
	}

	@Override
	public void propertyChanged(final ResourceConfig config, final String propertyName, final String newValue, final UiResponse response, final boolean validationRequested) {
		response.setProperty(resource, wrapperScriptProperty, daemon.getWrapperScript());
	}

	@Override
	public void fixError(final ResourceConfig config, final String propertyName, final String action) {
		// Never needed - we do not report errors
	}
}
