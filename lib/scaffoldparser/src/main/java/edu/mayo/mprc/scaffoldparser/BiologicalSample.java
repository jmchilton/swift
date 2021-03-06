package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("BiologicalSample")
public final class BiologicalSample {
	@XStreamAlias("sampleName")
	@XStreamAsAttribute
	private String sampleName;

	@XStreamAlias("category")
	@XStreamAsAttribute
	private String category;

	@XStreamAlias("description")
	@XStreamAsAttribute
	private String description;

	@XStreamAlias("analyzeAsMudPit")
	@XStreamAsAttribute
	private boolean analyzeAsMudPit;

	@XStreamAlias("note")
	private String note;

	@XStreamImplicit
	private List<TandemMassSpectrometrySample> tandemMassSpectrometrySamples;

	public BiologicalSample() {
	}

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isAnalyzeAsMudPit() {
		return analyzeAsMudPit;
	}

	public void setAnalyzeAsMudPit(boolean analyzeAsMudPit) {
		this.analyzeAsMudPit = analyzeAsMudPit;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public List<TandemMassSpectrometrySample> getTandemMassSpectrometrySamples() {
		return tandemMassSpectrometrySamples;
	}

	public void setTandemMassSpectrometrySamples(List<TandemMassSpectrometrySample> tandemMassSpectrometrySamples) {
		this.tandemMassSpectrometrySamples = tandemMassSpectrometrySamples;
	}
}
