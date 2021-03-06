package edu.mayo.mprc.mascot;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.config.DaemonConfig;
import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;
import edu.mayo.mprc.config.ui.ServiceUiFactory;
import edu.mayo.mprc.config.ui.UiBuilder;
import edu.mayo.mprc.daemon.WorkPacket;
import edu.mayo.mprc.daemon.Worker;
import edu.mayo.mprc.daemon.WorkerFactoryBase;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.progress.PercentDone;
import edu.mayo.mprc.daemon.progress.ProgressReporter;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.FormScraper;
import edu.mayo.mprc.utilities.StreamRegExMatcher;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Daemon worker for Mascot.
 */
public final class MascotWorker implements Worker {
	private static final Logger LOGGER = Logger.getLogger(MascotWorker.class);

	private static final int BUFFER_SIZE = 8192;
	private static final Pattern PERCENT_DONE = Pattern.compile("^\\.*(\\d+)\\% complete\\s*$");
	private static final String MASCOT_URL = "mascotUrl";
	public static final String TYPE = "mascot";
	public static final String NAME = "Mascot";
	public static final String DESC = "<a href=\"http://www.matrixscience.com/\">Mascot search engine</a> support.<p>Swift was tested against Mascot 2.2 without enabled security.</p>";
	public static final int INPUT_FILE_TIMEOUT = 2 * 60 * 1000;
	private URL baseUrl;
	private URL datFileBaseUrl;
	private HttpURLConnection connection;
	private WritableByteChannel channel;
	private String boundary;
	private File mascotOutputFile;
	private final Charset charset = Charset.forName("UTF-8");

	private static final Pattern DB_TAG_PATTERN = Pattern.compile("\\$\\{(?:DB|DBPath):([^}]+)\\}");

	private static final Random RANDOM = new Random();
	/**
	 * The CGI that runs mascot search. Relative to mascot base.
	 */
	public static final String MASCOT_CGI = "cgi/nph-mascot.exe?1";

	public void processRequest(WorkPacket workPacket, ProgressReporter progressReporter) {
		try {
			progressReporter.reportStart();
			process(workPacket, progressReporter);
			workPacket.synchronizeFileTokensOnReceiver();
			progressReporter.reportSuccess();
		} catch (Exception t) {
			progressReporter.reportFailure(t);
		}
	}

	private void process(WorkPacket workPacket, ProgressReporter progressReporter) {
		MascotWorkPacket mascotWorkPacket = (MascotWorkPacket) workPacket;
		assert mascotWorkPacket.getInputFile() != null : "Mascot search failed: mgf file not specified";
		assert mascotWorkPacket.getSearchParamsFile() != null : "Mascot search failed: param file not specified";
		assert mascotWorkPacket.getOutputFile() != null : "Mascot search failed: output file not specified";
		assert mascotWorkPacket.getShortDbName() != null : "Mascot search failed: short db name not specified";
		mascotWorkPacket.waitForInputFiles();

		try {
			LOGGER.debug(MessageFormat.format("Mascot search starting:\n\t{0} -> {1}\n\t(db name: {2})",
					mascotWorkPacket.getInputFile().getAbsolutePath(),
					mascotWorkPacket.getOutputFile().getAbsolutePath(),
					mascotWorkPacket.getShortDbName()));
			this.setMascotOutputFile(mascotWorkPacket.getOutputFile());

			// We have to modify the mascot params file, replacing the ${DB:whatever} tag with the supplied short db name.
			StreamRegExMatcher matcher = new StreamRegExMatcher(DB_TAG_PATTERN, mascotWorkPacket.getSearchParamsFile());
			matcher.replaceAll(Matcher.quoteReplacement(mascotWorkPacket.getShortDbName()));
			matcher.writeContentsToFile(mascotWorkPacket.getSearchParamsFile());
			matcher.close();

			// Now we can run the search
			this.search(
					mascotWorkPacket.getSearchParamsFile().getAbsolutePath(),
					mascotWorkPacket.getInputFile().getAbsolutePath(),
					progressReporter);
		} catch (Exception e) {
			throw new DaemonException("Mascot search failed", e);
		}
		// When the search completes, we are done.
	}

	public void setUrl(URL mascotUrl) {
		if (mascotUrl == null) {
			throw new MprcException("The mascot url must not be null");
		}
		boundary = ("--------" + RANDOM.nextLong()) + Math.abs(RANDOM.nextLong());
		baseUrl = mascotUrl;
		datFileBaseUrl = mascotCgiUrl(mascotUrl);
	}

	static URL mascotCgiUrl(URL mascotBaseUrl) {
		try {
			return new URL(mascotBaseUrl, MASCOT_CGI);
		} catch (MalformedURLException e) {
			throw new MprcException("Cannot obtain mascot CGI url from base url " + mascotBaseUrl.toString(), e);
		}
	}

	private void setMascotOutputFile(File file) {
		mascotOutputFile = file;
	}

