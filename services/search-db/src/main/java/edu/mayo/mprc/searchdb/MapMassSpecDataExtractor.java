package edu.mayo.mprc.searchdb;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.searchdb.dao.TandemMassSpectrometrySample;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Map;

/**
 * Extract mass spec data from a map of {@link RawFileMetaData} objects embedded in the work packet.
 *
 * @author Roman Zenka
 */
public class MapMassSpecDataExtractor implements MassSpecDataExtractor {

	public static final String MUDPIT_PREFIX = "Mudpit_";
	private final Map<String/*msmsSampleName*/, RawFileMetaData> metaDataMap;

	public MapMassSpecDataExtractor(final Map<String, RawFileMetaData> metaDataMap) {
		this.metaDataMap = metaDataMap;
	}

	@Override
	public TandemMassSpectrometrySample getTandemMassSpectrometrySample(final String biologicalSampleName, final String msmsSampleName) {
		final RawFileMetaData rawFileMetaData = getMetadata(msmsSampleName);
		if (rawFileMetaData == null) {
			return new TandemMassSpectrometrySample(null, null, 0, 0, 0, null, null, null, 0.0, null, null, null, null, null);
		} else {
			try {
				final InfoFileParser parser = new InfoFileParser();
				final InfoFileData data = parser.parse(rawFileMetaData.getInfo());
				return new TandemMassSpectrometrySample(
						rawFileMetaData.getRawFile(),
						new DateTime(rawFileMetaData.getRawFile().lastModified()),
						data.getMs1Spectra(),
						data.getMs2Spectra(),
						data.getMs3PlusSpectra(),
						data.getInstrumentName(),
						data.getInstrumentSerialNumber(),
						data.getStartTime(),
						data.getRunTimeInSeconds(),
						data.getComment(),
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

	/**
	 * Scaffold seems to have an unpleasant habit of sometimes prefixing the name of the msms sample with "Mudpit_".
	 * It is not clear what that means.
	 *
	 * @param msmsSampleName Name of the ms/ms sample.
	 * @return Metadata about the matching .RAW file.
	 */
	private RawFileMetaData getMetadata(final String msmsSampleName) {
		final RawFileMetaData metaData = metaDataMap.get(msmsSampleName);
		if (metaData != null) {
			return metaData;
		}
		if (msmsSampleName.startsWith(MUDPIT_PREFIX)) {
			final RawFileMetaData mudpitMetaData = metaDataMap.get(msmsSampleName.substring(MUDPIT_PREFIX.length()));
			if (mudpitMetaData != null) {
				return mudpitMetaData;
			}
		}
		return null;
	}
}
