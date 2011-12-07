package edu.mayo.mprc.swift.ui.client.rpc;

import java.util.List;

/**
 * Used for initial client side load of parameter values to avoid multiple round trips.
 */
public final class ClientParamSetValues implements ClientValue {
	private static final long serialVersionUID = 20101221L;
	private List<ClientParam> values;

	/**
	 * Null Constructor
	 */
	public ClientParamSetValues() {
	}

	public ClientParamSetValues(List<ClientParam> values) {
		this.values = values;
	}

	public List<ClientParam> getValues() {
		return values;
	}

	public void setValues(List<ClientParam> values) {
		this.values = values;
	}
}
