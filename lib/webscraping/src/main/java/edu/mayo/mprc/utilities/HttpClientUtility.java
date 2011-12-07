package edu.mayo.mprc.utilities;

import edu.mayo.mprc.MprcException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

public final class HttpClientUtility {

	private static final Logger LOGGER = Logger.getLogger(HttpClientUtility.class);

	private HttpClientUtility() {

	}

	public static void executeMethod(HttpClient httpClient, HttpMethodBase method) throws IOException {
		LOGGER.debug("Executing " + method.getName() + " method at " + method.getURI().toString());

		int statusCode = httpClient.executeMethod(method);

		LOGGER.debug("Http Headers:");

		for (int i = 0; i < method.getRequestHeaders().length; i++) {
			LOGGER.debug(method.getRequestHeaders()[i].getName() + ": " + method.getRequestHeaders()[i].getValue());
		}

		if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_MOVED_TEMPORARILY) {
			throw new MprcException("Error executing " + method.getName() + " method at " + method.getURI().toString() + ". Error: " + method.getStatusText());
		}

		LOGGER.debug("Executed " + method.getName() + " method at " + method.getURI().toString() + ". Status: " + method.getStatusText());
	}

	/**
	 * Download a give URL, trusting HTTPS servers even if their key does not match.
	 *
	 * @param url         URL to download from.
	 * @param destination Where to download the data to.
	 */
	public static void downloadUrlHttps(String url, File destination) {
		HttpClient client = new HttpClient();

		GetMethod get = new GetMethod(url);

		get.setDoAuthentication(false);

		InputStream responseBodyAsStream = null;
		try {
			int status = client.executeMethod(get);
			if (status != 200) {
				throw new MprcException(MessageFormat.format("Cannot download url [{0}] to file [{1}] - error {2}", url, destination.getAbsolutePath(), status));
			}

			responseBodyAsStream = get.getResponseBodyAsStream();
			FileUtilities.writeStreamToFile(responseBodyAsStream, destination);
		} catch (Exception e) {
			throw new MprcException(MessageFormat.format("Cannot download url [{0}] to file [{1}]", url, destination.getAbsolutePath()), e);
		} finally {
			FileUtilities.closeQuietly(responseBodyAsStream);
			// release any connection resources used by the method
			get.releaseConnection();
		}
	}
}