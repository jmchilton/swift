package edu.mayo.mprc.swift.ui.client.widgets.validation;

import edu.mayo.mprc.swift.ui.client.rpc.ClientModSpecificity;
import edu.mayo.mprc.swift.ui.client.rpc.ClientModSpecificitySet;
import edu.mayo.mprc.swift.ui.client.rpc.ClientValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows multiple-selection of modifications.
 */
public final class ModificationListBox extends ValidatableListBox {
	public ModificationListBox(String param, boolean allowMultiple) {
		super(param, allowMultiple);
	}

	public ClientValue bundle(List<? extends ClientValue> selected) {
		List<ClientModSpecificity> specs = new ArrayList<ClientModSpecificity>(selected.size());
		for (ClientValue value : selected) {
			specs.add(ClientModSpecificity.cast(value));
		}
		return new ClientModSpecificitySet(specs);
	}

	public String getStringValue(ClientValue value) {
		return ClientModSpecificity.cast(value).getName();
	}

	public List<? extends ClientValue> unbundle(ClientValue value) {
		return ClientModSpecificitySet.cast(value).getModSpecificities();
	}

	/**
	 * @return We have our own mechanism of fetching allowed values, because there are many modifications and they never change.
	 *         We never request update of allowed values.
	 */
	public String getAllowedValuesParam() {
		return null;
	}
}
