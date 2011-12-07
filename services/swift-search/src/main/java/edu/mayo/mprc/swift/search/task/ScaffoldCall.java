package edu.mayo.mprc.swift.search.task;

class ScaffoldCall {
	private final String experiment;
	private final String version;

	ScaffoldCall(String experiment, String version) {
		this.experiment = experiment;
		this.version = version;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ScaffoldCall that = (ScaffoldCall) o;

		if (experiment != null ? !experiment.equals(that.experiment) : that.experiment != null) {
			return false;
		}
		if (version != null ? !version.equals(that.version) : that.version != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = experiment != null ? experiment.hashCode() : 0;
		result = 31 * result + (version != null ? version.hashCode() : 0);
		return result;
	}
}
