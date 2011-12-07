package edu.mayo.mprc.peaks.core;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.HttpClientUtility;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Class represents a peaks online client instance.
 */
public final class Peaks {

	private static final Logger LOGGER = Logger.getLogger(PeaksSearch.class);

	private HttpClient httpClient;
	private PeaksURIs peaksURIs;

	public static final String LOGIN_FORM_EMAIL_PARAM = "email";
	public static final String LOGIN_FORM_PASSWORD_PARAM = "pwd";

	private static final String SUCCESSFUL_ACTION_REDIRECT_HEADER_NAME = "Location";
	private static final String SUCCESSFUL_ACTION_REDIRECT_HEADER_PART_VALUE = "search.jsp";

	public Peaks(PeaksURIs peaksURIs, String userName, String password) throws IOException {
		this.peaksURIs = peaksURIs;

		httpClient = new HttpClient();

		//Set authentication credentials for any uri, and make authentication happen before any call by the client
		httpClient.getParams().setAuthenticationPreemptive(true);
		httpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));

		login(userName, password);
	}

	private void login(String userName, String password) throws IOException {

		PostMethod method = null;

		try {
			method = new PostMethod(peaksURIs.getLoginURI().toString());
			method.setParameter(LOGIN_FORM_EMAIL_PARAM, userName);
			method.setParameter(LOGIN_FORM_PASSWORD_PARAM, password);

			HttpClientUtility.executeMethod(httpClient, method);

			if (!wasMethodExecutedSuccessfully(method)) {
				throw new MprcException("Method: " + method.getName() + " " + peaksURIs.getLoginURI().toString() + " has failed execution.");
			}
		} finally {
			method.releaseConnection();
		}
	}

	public PeaksSearch getPeaksOnlineSearch() {
		return new PeaksSearch(peaksURIs.getSearchSubmitURI(), httpClient);
	}

	public PeaksResult getPeaksOnlineResult(int maximumNumberOfSearchDays) throws IOException {
		return new PeaksResult(peaksURIs.getSearchResultURI(), peaksURIs.getSearchMonitorURI(), peaksURIs.getUserSettingsURI(), httpClient, maximumNumberOfSearchDays);
	}

	public PeaksResult getPeaksOnlineResult() throws IOException {
		return new PeaksResult(peaksURIs.getSearchResultURI(), peaksURIs.getSearchMonitorURI(), peaksURIs.getUserSettingsURI(), httpClient);
	}

	/**
	 * @param days 0 value yields all searches.
	 * @return
	 * @throws java.io.IOException
	 */
	public PeaksResult getPeaksOnlineResultWithinDays(int days) throws IOException {
		return new PeaksResult(peaksURIs.getSearchResultURI(), peaksURIs.getSearchMonitorURI(), peaksURIs.getUserSettingsURI(), httpClient, days);
	}

	public PeaksAdmin getPeaksOnlineAdmin() {
		return new PeaksAdmin(peaksURIs.getAdminURI(), peaksURIs.getAdminAddUpdateDatabaseURI(), httpClient);
	}

	public final PeaksURIs getPeaksURIs() {
		return peaksURIs;
	}

	private boolean wasMethodExecutedSuccessfully(HttpMethod method) {
		//Verify success of action by checking redirect URI.
		for (int i = 0; i < method.getResponseHeaders().length; i++) {
			if (method.getResponseHeaders()[i].getName().equals(SUCCESSFUL_ACTION_REDIRECT_HEADER_NAME) && method.getResponseHeaders()[i].getValue().indexOf(SUCCESSFUL_ACTION_REDIRECT_HEADER_PART_VALUE) != -1) {
				return true;
			}
		}

		return false;
	}
}
