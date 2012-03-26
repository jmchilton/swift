package edu.mayo.mprc.swift.ui.client.rpc;

/**
 * Tuple: Param id string, ClientValue, ClientValidation
 */
public final class ClientParam implements ClientValue {
	private static final long serialVersionUID = 20101221L;
	private String paramId;
	private ClientValue value;
	private ClientValidationList validationList;

	public ClientParam() {
	}

	public ClientParam(final String paramId,
	                   final ClientValue value,
	                   final ClientValidationList validationList) {
		this.paramId = paramId;
		this.value = value;
		this.validationList = validationList;
	}

	public String getParamId() {
		return paramId;
	}

	public void setParamId(final String paramId) {
		this.paramId = paramId;
	}

	public ClientValidationList getValidationList() {
		return validationList;
	}

	public void setValidationList(final ClientValidationList validationList) {
		this.validationList = validationList;
	}

	public ClientValue getValue() {
		return value;
	}

	public void setValue(final ClientValue value) {
		this.value = value;
	}

	public String toString() {
		return getParamId();
	}
}
