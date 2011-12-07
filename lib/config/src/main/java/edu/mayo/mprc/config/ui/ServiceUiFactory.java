package edu.mayo.mprc.config.ui;

import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.ResourceConfig;

/**
 * A factory able to create the user interface for given resource. It uses a visitor pattern,
 * the user provides {@link UiBuilder} that gets methods invoked on it that describe the user interface.
 * <p/>
 * The user interface is created this way instead of storing it as a configuration file so the code can actually
 * use the context to dynamically change the UI appearance. For instance, if a certain module is not available,
 * the UI would not even offer it to the user.
 */
public interface ServiceUiFactory {
	/**
	 * Creates a new UI by invoking methods on provided {@link UiBuilder}
	 *
	 * @param daemon   Configuration of the daemon within which is the service being created.
	 * @param resource Initial data for the resource.
	 * @param builder  Call methods on the builder to specify components of the user interface.
	 */
	void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder);
}
