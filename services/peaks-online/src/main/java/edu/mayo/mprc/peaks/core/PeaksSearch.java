package edu.mayo.mprc.peaks.core;

import edu.mayo.mprc.utilities.HttpClientUtility;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

import java.io.IOException;
import java.net.URI;

/**
 * Class provides functionality to submit peaks online searches.
 */
public final class PeaksSearch {
	private URI uri;
	private HttpClient httpClient;

	private static final String SEARCH_ID_URL_HEADER_NAME = "Location";

	/**
	 * Constructor.
	 *
	 * @param uri        Search request URI.
	 * @param httpClient PeaksOnline http client.
	 */
	protected PeaksSearch(URI uri, HttpClient httpClient) {
		this.uri = uri;
		this.httpClient = httpClient;
	}

	/**
	 * Submit peaks online search.
	 *
	 * @param peaksOnlineSearchParameters search parameters.
	 * @return returns search unique id.
	 * @throws IOException
	 */
	public String submitSearch(PeaksSearchParameters peaksOnlineSearchParameters) throws IOException {

		PostMethod method = null;

		try {
			method = new PostMethod(uri.toString());

			Part[] parts = new Part[peaksOnlineSearchParameters.getNumberOfParameters()];
			int index = 0;

			for (PeaksParameter peaksOnlineParameter : peaksOnlineSearchParameters.getParameters().values()) {
				parts[index++] = peaksOnlineParameter.getHttpHeaderPart();
			}

			MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(parts, method.getParams());
			method.setRequestEntity(multipartRequestEntity);

			HttpClientUtility.executeMethod(httpClient, method);

			//Get Search unique id.
			for (int i = 0; i < method.getResponseHeaders().length; i++) {
				if (method.getResponseHeaders()[i].getName().equals(SEARCH_ID_URL_HEADER_NAME)) {
					//Force paarsing of the search id value to validate the id
					return Long.toString(Long.parseLong(method.getResponseHeaders()[i].getValue().substring(method.getResponseHeaders()[i].getValue().lastIndexOf('=') + 1)));
				}
			}
		} finally {
			method.releaseConnection();
		}

		return null;
	}
}
