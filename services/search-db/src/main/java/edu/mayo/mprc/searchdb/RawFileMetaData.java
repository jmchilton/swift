package edu.mayo.mprc.searchdb;

import com.google.common.base.Charsets;
import edu.mayo.mprc.daemon.files.FileHolder;
import edu.mayo.mprc.searchdb.dao.TandemMassSpectrometrySample;
import edu.mayo.mprc.utilities.FileUtilities;
import org.joda.time.DateTime;

import java.io.File;

/**
 * A list of raw file metadata stored as many data files.
 *
 * @author Roman Zenka
 */
public class RawFileMetaData extends FileHolder {
	private static final long serialVersionUID = 8085845860082715912L;
	private File rawFile;
	private File info;
	private File tuneMethod;
	private File instrumentMethod;
	private File sampleInformation;
	private File errorLog;
	/**
	 * Maximum amount of characters to read for the log files
	 */
	private static final int MAX_LENGTH = 65535;

	public RawFileMetaData(final File rawFile, final File info, final File tuneMethod, final File instrumentMethod, final File sampleInformation, final File errorLog) {
		this.rawFile = rawFile;
		this.info = info;
		this.tuneMethod = tuneMethod;
		this.instrumentMethod = instrumentMethod;
		this.sampleInformation = sampleInformation;
		this.errorLog = errorLog;
	}

	/**
	 * Parses the raw file metadata into {@link TandemMassSpectrometrySample} object.
	 *
	 * @return Parsed data
	 */
	public TandemMassSpectrometrySample parse() {
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
				FileUtilities.toString(getTuneMethod(), Charsets.ISO_8859_1, MAX_LENGTH),
				FileUtilities.toString(getInstrumentMethod(), Charsets.ISO_8859_1, MAX_LENGTH),
				FileUtilities.toString(getSampleInformation(), Charsets.ISO_8859_1, MAX_LENGTH),
				FileUtilities.toString(getErrorLog(), Charsets.ISO_8859_1, MAX_LENGTH)
		);
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
