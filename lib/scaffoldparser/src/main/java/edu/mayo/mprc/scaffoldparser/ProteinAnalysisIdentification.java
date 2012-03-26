package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("ProteinAnalysisIdentification")
public final class ProteinAnalysisIdentification {

	@XStreamAlias("accessionNumber")
	@XStreamAsAttribute
	private String accessionNumber;

	@XStreamAlias("proteinName")
	@XStreamAsAttribute
	private String proteinName;

	@XStreamAlias("matchProbability")
	@XStreamAsAttribute
	private double matchProbability;

	@XStreamAlias("sequenceCoverageFraction")
	@XStreamAsAttribute
	private double sequenceCoverageFraction;

	@XStreamImplicit
	private List<PeptideGroupIdentification> peptideGroupIdentifications;

	public ProteinAnalysisIdentification() {
	}

	public String getAccessionNumber() {
		return accessionNumber;
	}

	public void setAccessionNumber(final String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public String getProteinName() {
		return proteinName;
	}

	public void setProteinName(final String proteinName) {
		this.proteinName = proteinName;
	}

	public double getMatchProbability() {
		return matchProbability;
	}

	public void setMatchProbability(final double matchProbability) {
		this.matchProbability = matchProbability;
	}

	public double getSequenceCoverageFraction() {
		return sequenceCoverageFraction;
	}

	public void setSequenceCoverageFraction(final double sequenceCoverageFraction) {
		this.sequenceCoverageFraction = sequenceCoverageFraction;
	}

	public List<PeptideGroupIdentification> getPeptideGroupIdentifications() {
		return peptideGroupIdentifications;
	}

	public void setPeptideGroupIdentifications(final List<PeptideGroupIdentification> peptideGroupIdentifications) {
		this.peptideGroupIdentifications = peptideGroupIdentifications;
	}
}
