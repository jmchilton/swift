package edu.mayo.mprc.swift.dbmapping;

import edu.mayo.mprc.database.PersistableBase;

import java.io.File;

/**
 * Information about how to search a single input file.
 */
public class FileSearch extends PersistableBase {
	/**
	 * File to be searched.
	 */
	private File inputFile;
	/**
	 * Biological sample name (Scaffold column).
	 */
	private String biologicalSample;
	/**
	 * Category name (several samples can belong to one category). When null, the category is "none".
	 */
	private String categoryName;
	/**
	 * Experiment name (Scaffold file).
	 */
	private String experiment;

	/**
	 * Set of requested search engines.
	 */
	private EnabledEngines enabledEngines;

	private Integer swiftSearchDefinitionId;

	public FileSearch() {
	}

	public FileSearch(File inputFile, String biologicalSample, String categoryName, String experiment, EnabledEngines enabledEngines) {
		this.inputFile = inputFile;
		this.biologicalSample = biologicalSample;
		this.categoryName = categoryName;
		this.experiment = experiment;
		this.enabledEngines = enabledEngines;
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public String getBiologicalSample() {
		return biologicalSample;
	}

	void setBiologicalSample(String biologicalSample) {
		this.biologicalSample = biologicalSample;
	}

	public String getCategoryName() {
		return categoryName;
	}

	void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getExperiment() {
		return experiment;
	}

	void setExperiment(String experiment) {
		this.experiment = experiment;
	}

	public EnabledEngines getEnabledEngines() {
		return enabledEngines;
	}

	public void setEnabledEngines(EnabledEngines enabledEngines) {
		this.enabledEngines = enabledEngines;
	}

	public Integer getSwiftSearchDefinitionId() {
		return swiftSearchDefinitionId;
	}

	public void setSwiftSearchDefinitionId(Integer id) {
		this.swiftSearchDefinitionId = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof FileSearch)) {
			return false;
		}

		FileSearch that = (FileSearch) o;

		if (getBiologicalSample() != null ? !getBiologicalSample().equals(that.getBiologicalSample()) : that.getBiologicalSample() != null) {
			return false;
		}
		if (getCategoryName() != null ? !getCategoryName().equals(that.getCategoryName()) : that.getCategoryName() != null) {
			return false;
		}
		if (getEnabledEngines() != null ? !getEnabledEngines().equals(that.getEnabledEngines()) : that.getEnabledEngines() != null) {
			return false;
		}
		if (getExperiment() != null ? !getExperiment().equals(that.getExperiment()) : that.getExperiment() != null) {
			return false;
		}
		if (getInputFile() != null ? !getInputFile().equals(that.getInputFile()) : that.getInputFile() != null) {
			return false;
		}
		if (getSwiftSearchDefinitionId() != null ? !getSwiftSearchDefinitionId().equals(that.getSwiftSearchDefinitionId()) : that.getSwiftSearchDefinitionId() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = getInputFile() != null ? getInputFile().hashCode() : 0;
		result = 31 * result + (getBiologicalSample() != null ? getBiologicalSample().hashCode() : 0);
		result = 31 * result + (getCategoryName() != null ? getCategoryName().hashCode() : 0);
		result = 31 * result + (getExperiment() != null ? getExperiment().hashCode() : 0);
		result = 31 * result + (getEnabledEngines() != null ? getEnabledEngines().hashCode() : 0);
		result = 31 * result + (getSwiftSearchDefinitionId() != null ? getSwiftSearchDefinitionId().hashCode() : 0);
		return result;
	}

	public boolean isSearch(String searchEngineCode) {
		return enabledEngines != null && enabledEngines.isEnabled(searchEngineCode);
	}
}
