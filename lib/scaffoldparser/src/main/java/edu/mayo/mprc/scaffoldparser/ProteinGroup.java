package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("ProteinGroup")
public final class ProteinGroup {
	@XStreamAlias("matchProbability")
	@XStreamAsAttribute
	private double matchProbability;

	@XStreamAlias("numberUniqueSpectrumMatches")
	@XStreamAsAttribute
	private int numberUniqueSpectrumMatches;

	@XStreamAlias("preferredProteinAnnotation")
	private PreferredProteinAnnotation preferredProteinAnnotation;

	@XStreamImplicit
	private List<ProteinAnalysisIdentification> proteinAnalysisIdentifications;

	public double getMatchProbability() {
		return matchProbability;
	}

	public void setMatchProbability(final double matchProbability) {
		this.matchProbability = matchProbability;
	}

	public int getNumberUniqueSpectrumMatches() {
		return numberUniqueSpectrumMatches;
	}

	public void setNumberUniqueSpectrumMatches(final int numberUniqueSpectrumMatches) {
		this.numberUniqueSpectrumMatches = numberUniqueSpectrumMatches;
	}

	public PreferredProteinAnnotation getPreferredProteinAnnotation() {
		return preferredProteinAnnotation;
	}

	public void setPreferredProteinAnnotation(final PreferredProteinAnnotation preferredProteinAnnotation) {
		this.preferredProteinAnnotation = preferredProteinAnnotation;
	}

	public List<ProteinAnalysisIdentification> getProteinAnalysisIdentifications() {
		return proteinAnalysisIdentifications;
	}

	public void setProteinAnalysisIdentifications(final List<ProteinAnalysisIdentification> proteinAnalysisIdentifications) {
		this.proteinAnalysisIdentifications = proteinAnalysisIdentifications;
	}
}
