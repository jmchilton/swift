package edu.mayo.mprc.peaks.core;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Class to hold information of URIs used by the PeaksOnlineSearch class.
 */
public final class PeaksURIs implements Serializable {
	private static final long serialVersionUID = 20111119L;
	private URI baseURI;
	private URI loginURI;
	private URI searchSubmitURI;
	private URI searchResultURI;
	private URI searchMonitorURI;
	private URI adminURI;
	private URI adminAddUpdateDatabaseURI;
	private URI userSettingsURI;

	public PeaksURIs(URI baseURI) throws URISyntaxException {
		this.baseURI = baseURI;
		this.loginURI = new URI(baseURI.toString() + "/ManageServlet?action=login");
		this.searchSubmitURI = new URI(baseURI.toString() + "/SearchRequest?sg=1");
		this.searchResultURI = new URI(baseURI.toString() + "/results.jsp?ss=1");
		this.searchMonitorURI = new URI(baseURI.toString() + "/monitor.jsp");
		this.adminURI = new URI(baseURI.toString() + "/admin.jsp?section=1");
		this.adminAddUpdateDatabaseURI = new URI(baseURI.toString() + "/ManageServlet?action=customdb");
		this.userSettingsURI = new URI(baseURI.toString() + "/ManageServlet?action=usersettings");
	}

	public URI getBaseURI() {
		return baseURI;
	}

	public void setBaseURI(URI baseURI) {
		this.baseURI = baseURI;
	}

	public URI getLoginURI() {
		return loginURI;
	}

	public void setLoginURI(URI loginURI) {
		this.loginURI = loginURI;
	}

	public URI getSearchSubmitURI() {
		return searchSubmitURI;
	}

	public void setSearchSubmitURI(URI searchSubmitURI) {
		this.searchSubmitURI = searchSubmitURI;
	}

	public URI getSearchResultURI() {
		return searchResultURI;
	}

	public void setSearchResultURI(URI searchResultURI) {
		this.searchResultURI = searchResultURI;
	}

	public URI getAdminAddUpdateDatabaseURI() {
		return adminAddUpdateDatabaseURI;
	}

	public void setAdminAddUpdateDatabaseURI(URI adminAddUpdateDatabaseURI) {
		this.adminAddUpdateDatabaseURI = adminAddUpdateDatabaseURI;
	}

	public URI getUserSettingsURI() {
		return userSettingsURI;
	}

	public void setUserSettingsURI(URI userSettingsURI) {
		this.userSettingsURI = userSettingsURI;
	}

	public URI getSearchMonitorURI() {
		return searchMonitorURI;
	}

	public void setSearchMonitorURI(URI searchMonitorURI) {
		this.searchMonitorURI = searchMonitorURI;
	}

	public URI getAdminURI() {
		return adminURI;
	}

	public void setAdminURI(URI adminURI) {
		this.adminURI = adminURI;
	}
}
