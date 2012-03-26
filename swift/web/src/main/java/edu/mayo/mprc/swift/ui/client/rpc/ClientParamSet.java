package edu.mayo.mprc.swift.ui.client.rpc;

/**
 * Client side proxy for {@link edu.mayo.mprc.swift.params2.SearchEngineParameters}.
 */
public final class ClientParamSet implements ClientValue {
	private static final long serialVersionUID = 20101221L;
	private int id;
	private String name;
	private String ownerEmail;
	private String initials;

	public ClientParamSet() {
	}

	public ClientParamSet(final int id, final String name, final String ownerEmail, final String initials) {
		this.id = id;
		this.name = name;
		this.ownerEmail = ownerEmail;
		this.initials = initials;
	}

	public ClientParamSet(final String name, final String ownerEmail, final String initials) {
		this.name = name;
		this.ownerEmail = ownerEmail;
		this.initials = initials;
	}

	public boolean isTemporary() {
		return id < 0;  // TODO wise?
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(final String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}

	public ClientParamSet copy() {
		return new ClientParamSet(id, name, ownerEmail, initials);
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(final String initials) {
		this.initials = initials;
	}

	public boolean equals(final Object t) {
		if (!(t instanceof ClientParamSet)) {
			return false;
		}
		final ClientParamSet tt = (ClientParamSet) t;
		return tt.id == id;
	}

	public int hashCode() {
		return id;
	}

	public String toString() {
		return getName() + " (" + id + ")";
	}

}

