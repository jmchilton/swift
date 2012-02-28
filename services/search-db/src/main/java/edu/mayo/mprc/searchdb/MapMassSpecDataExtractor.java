package edu.mayo.mprc.searchdb;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.io.TsvStreamReader;
import edu.mayo.mprc.searchdb.dao.TandemMassSpectrometrySample;
import edu.mayo.mprc.utilities.FileUtilities;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Extract mass spec data from a map of {@link RawFileMetaData} objects embedded in the work packet.
 *
 * @author Roman Zenka
 */
public class MapMassSpecDataExtractor implements MassSpecDataExtractor {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private final Map<String/*msmsSampleName*/, RawFileMetaData> metaDataMap;

	public MapMassSpecDataExtractor(Map<String, RawFileMetaData> metaDataMap) {
		this.metaDataMap = metaDataMap;
	}

	@Override
	public TandemMassSpectrometrySample getTandemMassSpectrometrySample(String biologicalSampleName, String msmsSampleName) {
		final RawFileMetaData rawFileMetaData = metaDataMap.get(msmsSampleName);
		if (rawFileMetaData == null) {
			return new TandemMassSpectrometrySample(null, null, 0, 0, 0, null, null, 0.0, null, null, null, null, null);
		} else {
			try {
				TsvStreamReader reader = null;
				int ms1Spectra = 0;
				int ms2Spectra = 0;
				int ms3PlusSpectra = 0;
				String instrumentName = "<unknown>";
				String instrumentSerialNumber = "<unknown>";
				Date startTime = null;
				double runTimeInSeconds = 0.0;
				String comment = "";
				try {
					reader = new TsvStreamReader(rawFileMetaData.getInfo());
					final int[] indices = {0, 1};
					ArrayList<String> values = new ArrayList<String>(2);
					while (reader.nextValues(indices, values)) {
						final String key = values.size() > 0 ? values.get(0).trim() : "";
						final String value = values.size() > 1 ? values.get(1).trim() : "";
						if ("Creation Date".equals(key)) {
							try {
								startTime = DATE_FORMAT.parse(value);
							} catch (ParseException e) {
								throw new MprcException("Malformed .RAW file start date: " + value, e);
							}
						} else if ("MS1 Spectra".equals(key)) {
							ms1Spectra = Integer.parseInt(value);
						} else if ("MS2 Spectra".equals(key)) {
							ms2Spectra = Integer.parseInt(value);
						} else if ("MS3+ Spectra".equals(key)) {
							ms3PlusSpectra = Integer.parseInt(value);
						} else if ("Instrument Serial".equals(key)) {
							instrumentSerialNumber = value;
						} else if ("Instrument Name".equals(key)) {
							instrumentName = value;
						} else if ("Run Time (seconds)".equals(key)) {
							runTimeInSeconds = Double.parseDouble(value);
						} else if ("Comment".equals(key)) {
							comment = value;
						}
					}
				} finally {
					FileUtilities.closeQuietly(reader);
				}

				return new TandemMassSpectrometrySample(
						rawFileMetaData.getRawFile(),
						new Date(rawFileMetaData.getRawFile().lastModified()),
						ms1Spectra,
						ms2Spectra,
						ms3PlusSpectra,
						instrumentSerialNumber,
						startTime,
						runTimeInSeconds,
						comment,
						Files.toString(rawFileMetaData.getTuneMethod(), Charsets.ISO_8859_1),
						Files.toString(rawFileMetaData.getInstrumentMethod(), Charsets.ISO_8859_1),
						Files.toString(rawFileMetaData.getSampleInformation(), Charsets.ISO_8859_1),
						Files.toString(rawFileMetaData.getErrorLog(), Charsets.ISO_8859_1)
				);
			} catch (IOException e) {
				throw new MprcException("Could not load metadata for raw file: [" + rawFileMetaData.getRawFile().getAbsolutePath() + "]", e);
			}
		}
	}
}
