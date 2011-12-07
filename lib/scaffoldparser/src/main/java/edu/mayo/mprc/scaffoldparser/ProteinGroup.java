package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("ProteinGroup")
public final class ProteinGroup {
	@XStreamAlias("preferredProteinAnnotation")
	private PreferredProteinAnnotation preferredProteinAnnotation;

	@XStreamImplicit
	private List<ProteinAnalysisIdentification> proteinAnalysisIdentifications;

	public PreferredProteinAnnotation getPreferredProteinAnnotation() {
		return preferredProteinAnnotation;
	}

	public void setPreferredProteinAnnotation(PreferredProteinAnnotation preferredProteinAnnotation) {
		this.preferredProteinAnnotation = preferredProteinAnnotation;
	}

	public List<ProteinAnalysisIdentification> getProteinAnalysisIdentifications() {
		return proteinAnalysisIdentifications;
	}

	public void setProteinAnalysisIdentifications(List<ProteinAnalysisIdentification> proteinAnalysisIdentifications) {
		this.proteinAnalysisIdentifications = proteinAnalysisIdentifications;
	}
}
