package edu.mayo.mprc.peaks.core;

import edu.mayo.mprc.utilities.HttpClientUtility;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Class provides peaks online administrative functionalities.
 */
public final class PeaksAdmin {

	private static final Logger LOGGER = Logger.getLogger(PeaksAdmin.class);

	public static final String NCBINR_DB_FORMAT = "NCBI nr";
	public static final String SWISSPROT_DB_FORMAT = "Swiss-Prot";
	public static final String IPI_DB_FORMAT = "IPI";
	public static final String TREMBL_DB_FORMAT = "TrEMBL";
	public static final String MSDB_DB_FORMAT = "MSDB";
	public static final String UNIPROT_DB_FORMAT = "UniProt";

	public static final String DB_NAME_PARAM = "textDBName";
	public static final String DB_PATH_PARAM = "textDBPath";
	public static final String DB_FORMAT_PARAM = "selectDBFormat";
	public static final String DB_USERACTION_PARAM = "userAction";
	public static final String DB_EST_PARAM = "checkEST";
	public static final String DB_ID_PARAM = "selectdb";
	public static final String DB_DELETE_ID_PARAM = "dblist";

	private static final String SUCCESSFUL_ACTION_REDIRECT_HEADER_NAME = "Location";
	//Todo: Find a way to check for the result other that using this URL. If URL is changed this is broken.
	private static final String SUCCESSFUL_ACTION_REDIRECT_HEADER_VALUE = "http://peaks.mayo.edu:8080/peaksonline/?section=1";


	private static List<String> DatabaseFormats;

	static {
		DatabaseFormats = new ArrayList<String>();
		DatabaseFormats.add(NCBINR_DB_FORMAT);
		DatabaseFormats.add(SWISSPROT_DB_FORMAT);
		DatabaseFormats.add(IPI_DB_FORMAT);
		DatabaseFormats.add(TREMBL_DB_FORMAT);
		DatabaseFormats.add(MSDB_DB_FORMAT);
		DatabaseFormats.add(UNIPROT_DB_FORMAT);
	}

	private URI adminURI;
	private URI addUpdateDatabaseURI;
	private HttpClient httpClient;

	protected PeaksAdmin(final URI adminURI, final URI addUpdateDatabaseURI, final HttpClient httpClient) {
		this.adminURI = adminURI;
		this.addUpdateDatabaseURI = addUpdateDatabaseURI;
		this.httpClient = httpClient;
	}

	/**
	 * Adds database to peaks online search engine.
	 *
	 * @param databaseName
	 * @param databaseFilePath File path must be relative to the server hosting the peaks online search engine.
	 * @param databaseFormat
	 * @param estDatabase
	 * @return
	 */
	public boolean addDatabase(final String databaseName, final String databaseFilePath, final String databaseFormat, final boolean estDatabase) throws IOException {
		return addUpdateDatabase(null, databaseName, databaseFilePath, databaseFormat, estDatabase);
	}

	public boolean addDatabase(final PeaksDatabase peaksOnlineDatabase) throws IOException {
		return addUpdateDatabase(peaksOnlineDatabase.getDatabaseId(), peaksOnlineDatabase.getDatabaseName(), peaksOnlineDatabase.getDatabaseFilePath(), peaksOnlineDatabase.getDatabaseFormat(), peaksOnlineDatabase.isEstDatabase());
	}

	public boolean removeDatabase(final PeaksDatabase peaksOnlineDatabase) throws IOException {
		return removeDatabaseLocal(peaksOnlineDatabase.getDatabaseId());
	}

	public boolean removeDatabase(final String databaseId) throws IOException {
		return removeDatabaseLocal(databaseId);
	}

	public Collection<PeaksDatabase> getAllDatabases() throws IOException {

		GetMethod method = null;
		String string = null;
		String[] parsedString = null;
		final LinkedList<String> databaseIds = new LinkedList();
		final LinkedList<PeaksDatabase> databases = new LinkedList();

		try {
			method = new GetMethod(adminURI.toString());

			HttpClientUtility.executeMethod(httpClient, method);

			string = method.getResponseBodyAsString();

			//Parse html response
			string = string.substring(string.indexOf("<form name=\"formCustomDB\" method=\"post\" "));
			parsedString = string.substring(0, string.lastIndexOf("href=\"admin.jsp?section=1&selectdb=")).split("name=\"dblist\" value=\"");

			if (parsedString.length > 1) {
				for (int i = 1; i < parsedString.length; i++) {
					databaseIds.add(parsedString[i].split("\"")[0]);
				}
			}
		} finally {
			method.releaseConnection();
		}

		for (final String databaseId : databaseIds) {
			databases.add(getDatabase(databaseId));
		}

		return databases;
	}

