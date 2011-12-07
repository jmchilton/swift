package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("TandemMassSpectrometrySample")
public final class TandemMassSpectrometrySample {
	@XStreamAlias("fractionName")
	@XStreamAsAttribute
	private String fractionName;

	@XStreamAlias("fractionNumber")
	@XStreamAsAttribute
	private int fractionNumber;

	@XStreamAlias("massSpecMachineName")
	@XStreamAsAttribute
	private String massSpecMachineName;

	@XStreamImplicit
	private List<ProteinGroup> proteinGroups;

	public String getFractionName() {
		return fractionName;
	}

	public void setFractionName(String fractionName) {
		this.fractionName = fractionName;
	}

	public int getFractionNumber() {
		return fractionNumber;
	}

	public void setFractionNumber(int fractionNumber) {
		this.fractionNumber = fractionNumber;
	}

	public String getMassSpecMachineName() {
		return massSpecMachineName;
	}

	public void setMassSpecMachineName(String massSpecMachineName) {
		this.massSpecMachineName = massSpecMachineName;
	}

	public List<ProteinGroup> getProteinGroups() {
		return proteinGroups;
	}

	public void setProteinGroups(List<ProteinGroup> proteinGroups) {
		this.proteinGroups = proteinGroups;
	}
}
