package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("PeptideGroupIdentification")
public final class PeptideGroupIdentification {

	@XStreamImplicit
	private List<PeptideAnalysisIdentification> peptideAnalysisIdentifications;

	public PeptideGroupIdentification() {
	}

	public List<PeptideAnalysisIdentification> getPeptideAnalysisIdentifications() {
		return peptideAnalysisIdentifications;
	}

	public void setPeptideAnalysisIdentifications(final List<PeptideAnalysisIdentification> peptideAnalysisIdentifications) {
		this.peptideAnalysisIdentifications = peptideAnalysisIdentifications;
	}
}
