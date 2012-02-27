package edu.mayo.mprc.searchdb;

import java.io.Serializable;

/**
 * A list of raw file metadata, transferred in string form as this info is small and not worth all the file upload/
 * download hassle.
 *
 * @author Roman Zenka
 */
public class RawFileMetaData implements Serializable {
	private String info;
	private String tuneMethod;
	private String instrumentMethod;
	private String sampleInformation;
	private String errorLog;

	public RawFileMetaData(String info, String tuneMethod, String instrumentMethod, String sampleInformation, String errorLog) {
		this.info = info;
		this.tuneMethod = tuneMethod;
		this.instrumentMethod = instrumentMethod;
		this.sampleInformation = sampleInformation;
		this.errorLog = errorLog;
	}

	public String getInfo() {
		return info;
	}

	public String getTuneMethod() {
		return tuneMethod;
	}

	public String getInstrumentMethod() {
		return instrumentMethod;
	}

	public String getSampleInformation() {
		return sampleInformation;
	}

	public String getErrorLog() {
		return errorLog;
	}
}
