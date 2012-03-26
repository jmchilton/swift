package edu.mayo.mprc.peaks.core;

import edu.mayo.mprc.utilities.HttpClientUtility;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.net.URI;

/**
 * Class
 */
public final class PeaksResult {
	private URI searchURI;
	private URI monitorURI;
	private URI userSettingsURI;
	private HttpClient httpClient;

	public static final String SEARCH_WAITING_STATUS = "waiting";
	public static final String SEARCH_RUNNING_STATUS = "running";
	public static final String SEARCH_COMPLETED_STATUS = "completed";


	public static final String SELECTMAX_SETTING_PARAM = "selectMax";

	/**
	 * Constructor default the maximum number of search days to 10.
	 *
	 * @param searchURI
	 * @param userSettingsURI
	 * @param httpClient
	 * @throws IOException
	 */
	protected PeaksResult(final URI searchURI, final URI monitorURI, final URI userSettingsURI, final HttpClient httpClient) throws IOException {
		this(searchURI, monitorURI, userSettingsURI, httpClient, 10);
	}

	/**
	 * @param searchURI
	 * @param userSettingsURI
	 * @param httpClient
	 * @param maximumNumberOfSearchDays 0 means all days which means that all searches in the
	 *                                  database will be retreived.
	 * @throws IOException
	 */
	protected PeaksResult(final URI searchURI, final URI monitorURI, final URI userSettingsURI, final HttpClient httpClient, final int maximumNumberOfSearchDays) throws IOException {
		this.searchURI = searchURI;
		this.monitorURI = monitorURI;
		this.userSettingsURI = userSettingsURI;
		this.httpClient = httpClient;

		setMaximumNumberOfSearchDays(maximumNumberOfSearchDays);
	}

	public String getSearchStatus(final String searchId) throws IOException {

		GetMethod method = null;
		String string = null;
		String[] parsedString = null;
		String[] parsedSubstring = null;

		try {
			try {
				method = new GetMethod(monitorURI.toString());

				HttpClientUtility.executeMethod(httpClient, method);

				string = new String(method.getResponseBody());
				string = string.substring(string.indexOf("<div id=\"taskqueuepane\""));
				parsedString = string.substring(0, string.indexOf("</div>")).split("<input type=\"checkbox\" name=\"taskList\" value=\"");
				parsedSubstring = null;

				if (parsedString.length > 1) {
					for (int i = 1; i < parsedString.length; i++) {
						parsedSubstring = parsedString[i].split("\"></td><td nowrap> ");

						if (parsedSubstring[0].equals(searchId)) {
							return parsedSubstring[1].split("</td><td nowrap> ")[0];
						}
					}
				}
			} finally {
				method.releaseConnection();
			}

			method = new GetMethod(searchURI.toString());

			HttpClientUtility.executeMethod(httpClient, method);

			string = new String(method.getResponseBody());
			string = string.substring(string.indexOf("<div id=\"searchHistory\""));
			parsedString = string.substring(0, string.indexOf("</div>")).split("<input type=\"checkbox\" name=\"resultList\" value=\"");

			if (parsedString.length > 1) {
				for (int i = 1; i < parsedString.length; i++) {
					parsedSubstring = parsedString[i].split("\"> <a target=\"_blank\" href=\"PeaksResult");

					if (parsedSubstring[0].equals(searchId)) {
						return SEARCH_COMPLETED_STATUS;
					}
				}
			}

		} finally {
			method.releaseConnection();
		}

		return null;
	}

	/**
	 * Sets the maximum number of days to display search results.
	 *
	 * @param numberOfDays
	 */
	private void setMaximumNumberOfSearchDays(final int numberOfDays) throws IOException {
		PostMethod method = null;

		try {
			method = new PostMethod(userSettingsURI.toString());
			method.setParameter(SELECTMAX_SETTING_PARAM, Integer.toString(numberOfDays));

			HttpClientUtility.executeMethod(httpClient, method);
		} finally {
			method.releaseConnection();
		}
	}
}
