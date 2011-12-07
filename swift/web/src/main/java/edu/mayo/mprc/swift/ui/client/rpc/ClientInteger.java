package edu.mayo.mprc.swift.ui.client.rpc;

public final class ClientInteger implements ClientValue {
	private static final long serialVersionUID = 20101221L;
	private Integer value;

	public ClientInteger() {
	}

	public ClientInteger(Integer value) {
		this.value = value;
	}

	public ClientInteger(int value) {
		this.value = value;
	}

	public ClientInteger(String value) {
		this(Integer.parseInt(value));
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public String toString() {
		if (this.value == null) {
			return "(null)";
		}
		return String.valueOf(value);
	}
}
