package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import edu.mayo.mprc.MprcException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@XStreamAlias("SpectrumAnalysisIdentification")
public final class SpectrumAnalysisIdentification {

	@XStreamAlias("spectrum")
	@XStreamAsAttribute
	private String spectrum;

	@XStreamAlias("analysisProgram")
	@XStreamAsAttribute
	private String analysisProgram;

	@XStreamAlias("charge")
	@XStreamAsAttribute
	private int charge;

	private static final Pattern SPECTRUM_PATTERN = Pattern.compile("[^(]*\\((.*)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\.dta\\)");
	private static final Pattern SPECTRUM_FILE_PATTERN = Pattern.compile("(.*)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\.dta");

	public SpectrumAnalysisIdentification() {
	}

	public String getSpectrum() {
		return spectrum;
	}

	public void setSpectrum(final String spectrum) {
		this.spectrum = spectrum;
	}

	public String getAnalysisProgram() {
		return analysisProgram;
	}

	public void setAnalysisProgram(final String analysisProgram) {
		this.analysisProgram = analysisProgram;
	}

	public int getCharge() {
		return charge;
	}

	public void setCharge(final int charge) {
		this.charge = charge;
	}

	public int getSpectrumNumber() {
		return getSpectrumNumber(spectrum);
	}

	public String getSpectrumName() {
		return getSpectrumName(spectrum);
	}

	static int getSpectrumNumber(final String spectrum) {
		final Matcher matcher = SPECTRUM_PATTERN.matcher(spectrum);
		if (matcher.matches()) {
			return Integer.parseInt(matcher.group(2));
		}
		final Matcher matcher2 = SPECTRUM_FILE_PATTERN.matcher(spectrum);
		if (matcher2.matches()) {
			return Integer.parseInt(matcher2.group(2));
		}
		throw new MprcException("Cannot parse spectrum number from the string " + spectrum);
	}

	static String getSpectrumName(final String spectrum) {
		final Matcher matcher = SPECTRUM_PATTERN.matcher(spectrum);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		final Matcher matcher2 = SPECTRUM_FILE_PATTERN.matcher(spectrum);
		if (matcher2.matches()) {
			return matcher2.group(1);
		}
		throw new MprcException("Cannot parse spectrum name from the string " + spectrum);
	}
}
