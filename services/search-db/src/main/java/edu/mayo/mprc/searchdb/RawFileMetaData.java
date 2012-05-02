package edu.mayo.mprc.searchdb;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.files.FileHolder;
import edu.mayo.mprc.searchdb.dao.TandemMassSpectrometrySample;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

/**
 * A list of raw file metadata stored as many data files.
 *
 * @author Roman Zenka
 */
public class RawFileMetaData extends FileHolder {
	private File rawFile;
	private File info;
	private File tuneMethod;
	private File instrumentMethod;
	private File sampleInformation;
	private File errorLog;

	public RawFileMetaData(final File rawFile, final File info, final File tuneMethod, final File instrumentMethod, final File sampleInformation, final File errorLog) {
		this.rawFile = rawFile;
		this.info = info;
		this.tuneMethod = tuneMethod;
		this.instrumentMethod = instrumentMethod;
		this.sampleInformation = sampleInformation;
		this.errorLog = errorLog;
	}

	/**
	 * Parses the raw file metadata into {@link edu.mayo.mprc.searchdb.dao.TandemMassSpectrometrySample} object.
	 * @return Parsed data
	 */
	public TandemMassSpectrometrySample parse() {
		try {
			final InfoFileParser parser = new InfoFileParser();
			final InfoFileData data = parser.parse(getInfo());
			return new TandemMassSpectrometrySample(
					getRawFile(),
					new DateTime(getRawFile().lastModified()),
					data.getMs1Spectra(),
					data.getMs2Spectra(),
					data.getMs3PlusSpectra(),
					data.getInstrumentName(),
					data.getInstrumentSerialNumber(),
					data.getStartTime(),
					data.getRunTimeInSeconds(),
					data.getComment(),
					Files.toString(getTuneMethod(), Charsets.ISO_8859_1),
					Files.toString(getInstrumentMethod(), Charsets.ISO_8859_1),
					Files.toString(getSampleInformation(), Charsets.ISO_8859_1),
					Files.toString(getErrorLog(), Charsets.ISO_8859_1)
			);
		} catch (IOException e) {
			throw new MprcException("Could not parse metadata for raw file: [" + getRawFile().getAbsolutePath() + "]", e);
		}
	}

	public File getRawFile() {
		return rawFile;
	}

	public File getInfo() {
		return info;
	}

	public File getTuneMethod() {
		return tuneMethod;
	}

	public File getInstrumentMethod() {
		return instrumentMethod;
	}

	public File getSampleInformation() {
		return sampleInformation;
	}

	public File getErrorLog() {
		return errorLog;
	}
}
