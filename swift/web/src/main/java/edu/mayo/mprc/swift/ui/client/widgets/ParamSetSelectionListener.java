package edu.mayo.mprc.swift.ui.client.widgets;

import edu.mayo.mprc.swift.ui.client.rpc.ClientParamSet;

public interface ParamSetSelectionListener {

	/**
	 * Fired whenever the selection changes.
	 */
	void selected(ClientParamSet selection);

}
