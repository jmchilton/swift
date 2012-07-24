package edu.mayo.mprc.utilities;

import com.google.common.io.CharStreams;
import edu.mayo.mprc.MprcException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public final class HttpClientUtility {

	private static final Logger LOGGER = Logger.getLogger(HttpClientUtility.class);

	private HttpClientUtility() {

	}

	/**
	 * Execute a given http method (get/post) and return response. If the status is not OK, an exception is thrown.
	 *
	 * @param httpClient Client to execute the method on.
	 * @param method     Method to execute.
	 * @return {@link HttpResponse}
	 */
	public static HttpResponse executeMethod(final HttpClient httpClient, final HttpUriRequest method) {
		LOGGER.debug("Executing " + method.getMethod() + " method at " + method.getURI().toString());

		final HttpResponse httpResponse;
		try {
			httpResponse = httpClient.execute(method);
		} catch (IOException e) {
			throw new MprcException("Error executing " + method.getMethod() + " method at " + method.getURI().toString(), e);
		}

		LOGGER.debug("Http Headers:");


		for (final Header header : method.getAllHeaders()) {
			LOGGER.debug(header.getName() + ": " + header.getValue());
		}

		final int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_MOVED_TEMPORARILY) {
			throw new MprcException("Error executing " + method.getMethod() + " method at " + method.getURI().toString() + ". Error: " + httpResponse.getStatusLine().getReasonPhrase());
		}

		LOGGER.debug("Executed " + method.getMethod() + " method at " + method.getURI().toString() + ". Status: " + httpResponse.getStatusLine().getReasonPhrase());

		return httpResponse;
	}

	public static String loadResponseToString(HttpResponse httpResponse) {
		String response = null;
		if (httpResponse.getEntity() != null) {
			InputStreamReader inputStreamReader = null;
			try {
				inputStreamReader = new InputStreamReader(httpResponse.getEntity().getContent(), httpResponse.getEntity().getContentEncoding().getValue());
				response = CharStreams.toString(inputStreamReader);
			} catch (IOException e) {
				throw new MprcException(e);
			} finally {
				FileUtilities.closeQuietly(inputStreamReader);
			}
		}
		return response;
	}

	public static String getPostResponse(final HttpClient httpClient, final String uri, final Map<String, String> methodParams) {
		try {
			final HttpUriRequest method = new HttpPost(uri);
			final HttpParams params = new BasicHttpParams();
			for (final Map.Entry<String, String> me : methodParams.entrySet()) {
				params.setParameter(me.getKey(), me.getValue());
			}
			method.setParams(params);

			final HttpResponse httpResponse = executeMethod(httpClient, method);

			return loadResponseToString(httpResponse);
		} catch (MprcException e) {
			throw new MprcException("Could not post to web page [" + uri + "]", e);
		}
	}

	public static String httpGet(HttpClient httpClient, final String uri) {
		final HttpResponse httpResponse = executeMethod(httpClient, new HttpGet(uri));
		return loadResponseToString(httpResponse);
	}
}