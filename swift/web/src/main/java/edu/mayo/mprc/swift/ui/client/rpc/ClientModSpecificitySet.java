package edu.mayo.mprc.swift.ui.client.rpc;

import edu.mayo.mprc.common.client.ExceptionUtilities;

import java.util.List;

public final class ClientModSpecificitySet implements ClientValue {
	private static final long serialVersionUID = 20101221L;
	private List<ClientModSpecificity> modSpecificities;

	public ClientModSpecificitySet() {
	}

	public ClientModSpecificitySet(List<ClientModSpecificity> modSpecificities) {
		this.modSpecificities = modSpecificities;
	}

	public List<ClientModSpecificity> getModSpecificities() {
		return modSpecificities;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ClientModSpecificitySet)) {
			return false;
		}
		ClientModSpecificitySet tt = (ClientModSpecificitySet) obj;
		final List<ClientModSpecificity> otherSpecificities = tt.getModSpecificities();
		final List<ClientModSpecificity> specificities = getModSpecificities();
		if (otherSpecificities == null && specificities == null) {
			return true;
		}
		if (otherSpecificities == null || specificities == null) {
			return false;
		}
		if (otherSpecificities.size() != specificities.size()) {
			return false;
		}
		for (int i = 0; i < specificities.size(); ++i) {
			if (!specificities.get(i).equals(otherSpecificities.get(i))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		return modSpecificities != null ? modSpecificities.hashCode() : 0;
	}

	public static ClientModSpecificitySet cast(ClientValue value) {
		if (!(value instanceof ClientModSpecificitySet)) {
			ExceptionUtilities.throwCastException(value, ClientModSpecificitySet.class);
			return null;
		}
		return (ClientModSpecificitySet) value;
	}
}
