package edu.mayo.mprc.swift.ui.client.rpc;

import edu.mayo.mprc.swift.params2.Instrument;

/**
 * Client side analog of {@link Instrument}.
 */
public final class ClientInstrument implements ClientValue, Comparable {
	private static final long serialVersionUID = 20101221L;
	private String name;

	public ClientInstrument() {
	}

	public ClientInstrument(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object t) {
		if (!(t instanceof ClientInstrument)) {
			return false;
		}
		ClientInstrument tt = (ClientInstrument) t;
		return getName().equals(tt.getName());
	}

	public int hashCode() {
		return getName().hashCode();
	}

	public int compareTo(Object t) {
		if (!(t instanceof ClientInstrument)) {
			return 1;
		}
		ClientInstrument tt = (ClientInstrument) t;
		return getName().compareTo(tt.getName());
	}

}
