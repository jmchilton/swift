package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("Experiment")
public final class Experiment {

	@XStreamAlias("experimentName")
	@XStreamAsAttribute
	private String experimentName;

	@XStreamAlias("analysisDate")
	@XStreamAsAttribute
	private String analysisDate;

	@XStreamImplicit
	private List<DisplayThresholds> displayThresholds;

	@XStreamImplicit
	private List<BiologicalSample> biologicalSamples;

	public String getExperimentName() {
		return experimentName;
	}

	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}

	public String getAnalysisDate() {
		return analysisDate;
	}

	public void setAnalysisDate(String analysisDate) {
		this.analysisDate = analysisDate;
	}

	public List<DisplayThresholds> getDisplayThresholds() {
		return displayThresholds;
	}

	public void setDisplayThresholds(List<DisplayThresholds> displayThresholds) {
		this.displayThresholds = displayThresholds;
	}

	public List<BiologicalSample> getBiologicalSamples() {
		return biologicalSamples;
	}

	public void setBiologicalSamples(List<BiologicalSample> biologicalSamples) {
		this.biologicalSamples = biologicalSamples;
	}
}
