package edu.mayo.mprc.swift.configuration.server;

import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.UiResponse;
import edu.mayo.mprc.swift.configuration.client.model.UiChangesReplayer;

import java.util.ArrayList;
import java.util.Map;

public class SerializingUiChanges implements UiResponse {
	private ArrayList<String> commands = new ArrayList<String>(4);
	private DependencyResolver resolver;

	public SerializingUiChanges(final DependencyResolver resolver) {
		this.resolver = resolver;
	}

	public void displayPropertyError(final ResourceConfig config, final String propertyName, final String error) {
		commands.add(UiChangesReplayer.DISPLAY_PROPERTY_ERROR);
		commands.add(resolver.getIdFromConfig(config));
		commands.add(propertyName);
		commands.add(error);
	}

	public void setProperty(final ResourceConfig config, final String propertyName, final String newValue) {
		commands.add(UiChangesReplayer.SET_PROPERTY);
		commands.add(resolver.getIdFromConfig(config));
		commands.add(propertyName);
		commands.add(newValue);

		// Actually set the property value on our config
		final Map<String, String> values = config.save(resolver);
		values.put(propertyName, newValue);
		config.load(values, resolver);
	}

	public UiChangesReplayer getReplayer() {
		return new UiChangesReplayer(commands);
	}
}
