package edu.mayo.mprc.swift.configuration.server;

import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.PropertyChangeListener;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.swift.configuration.client.view.UiBuilderReplayer;
import edu.mayo.mprc.swift.configuration.server.properties.validator.ExecutableValidator;
import edu.mayo.mprc.swift.configuration.server.properties.validator.FileValidator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of UiBuilder that stores all commands as an array of strings, allowing them to be replayed later.
 */
public class SerializingUiBuilder implements UiBuilder, Serializable {
	private static final long serialVersionUID = 20101231L;

	private ArrayList<String> commands = new ArrayList<String>(20);
	private ListenerMapBuilder recorder;
	private String previousProperty;
	private DependencyResolver resolver;

	public SerializingUiBuilder(ListenerMapBuilder recorder, DependencyResolver resolver) {
		this.recorder = recorder;
		this.resolver = resolver;
	}

	/**
	 * @return Return an object that can be transferred to the client to replay the UI commands.
	 */
	public UiBuilderReplayer getReplayer() {
		return new UiBuilderReplayer(commands);
	}

	public UiBuilder nativeInterface(String className) {
		commands.add(UiBuilderReplayer.NATIVE_INTERFACE);
		commands.add(className);
		return this;
	}

	public UiBuilder property(String name, String displayName, String description) {
		previousProperty = name;
		commands.add(UiBuilderReplayer.PROPERTY);
		commands.add(name);
		commands.add(displayName);
		commands.add(description);
		return this;
	}

	public UiBuilder required() {
		commands.add(UiBuilderReplayer.REQUIRED);
		return this;
	}

	public UiBuilder defaultValue(String value) {
		commands.add(UiBuilderReplayer.DEFAULT_VALUE);
		commands.add(value);
		return this;
	}

	@Override
	public UiBuilder defaultValue(ResourceConfig value) {
		return defaultValue(resolver.getIdFromConfig(value));
	}

	public UiBuilder addChangeListener(PropertyChangeListener listener) {
		recorder.setListener(previousProperty, listener);
		return this;
	}

	public UiBuilder addDaemonChangeListener(PropertyChangeListener listener) {
		recorder.setDaemonListener(listener);
		return this;
	}

	public UiBuilder validateOnDemand(PropertyChangeListener listener) {
		commands.add(UiBuilderReplayer.VALIDATE_ON_DEMAND);
		recorder.setListener(previousProperty, listener);
		return this;
	}

	public UiBuilder boolValue() {
		commands.add(UiBuilderReplayer.BOOL_VALUE);
		return this;
	}

	public UiBuilder existingDirectory() {
		recorder.setListener(previousProperty, new FileValidator(null, true, true, false));
		return this;
	}

	public UiBuilder existingFile() {
		recorder.setListener(previousProperty, new FileValidator(null, true, false, false));
		return this;
	}

	public UiBuilder integerValue(Integer minimum, Integer maximum) {
		commands.add(UiBuilderReplayer.INTEGER_VALUE);
		commands.add(minimum == null ? null : String.valueOf(minimum));
		commands.add(maximum == null ? null : String.valueOf(maximum));
		return this;
	}

	public UiBuilder executable(List<String> commandLineParams) {
		commands.add(UiBuilderReplayer.VALIDATE_ON_DEMAND);
		recorder.setListener(previousProperty, new ExecutableValidator(commandLineParams));
		return this;
	}

	public UiBuilder reference(String... type) {
		commands.add(UiBuilderReplayer.REFERENCE);
		commands.add(String.valueOf(type.length));
		commands.addAll(Arrays.asList(type));
		return this;
	}

	public UiBuilder enable(String propertyName, boolean synchronous) {
		commands.add(UiBuilderReplayer.ENABLE);
		commands.add(propertyName);
		commands.add(Boolean.toString(synchronous));
		return this;
	}
}
