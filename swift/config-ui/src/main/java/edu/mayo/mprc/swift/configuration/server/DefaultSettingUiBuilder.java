package edu.mayo.mprc.swift.configuration.server;

import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.PropertyChangeListener;
import edu.mayo.mprc.config.ui.UiBuilder;

import java.util.List;
import java.util.Map;

/**
 * A special {@link edu.mayo.mprc.config.ui.UiBuilder} that collects property default settings.
 * Using this builder we can collect.
 */
class DefaultSettingUiBuilder implements UiBuilder {
	private String lastPropertyName;
	private Map<String, String> values;
	private DependencyResolver resolver;

	public DefaultSettingUiBuilder(Map<String, String> initialValues, DependencyResolver resolver) {
		this.values = initialValues;
		this.resolver = resolver;
	}

	public Map<String, String> getValues() {
		return values;
	}

	@Override
	public UiBuilder nativeInterface(String className) {
		return this;
	}

	@Override
	public UiBuilder property(String name, String displayName, String description) {
		// Remember the property name
		lastPropertyName = name;
		return this;
	}

	@Override
	public UiBuilder required() {
		return this;
	}

	@Override
	public UiBuilder defaultValue(String value) {
		// Store the default value to the last property
		values.put(lastPropertyName, value);
		return this;
	}

	@Override
	public UiBuilder defaultValue(ResourceConfig value) {
		values.put(lastPropertyName, resolver.getIdFromConfig(value));
		return this;
	}

	@Override
	public UiBuilder addChangeListener(PropertyChangeListener listener) {
		return this;
	}

	@Override
	public UiBuilder addDaemonChangeListener(PropertyChangeListener listener) {
		return this;
	}

	@Override
	public UiBuilder validateOnDemand(PropertyChangeListener validator) {
		return this;
	}

	@Override
	public UiBuilder boolValue() {
		return this;
	}

	@Override
	public UiBuilder existingDirectory() {
		return this;
	}

	@Override
	public UiBuilder existingFile() {
		return this;
	}

	@Override
	public UiBuilder integerValue(Integer minimum, Integer maximum) {
		return this;
	}

	@Override
	public UiBuilder executable(List<String> commandLineParams) {
		return this;
	}

	@Override
	public UiBuilder reference(String... type) {
		return this;
	}

	@Override
	public UiBuilder enable(String propertyName, boolean synchronous) {
		return this;
	}
}
