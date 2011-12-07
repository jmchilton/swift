package edu.mayo.mprc.swift.dbmapping;

import edu.mayo.mprc.database.PersistableBase;

import java.io.File;

public class SpectrumQa extends PersistableBase {
	/**
	 * Name of the engine to perform the spectrum QA with.
	 */
	private String engine;

	/**
	 * Path to a parameter file defining how should the engine process the data.
	 * Relative to Swift install folder.
	 */
	private String paramFilePath;

	public static final String DEFAULT_ENGINE = "msmsEval";

	public SpectrumQa() {
	}

	public SpectrumQa(String paramFilePath, String engine) {
		this.paramFilePath = paramFilePath;
		this.engine = engine;
	}

	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}

	public File paramFile() {
		if (paramFilePath == null) {
			return null;
		}
		return new File(paramFilePath);
	}

	public String getParamFilePath() {
		return paramFilePath;
	}

	public void setParamFilePath(String paramFilePath) {
		this.paramFilePath = paramFilePath;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof SpectrumQa)) {
			return false;
		}

		SpectrumQa that = (SpectrumQa) o;

		if (getEngine() != null ? !getEngine().equals(that.getEngine()) : that.getEngine() != null) {
			return false;
		}
		if (getParamFilePath() != null ? !getParamFilePath().equals(that.getParamFilePath() != null ? that.getParamFilePath() : null) : that.getParamFilePath() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = getEngine() != null ? getEngine().hashCode() : 0;
		result = 31 * result + (getParamFilePath() != null ? getParamFilePath().hashCode() : 0);
		return result;
	}
}
