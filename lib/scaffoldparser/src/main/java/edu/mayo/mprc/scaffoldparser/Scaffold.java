package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("Scaffold")
public final class Scaffold {

	@XStreamAlias("version")
	@XStreamAsAttribute
	private String version;

	@XStreamImplicit
	private List<Experiment> experiments;

	public Scaffold() {
	}

	public Scaffold(String version, List<Experiment> experiments) {
		this.version = version;
		this.experiments = experiments;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<Experiment> getExperiments() {
		return experiments;
	}

	public void setExperiments(List<Experiment> experiments) {
		this.experiments = experiments;
	}
}
