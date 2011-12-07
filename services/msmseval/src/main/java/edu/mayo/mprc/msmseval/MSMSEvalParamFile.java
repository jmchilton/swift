package edu.mayo.mprc.msmseval;

/**
 * Information about a parameter file for msmsEval. Contains description and full path to the file.
 */
public final class MSMSEvalParamFile {
	private String path;
	private String description;

	public MSMSEvalParamFile(String path, String description) {
		this.path = path;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getPath() {
		return path;
	}
}
