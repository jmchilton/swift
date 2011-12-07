package edu.mayo.mprc.swift.ui.client.rpc;

/**
 * Client proxy for {@link edu.mayo.mprc.swift.params2.Protease}
 */
public final class ClientProtease implements ClientValue {
	private static final long serialVersionUID = 20101221L;
	private String name;

	public ClientProtease() {
	}

	public ClientProtease(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ClientProtease)) {
			return false;
		}
		ClientProtease oo = (ClientProtease) o;
		return getName().equals(oo.getName());
	}

	public int hashCode() {
		return name.hashCode();
	}
}
