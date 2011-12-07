package edu.mayo.mprc.swift.ui.client.rpc;

import edu.mayo.mprc.swift.dbmapping.FileSearch;

import java.io.Serializable;
import java.util.List;

/**
 * One entry in the table of files. UI equivalent of {@link FileSearch}
 *
 * @author: Roman Zenka
 */
public final class ClientFileSearch implements Serializable {
	private static final long serialVersionUID = 20111119L;
	/**
	 * This is a /-separated path relative to the "raw file root" (eg: instruments/foo/bar.RAW)
	 */
	private String path;
	private String biologicalSample;
	private String categoryName;
	private String experiment;
	private List<String> enabledEngineCodes;
	// Optional - does not have to be filled in. Used to transfer file sizes when search is loaded
	// When negative, it means the file could not be found on the filesystem
	private Long fileSize;

	public ClientFileSearch() {
	}

	/**
	 * Creates new file table entry.
	 *
	 * @param inputFilePath      Path to the input file (relative to browse root}
	 * @param biologicalSample   Name of the biological sample.
	 * @param categoryName       Name of the category.
	 * @param experiment         Name of the experiment.
	 * @param enabledEngineCodes List of engine codes enabled for this file search
	 */
	public ClientFileSearch(String inputFilePath, String biologicalSample, String categoryName, String experiment, List<String> enabledEngineCodes, Long fileSize) {
		path = inputFilePath;
		this.biologicalSample = biologicalSample;
		this.categoryName = categoryName;
		this.experiment = experiment;
		this.enabledEngineCodes = enabledEngineCodes;
		this.fileSize = fileSize;
	}

	public String getPath() {
		return path;
	}

	public String getBiologicalSample() {
		return biologicalSample;
	}

	public String getExperiment() {
		return experiment;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public List<String> getEnabledEngineCodes() {
		return enabledEngineCodes;
	}

	public Long getFileSize() {
		return fileSize;
	}
}
