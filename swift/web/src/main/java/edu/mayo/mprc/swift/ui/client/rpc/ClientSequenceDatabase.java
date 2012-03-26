package edu.mayo.mprc.swift.ui.client.rpc;

import edu.mayo.mprc.dbcurator.model.Curation;

/**
 * Client side proxy of {@link Curation}.
 */
public final class ClientSequenceDatabase implements ClientValue {
	private static final long serialVersionUID = 20101221L;
	private int id;
	private String displayName;
	private String shortName;
	private String ownerEmail;

	public ClientSequenceDatabase() {

	}

	public ClientSequenceDatabase(final int id, final String displayName, final String shortName, final String ownerEmail) {
		this.id = id;
		this.displayName = displayName;
		this.shortName = shortName;
		this.ownerEmail = ownerEmail;
	}

	public int getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getShortName() {
		return shortName;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public boolean equals(final Object o) {
		if (!(o instanceof ClientSequenceDatabase)) {
			return false;
		}
		final ClientSequenceDatabase oo = (ClientSequenceDatabase) o;
		return oo.getId() == getId();
	}

	public int hashCode() {
		return getId();
	}
}
