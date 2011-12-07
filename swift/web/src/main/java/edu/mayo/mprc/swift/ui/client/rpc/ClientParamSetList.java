package edu.mayo.mprc.swift.ui.client.rpc;

import java.util.List;

/**
 * Client side list of available ParamSets.
 */
public final class ClientParamSetList implements ClientValue {
	private static final long serialVersionUID = 20101221L;
	private List<ClientParamSet> list;

	public ClientParamSetList() {
	}

	public ClientParamSetList(List<ClientParamSet> list) {
		setList(list);
	}

	public List<ClientParamSet> getList() {
		return list;
	}

	public void setList(List<ClientParamSet> list) {
		this.list = list;
	}
}
