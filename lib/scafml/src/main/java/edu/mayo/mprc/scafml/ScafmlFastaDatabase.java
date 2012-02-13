package edu.mayo.mprc.scafml;

import edu.mayo.mprc.daemon.files.FileHolder;
import edu.mayo.mprc.fasta.DatabaseAnnotation;
import edu.mayo.mprc.utilities.xml.XMLUtilities;

import java.io.File;

public final class ScafmlFastaDatabase extends FileHolder {
	private static final long serialVersionUID = -998645023951149560L;
	private String id;
	private File database;
	private DatabaseAnnotation annotation;

	public ScafmlFastaDatabase(DatabaseAnnotation annotation) {
		this.annotation = annotation;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public File getDatabase() {
		return database;
	}

	public void setDatabase(File database) {
		this.database = database;
	}

	public void appendToDocument(StringBuilder result, String indent, boolean reportDecoyHits) {
		result.append(indent)
				.append("<" + "FastaDatabase")
				.append(" id=\"")
				.append(this.getId())
				.append("\" path=\"")
				.append(this.getDatabase().getAbsolutePath())
				.append("\"")
				.append(XMLUtilities.wrapatt("databaseAccessionRegEx", annotation.getAccessionRegex()))
				.append(XMLUtilities.wrapatt("databaseDescriptionRegEx", annotation.getDescriptionRegex()));
		if (reportDecoyHits) {
			result.append(XMLUtilities.wrapatt("decoyProteinRegEx", annotation.getDecoyRegex()));
		}
		result.append("/>\n");
	}
}