	private ByteBuffer getFormBodyTop(Map<String, String> hash) {
		Iterator<Map.Entry<String, String>> iterator = hash.entrySet().iterator();

		StringBuilder sb = new StringBuilder();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			sb.append("--").append(boundary).append("\r\n")
					.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n\r\n")
					.append(entry.getValue())
					.append("\r\n");
		}

		return charset.encode(sb.toString());
	}

	private ByteBuffer getFormFilePreamble(String filename) {
		String s = "--" + boundary + "\r\n";
		s += "Content-Disposition: form-data; name=\"FILE\";";
		s += " filename=\"" + filename + "\"\r\n";
		s += "Content-Type: application/octet-stream\r\n\r\n";
		return charset.encode(s);
	}

	private ByteBuffer getFormBodyBottom() {
		return charset.encode("\r\n--" + boundary + "--\r\n");
	}

	public void setupPOSTConnection(int len) {
		try {
			connection = (HttpURLConnection) datFileBaseUrl.openConnection();
		} catch (Exception e) {
			die("Couldn't setup POST connection", e);
		}

		try {
			connection.setRequestMethod("POST");
			// We must not use _connection.setChunkedStreamingMode(BUFFER_SIZE); here. This seems to be not supported
			// by mascot 2.2 on IIS 6.0. However, if we do not specify content length at all, we run out of heap space.
			// See http://forum.java.sun.com/thread.jspa?threadID=418441&messageID=2816084 for mor information 
			connection.setFixedLengthStreamingMode(len);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" +
					boundary);
			connection.setRequestProperty("Content-Length", Integer.toString(len));
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Keep-Alive", "300");
		} catch (Exception e) {
			die("POST error", e);
		}
	}

	/**
	 * @return Returns the path to the resulting Mascot .dat file
	 */
	private String getPOSTResponse(ProgressReporter progressReporter) {
		StringBuilder completePage = new StringBuilder();
		BufferedReader rreader = null;
		try {
			DataInputStream in = new DataInputStream(connection.getInputStream());
			rreader = new BufferedReader(new InputStreamReader(in));

			String str;
			String link;
			boolean inTag = false;
			String regex = "href=\".*file=(.*\\.dat)\"";
			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Pattern whitespace = Pattern.compile("^\\s*$", Pattern.CASE_INSENSITIVE);
			String url = null;
			while ((str = rreader.readLine()) != null) {

				Matcher matcher = pattern.matcher(str);
				if (matcher.find()) {
					url = matcher.group(1);
				}
				str = FormScraper.stripHtmlTags(str);
				if (!whitespace.matcher(str).matches()) {
					LOGGER.info(str);
					completePage.append(str).append("\n");
					extractReportPercentDone(progressReporter, str);
				}
			}
			in.close();
			if (url != null) {
				return url;
			}
		} catch (Exception t) {
			die("Exception while reading response: ", t);
		} finally {
			FileUtilities.closeQuietly(rreader);
		}
		throw new DaemonException("no data URL for the mascot result\nHere is the returned page source:\n" + completePage.toString());
	}

	private void extractReportPercentDone(ProgressReporter progressReporter, String logLine) {
		final Matcher percentMatcher = PERCENT_DONE.matcher(logLine);
		if (percentMatcher.matches()) {
			try {
				float value = Float.parseFloat(percentMatcher.group(1));
				progressReporter.reportProgress(new PercentDone(value));
			} catch (NumberFormatException ignore) {
				// SWALLOWED: Not a big deal
				LOGGER.debug("Cannot parse Mascot progress" + logLine);
			}
		}
	}

	private void getDatFile(URL url, File outputFile) {
		int pos = 0;
		byte[] buffer = new byte[BUFFER_SIZE];

		InputStream in = null;
		OutputStream out = null;

		try {
			URLConnection connection = url.openConnection();
			in = connection.getInputStream();
			out = new FileOutputStream(outputFile);
			int len = connection.getContentLength();

			while (pos < len) {
				if (in.available() > 0) {
					int read = in.read(buffer);
					pos += read;
					out.write(buffer, 0, read);
				}
			}
		} catch (Exception t) {
			throw new MprcException("Could not obtain Mascot .dat file from " + url.toString() + " as " + outputFile.getAbsolutePath(), t);
		} finally {
			FileUtilities.closeQuietly(out);
			FileUtilities.closeQuietly(in);
		}
	}

	private File getOutputFile() {
		return mascotOutputFile;
	}

	public void search(String parameters, String data, ProgressReporter progressReporter) {
		if (!new File(parameters.trim()).exists()) {
			throw new MprcException("parameters file does not exist: " + parameters.trim());
		}
		if (!new File(data.trim()).exists()) {
			throw new MprcException("data file does not exist: " + data.trim());
		}

		LOGGER.debug("Searching " + data + " with parameters " + parameters);
		BufferedReader reader = null;
		Map<String, String> hash = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(parameters.trim()), "UTF-8"));
			hash = readParameters(reader);
		} catch (FileNotFoundException e) {
			throw new MprcException("Cannot find Mascot parameters file " + parameters, e);
		} catch (UnsupportedEncodingException e) {
			throw new MprcException("Unsupported mascot parameter file encoding", e);
		} catch (IOException e) {
			throw new MprcException("Exception accessing mascot parameter file " + parameters, e);
		} finally {
			FileUtilities.closeQuietly(reader);
		}
		// append input parameters and data file names to the search title
		String title = hash.get("COM");
		if (title != null) {
			title = title.trim() + " ";
		} else {
			title = "";
		}
		title += data + " " + parameters;
		hash.put("COM", title);


		ByteBuffer buffer = getFormBodyTop(hash);
		ByteBuffer buffer2 = getFormFilePreamble(data);
		ByteBuffer buffer3 = getFormBodyBottom();
		FileChannel mgfChannel = null;
		long dataLength = 0;
		try {
			mgfChannel = new FileInputStream(data).getChannel();
			dataLength = mgfChannel.size();
		} catch (Exception e) {
			throw new MprcException("Could not open .mgf file for Mascot search " + data, e);
		}
		long length = dataLength + buffer.limit() + buffer2.limit() + buffer3.limit();
		if (length > Integer.MAX_VALUE) {
			throw new DaemonException("Too large data to post via HTTP: " + length + " bytes, maximum allowed is " + Integer.MAX_VALUE);
		}

		setupPOSTConnection((int) length);

		// try the niochannel approach
		try {
			channel = Channels.newChannel(connection.getOutputStream());
			channel.write(buffer);
			channel.write(buffer2);

			// this only works if dealing with less than 20 Meg
			long position = 0L;
			long amount = 0L;
			try {
				while (position < dataLength) {
					amount = mgfChannel.transferTo(position, dataLength - position, channel);
					position += amount;
				}
			} catch (Exception t) {
				LOGGER.debug("transferred " + position + "bytes, last chunk was " + amount + " bytes.");
				throw new MprcException("nio approach failed, " + t.getMessage(), t);
			}

			channel.write(buffer3);
		} catch (Exception e) {
			throw new MprcException("nio channel write failed, " + e.getMessage(), e);
		} finally {
			FileUtilities.closeQuietly(mgfChannel);
			FileUtilities.closeQuietly(channel);
		}

		String filePath = getPOSTResponse(progressReporter);
		try {
			progressReporter.reportProgress(
					new MascotResultUrl(
							new URL(baseUrl, "cgi/master_results.pl?file=" + filePath).toString()));
			getDatFile(new URL(datFileBaseUrl, filePath), getOutputFile());
		} catch (Exception e) {
			throw new MprcException("Cannot obtain mascot result", e);
		}
	}

	private Map<String, String> readParameters(BufferedReader reader)
			throws IOException, IllegalArgumentException {
		Map<String, String> hash = new HashMap<String, String>();
		String line;
		int count = 0;

		while ((line = reader.readLine()) != null) {
			count++;
			line = line.trim();
			if ((line.length() == 0) || (line.charAt(0) == '#')) {
				continue;
			}
			int pos = line.indexOf('=');
			// handle both the case where there is no equal sign or the key part is empty
			// (return values -1 and 0, respectively)
			if (pos <= 0) {
				throw (new IllegalArgumentException("Not in key=value format at line " + count));
			}
			hash.put(line.substring(0, pos), line.substring(pos + 1));
		}
		return hash;
	}

	private void die(String message, Throwable t) {
		LOGGER.error(t);
		throw new DaemonException(message, t);
	}

	public String toString() {
		return "Mascot worker for URL: " + datFileBaseUrl.toString();
	}

	/**
	 * A factory capable of creating the worker
	 */
	public static final class Factory extends WorkerFactoryBase<Config> {
		@Override
		public Worker create(Config config, DependencyResolver dependencies) {
			MascotWorker worker = new MascotWorker();
			try {
				worker.setUrl(new URL(config.getMascotUrl()));
			} catch (MalformedURLException e) {
				throw new MprcException("Not a valid mascot url: " + config.getMascotUrl(), e);
			}
			return worker;
		}
	}

	/**
	 * Configuration for the factory
	 */
	public static final class Config implements ResourceConfig {
		private String mascotUrl;

		public Config() {
		}

		public Config(String mascotUrl) {
			this.mascotUrl = mascotUrl;
		}

		public String getMascotUrl() {
			return mascotUrl;
		}

		public void setMascotUrl(String mascotUrl) {
			this.mascotUrl = mascotUrl;
		}

		@Override
		public Map<String, String> save(DependencyResolver resolver) {
			Map<String, String> map = new TreeMap<String, String>();
			map.put(MASCOT_URL, mascotUrl);
			return map;
		}

		@Override
		public void load(Map<String, String> values, DependencyResolver resolver) {
			mascotUrl = values.get(MASCOT_URL);
		}

		@Override
		public int getPriority() {
			return 0;
		}
	}

	public static final class Ui implements ServiceUiFactory {
		public void createUI(DaemonConfig daemon, ResourceConfig resource, UiBuilder builder) {
			builder.property(MASCOT_URL, "URL", "Mascot search engine URL").required();
		}
	}
}
