package edu.mayo.mprc.swift.configuration.client.model;

import java.util.ArrayList;

/**
 * Serialized UI module data (mirrors {@link edu.mayo.mprc.config.ServiceConfig}).
 * Module consists of a runner (mirrors {@link edu.mayo.mprc.config.RunnerConfig} and a service (mirrors {@link edu.mayo.mprc.config.ServiceConfig}.
 */
public final class ModuleModel extends ResourceModel {
	private static final int RUNNER = 0;
	private static final int SERVICE = 1;
	private static final long serialVersionUID = 6348206861156895486L;

	public ModuleModel() {
	}

	public ModuleModel(final String name, final String type, final ResourceModel service, final ResourceModel runner) {
		super(name, type);
		final ArrayList<ResourceModel> children = new ArrayList<ResourceModel>(2);
		runner.setParent(this);
		children.add(runner);
		service.setParent(this);
		children.add(service);
		setChildren(children);
	}

	public ResourceModel getRunner() {
		return getChildren().get(RUNNER);
	}

	public void setRunner(final ResourceModel runner) {
		runner.setParent(this);
		getChildren().set(RUNNER, runner);
	}

	public ResourceModel getService() {
		return getChildren().get(SERVICE);
	}

	public void setService(final ResourceModel service) {
		service.setParent(this);
		getChildren().set(SERVICE, service);
	}
}

