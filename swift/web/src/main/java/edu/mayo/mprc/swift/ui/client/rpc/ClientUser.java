package edu.mayo.mprc.swift.ui.client.rpc;

import java.io.Serializable;

/**
 * UI version of {@link edu.mayo.mprc.workspace.User}
 */
public final class ClientUser implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private String email;
	private String name;
	private String initials;
	private boolean parameterEditorEnabled;
	private boolean outputPathChangeEnabled;

	public ClientUser() {

	}

	public ClientUser(final String email, final String name, final String initials, final boolean parameterEditorEnabled, final boolean outputPathChangeEnabled) {
		this.email = email;
		this.name = name;
		this.initials = initials;
		this.parameterEditorEnabled = parameterEditorEnabled;
		this.outputPathChangeEnabled = outputPathChangeEnabled;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(final String initials) {
		this.initials = initials;
	}

	public boolean isParameterEditorEnabled() {
		return parameterEditorEnabled;
	}

	public void setParameterEditorEnabled(final boolean parameterEditorEnabled) {
		this.parameterEditorEnabled = parameterEditorEnabled;
	}

	public boolean isOutputPathChangeEnabled() {
		return outputPathChangeEnabled;
	}

	public void setOutputPathChangeEnabled(final boolean outputPathChangeEnabled) {
		this.outputPathChangeEnabled = outputPathChangeEnabled;
	}
}
