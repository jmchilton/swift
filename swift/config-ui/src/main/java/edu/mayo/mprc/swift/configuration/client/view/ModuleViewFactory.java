package edu.mayo.mprc.swift.configuration.client.view;

import java.io.Serializable;

public interface ModuleViewFactory extends Serializable {
	ModuleView createModuleView();
}
