package edu.mayo.mprc.swift.report;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.searchdb.dao.TandemMassSpectrometrySample;
import edu.mayo.mprc.utilities.CsvWriter;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.Tuple;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.*;

/**
 * Provides data for {@link SampleReport}
 * @author Roman Zenka
 */
public final class SampleReportData {
	private static final Logger LOGGER = Logger.getLogger(SampleReportData.class);
	private static final int TYPICAL_SAMPLE_HEADERS = 30;
	private static final List<String> FIXED_HEADER =Arrays.asList(
			"File", "Last Modified",
			"Ms1 Spectra", "Ms2 Spectra", "Ms3+ Spectra", "Instrument Name",
			"Instrument Serial #", "Start Time", "Run Time (seconds)", "Comment", "Error Log");
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");
	public static final int DYNAMIC_HEADER_OFFSET = FIXED_HEADER.size();

	/**
	 * Write the CSV data to a provided writer.
	 *
	 * @param writer Writer to write data to.
	 */
	static void writeCsv(Writer writer, List<TandemMassSpectrometrySample> samples) {
		CsvWriter csvWriter = new CsvWriter(writer);

		final Collection<String> combinedHeaders = combinedSampleInformationHeaders(samples);
		List<String> headerList = new ArrayList<String>(DYNAMIC_HEADER_OFFSET +combinedHeaders.size());
		headerList.addAll(FIXED_HEADER);
		headerList.addAll(combinedHeaders);
		final int headersLength = headerList.size();
		String[] headers=new String[headersLength];
		headerList.toArray(headers);

		csvWriter.writeNext(headers);

		// Loop over all the data and write values ordered according to the header
		final String[] data = new String[headersLength];
		for (final TandemMassSpectrometrySample sample : samples) {
			final Map<String, String> values = parseSampleValues(sample.getSampleInformation());

			// Copy fixed data
			data[0]=sample.getFile()==null?null:sample.getFile().getAbsolutePath();
			data[1]=sample.getLastModified()==null?null:DATE_FORMAT.print(sample.getLastModified());
			data[2]=String.valueOf(sample.getMs1Spectra());
			data[3]=String.valueOf(sample.getMs2Spectra());
			data[4]=String.valueOf(sample.getMs3PlusSpectra());
			data[5]=sample.getInstrumentName();
			data[6]=sample.getInstrumentSerialNumber();
			data[7]=DATE_FORMAT.print(sample.getStartTime());
			data[8]=String.valueOf(sample.getRunTimeInSeconds());
			data[9]=sample.getComment();
			data[10]=sample.getErrorLog();

			for (int i = DYNAMIC_HEADER_OFFSET; i < headersLength; i++) {
				final String header = headers[i];
				data[i] = values.get(header);
			}
			csvWriter.writeNext(data);
		}
		FileUtilities.closeQuietly(writer);
	}

	/**
	 * Provide a combined list of all headers present in a set of samples.
	 *
	 * @param samples A list of tandem mass spec samples.
	 * @return A set of headers.
	 */
	static Collection<String> combinedSampleInformationHeaders(final Iterable<TandemMassSpectrometrySample> samples) {
		final LinkedHashSet<String> headers = new LinkedHashSet<String>(100);
		for (final TandemMassSpectrometrySample sample : samples) {
			final String sampleInfo = sample.getSampleInformation();
			headers.addAll(parseSampleHeaders(sampleInfo));
		}
		return headers;
	}

	/**
	 * Take a sampleInfo record and parse the headers out.
	 *
	 * @param sampleInfo Information about the sample.
	 * @return A list of headers defined in the sample info field.
	 */
	static List<String> parseSampleHeaders(final String sampleInfo) {
		if (sampleInfo == null) {
			return Collections.emptyList();
		}
		final List<String> headers = new ArrayList<String>(TYPICAL_SAMPLE_HEADERS);
		final BufferedReader reader = new BufferedReader(new StringReader(sampleInfo));
		try {
			while (true) {
				final String line = reader.readLine();
				if (line == null) {
					break;
				}
				final String header = getHeader(line);
				if (header != null) {
					headers.add(header);
				}
			}
		} catch (IOException e) {
			throw new MprcException("Failed to parse sample headers", e);
		} finally {
			FileUtilities.closeQuietly(reader);
		}
		return headers;
	}

	static Map<String, String> parseSampleValues(final String sampleInfo) {
		if (sampleInfo == null) {
			return Collections.emptyMap();
		}
		final Map<String, String> values = new HashMap<String, String>(TYPICAL_SAMPLE_HEADERS);

		final BufferedReader reader = new BufferedReader(new StringReader(sampleInfo));
		try {
			while (true) {
				final String line = reader.readLine();
				if (line == null) {
					break;
				}
				final Tuple<String, String> entry = getEntry(line);
				if (entry != null) {
					values.put(entry.getFirst(), entry.getSecond());
				}
			}
		} catch (IOException e) {
			throw new MprcException("Failed to parse sample headers", e);
		} finally {
			FileUtilities.closeQuietly(reader);
		}
		return values;
	}

	/**
	 * @param line Line from sample info
	 * @return The header (if present) for the line
	 */
	private static String getHeader(final String line) {
		final int semicolonPos = line.indexOf(':');
		if (semicolonPos >= 0) {
			return line.substring(0, semicolonPos).trim();
		} else {
			return null;
		}
	}

	/**
	 * @param line Line from sample info.
	 * @return The key-value entry the line represents
	 */
	private static Tuple<String, String> getEntry(final String line) {
		final int semicolonPos = line.indexOf(':');
		if (semicolonPos >= 0) {
			return new Tuple<String, String>(line.substring(0, semicolonPos).trim(), line.substring(semicolonPos + 1).trim());
		} else {
			return null;
		}
	}
}