	public PeaksDatabase getDatabase(final String databaseId) throws IOException {

		GetMethod method = null;
		String string = null;
		String[] parsedString = null;
		String[] parsedSubstring = null;

		String databaseName = null;
		String databaseFilePath = null;
		String databaseFormat = null;
		boolean estDatabase = false;

		try {
			method = new GetMethod(adminURI.toString() + "&" + DB_ID_PARAM + "=" + databaseId);

			HttpClientUtility.executeMethod(httpClient, method);

			//Parse html response
			string = method.getResponseBodyAsString();
			string = string.substring(string.indexOf("<a href=\"#\"><b>Name</b></a>"));

			parsedString = string.substring(0, string.indexOf("<a href=\"#\"><b>This is an EST database</b></a>")).split("<input type=\"text\" name=\"textDBName\" value=\"");
			databaseName = parsedString[1].split("\"")[0];

			parsedString = parsedString[1].split("<input type=\"text\" name=\"textDBPath\" value=\"");
			databaseFilePath = parsedString[1].split("\"")[0];

			parsedString = parsedString[1].split("<select name=\"selectDBFormat\" style=\"width:320px\">");
			parsedSubstring = parsedString[1].split("</option>");

			for (int i = 0; i < parsedSubstring.length; i++) {
				if (parsedSubstring[i].indexOf("selected") != -1) {
					databaseFormat = DatabaseFormats.get(Integer.parseInt(parsedSubstring[i].split("<option value=\"")[1].split("\"")[0]));
					break;
				}
			}

			parsedString = parsedString[1].split("<input type=\"checkbox\" name=\"checkEST\"");
			estDatabase = parsedString[1].indexOf("checked") != -1;

			return new PeaksDatabase(databaseId, databaseName, databaseFilePath, databaseFormat, estDatabase);

		} finally {
			method.releaseConnection();
		}
	}

	public Collection<PeaksEnzyme> getAllEnzymes() throws IOException {

		GetMethod method = null;
		String responseBody = null;
		String[] parsedString = null;
		final LinkedList<PeaksEnzyme> enzymes = new LinkedList();

		try {
			method = new GetMethod(adminURI.toString());

			HttpClientUtility.executeMethod(httpClient, method);

			responseBody = new String(method.getResponseBody());

			//Parse html response
			responseBody = responseBody.substring(responseBody.indexOf("<form name=\"formEnzymeConf\" method=\"post\" "));
			parsedString = responseBody.substring(0, responseBody.indexOf("</form>")).split("name=\"enzymelist\" value=\"");

			if (parsedString.length > 1) {
				String enzymeId = null;
				String enzymeName = null;
				String tempString = null;

				for (int i = 1; i < parsedString.length; i++) {
					enzymeId = parsedString[i].split("\"")[0];
					tempString = parsedString[i].split("section=2&selectenzyme=")[1];
					enzymeName = tempString.substring(tempString.indexOf('>') + 1, tempString.indexOf('<'));

					enzymes.add(new PeaksEnzyme(enzymeId, enzymeName));
				}
			}
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}

		return enzymes;
	}

	private boolean removeDatabaseLocal(final String databaseId) throws IOException {

		PostMethod method = null;

		try {
			method = new PostMethod(addUpdateDatabaseURI.toString());
			method.setParameter(DB_DELETE_ID_PARAM, databaseId);
			method.setParameter(DB_USERACTION_PARAM, "2");

			HttpClientUtility.executeMethod(httpClient, method);

			return wasMethodExecutedSuccessfully(method);

		} finally {
			method.releaseConnection();
		}
	}

	private boolean addUpdateDatabase(final String databaseId, final String databaseName, final String databaseFilePath, final String databaseFormat, final boolean estDatabase) throws IOException {

		PostMethod method = null;

		try {
			method = new PostMethod(addUpdateDatabaseURI.toString());

			if (databaseId != null) {
				method.setParameter(DB_ID_PARAM, databaseId);
			}

			method.setParameter(DB_NAME_PARAM, databaseName);
			method.setParameter(DB_PATH_PARAM, databaseFilePath);
			method.setParameter(DB_FORMAT_PARAM, Integer.toString(DatabaseFormats.indexOf(databaseFormat)));
			method.setParameter(DB_USERACTION_PARAM, "1");

			if (estDatabase) {
				method.setParameter(DB_EST_PARAM, "on");
			}

			HttpClientUtility.executeMethod(httpClient, method);

			return wasMethodExecutedSuccessfully(method);

		} finally {
			method.releaseConnection();
		}
	}

	private boolean wasMethodExecutedSuccessfully(final HttpMethod method) {
		//Verify success of action by checking redirect URI.
		for (int i = 0; i < method.getResponseHeaders().length; i++) {
			if (method.getResponseHeaders()[i].getName().equals(SUCCESSFUL_ACTION_REDIRECT_HEADER_NAME) && method.getResponseHeaders()[i].getValue().indexOf(SUCCESSFUL_ACTION_REDIRECT_HEADER_VALUE) != -1) {
				return true;
			}
		}

		return false;
	}
}
