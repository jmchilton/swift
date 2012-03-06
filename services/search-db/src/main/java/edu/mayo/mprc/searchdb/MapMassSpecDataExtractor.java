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

	private final Map<String/*msmsSampleName*/, RawFileMetaData> metaDataMap;

	public MapMassSpecDataExtractor(Map<String, RawFileMetaData> metaDataMap) {
		this.metaDataMap = metaDataMap;
	}

	@Override
	public TandemMassSpectrometrySample getTandemMassSpectrometrySample(String biologicalSampleName, String msmsSampleName) {
		final RawFileMetaData rawFileMetaData = metaDataMap.get(msmsSampleName);
		if (rawFileMetaData == null) {
			return new TandemMassSpectrometrySample(null, null, 0, 0, 0, null, null, null, 0.0, null, null, null, null, null);
		} else {
			try {
				InfoFileParser parser = new InfoFileParser();
				InfoFileData data = parser.parse(rawFileMetaData.getInfo());
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
}
