package edu.mayo.mprc.qa;

import java.util.LinkedList;

/**
 * MS/MS specific data. This corresponds to an entry in a Mgf file.
 */
public final class MgfSpectrum {
	private String spectrumName;
	private double mgfMz;
	private int mgfCharge;
	private long scanId;
	private String mgfFileName;
	private long spectrumNumber;
	private LinkedList<String> scaffoldInfos;

	public MgfSpectrum(String spectrumName) {
		this.spectrumName = spectrumName;

		scaffoldInfos = new LinkedList<String>();
	}

	public MgfSpectrum(String spectrumName, double mgfMz, int mgfCharge, long scanId, String mgfFileName, long spectrumNumber) {
		this(spectrumName);
		this.mgfMz = mgfMz;
		this.mgfCharge = mgfCharge;
		this.scanId = scanId;
		this.mgfFileName = mgfFileName;
		this.spectrumNumber = spectrumNumber;
	}

	public String getMgfFileName() {
		return mgfFileName;
	}

	public void setMgfFileName(String mgfFileName) {
		this.mgfFileName = mgfFileName;
	}

	/**
	 * @return Spectra within .mgf file are numbered, starting with spectrum #0. This returns the number of the spectrum.
	 */
	public long getSpectrumNumber() {
		return spectrumNumber;
	}

	public void setSpectrumNumber(long spectrumNumber) {
		this.spectrumNumber = spectrumNumber;
	}

	public long getScanId() {
		return scanId;
	}

	public void setScanId(long scanId) {
		this.scanId = scanId;
	}

	public void addScaffoldInfo(String scaffoldInfo) {
		scaffoldInfos.add(scaffoldInfo);
	}

	public LinkedList<String> getScaffoldInfos() {
		return scaffoldInfos;
	}

	public String getSpectrumName() {
		return spectrumName;
	}

	public double getMgfMz() {
		return mgfMz;
	}

	public void setMgfMz(double mgfMz) {
		this.mgfMz = mgfMz;
	}

	public int getMgfCharge() {
		return mgfCharge;
	}

	public void setMgfCharge(int mgfCharge) {
		this.mgfCharge = mgfCharge;
	}
}
