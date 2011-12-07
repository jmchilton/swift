package edu.mayo.mprc.scafml;


import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.files.FileHolder;
import edu.mayo.mprc.utilities.xml.XMLUtilities;

import java.util.LinkedHashMap;

public final class ScafmlBiologicalSample extends FileHolder {
	/**
	 * identifier for biological sample, must be unique within experiment
	 * contains InputFile's
	 */
	private String id;

	private String analyzeAsMudpit;
	private String database;
	private String name;
	private String category;

	private LinkedHashMap<String, ScafmlInputFile> inputFiles;

	public ScafmlBiologicalSample(String id) {
		this.id = id;
		inputFiles = new LinkedHashMap<String, ScafmlInputFile>(5);
	}

	public ScafmlBiologicalSample() {
		this(null);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public ScafmlInputFile getInputFile(String id) {
		return this.inputFiles.get(id);
	}

	public void addInputFile(ScafmlInputFile pInputFile) {
		if (pInputFile == null) {
			throw new MprcException("null object for Input File");
		}
		final String id = pInputFile.getID();
		if (id == null) {
			throw new MprcException("no id for Input File\" object");
		}
		if (this.inputFiles.get(id) == null) {
			this.inputFiles.put(id, pInputFile);
		}
	}


	public void setAnalyzeAsMudpit(String sAnalyzeMudpit) {
		this.analyzeAsMudpit = sAnalyzeMudpit;
	}

	public String getAnalyzeAsMudpit() {
		return analyzeAsMudpit;
	}

	public void setDatabase(String sDatabase) {
		this.database = sDatabase;
	}

	public String getDatabase() {
		return database;
	}

	public void setName(String sName) {
		this.name = sName;
	}

	public String getName() {
		return name;
	}

	public void setCategory(String sCategory) {
		this.category = sCategory;
	}

	public String getCategory() {
		return category;
	}


	public void appendToDocument(StringBuilder result, String indent) {
		if (this.inputFiles.values().isEmpty()) {
			result.append(indent).append("<!-- Biological sample with no input files: ").append(this.getName()).append(" -->\n");
		} else {
			String header = indent + "<" + "BiologicalSample" +
					XMLUtilities.wrapatt("analyzeAsMudpit", this.getAnalyzeAsMudpit()) +
					XMLUtilities.wrapatt("database", this.getDatabase()) +
					XMLUtilities.wrapatt("name", this.getName()) +
					XMLUtilities.wrapatt("category", this.getCategory()) +
					">\n";
			result.append(header);
			// now the input files
			for (ScafmlInputFile inputfile : this.inputFiles.values()) {
				inputfile.appendToDocument(result, indent + '\t');
			}

			result.append(indent).append("</" + "BiologicalSample" + ">\n");
		}
	}
}

