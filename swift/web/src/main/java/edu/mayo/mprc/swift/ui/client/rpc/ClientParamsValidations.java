package edu.mayo.mprc.swift.ui.client.rpc;

import java.io.Serializable;
import java.util.Map;

/**
 * Wrapped {@link edu.mayo.mprc.swift.params2.mapping.ParamsValidations}
 */
public final class ClientParamsValidations implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private Map<String/* ParamId */, ClientValidationList> validationMap;

	public ClientParamsValidations() {
	}

	public ClientParamsValidations(final Map<String, ClientValidationList> validationMap) {
		this.validationMap = validationMap;
	}

	/**
	 * @return Map of Param Id -> list of validations for this param
	 */
	public Map<String, ClientValidationList> getValidationMap() {
		return validationMap;
	}
}
