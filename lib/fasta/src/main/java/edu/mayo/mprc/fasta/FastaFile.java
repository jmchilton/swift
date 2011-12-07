package edu.mayo.mprc.fasta;

import java.io.File;
import java.io.Serializable;

/**
 * FASTA file + metadata that specify how to handle it.
 */
public final class FastaFile implements Serializable {
	private static final long serialVersionUID = 20111021L;

	private String name;
	private String description;
	private File file;
	private DatabaseAnnotation annotation;

	public FastaFile(String name, String description, File file, DatabaseAnnotation annotation) {
		this.name = name;
		this.description = description;
		this.file = file;
		this.annotation = annotation;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public File getFile() {
		return file;
	}

	public DatabaseAnnotation getAnnotation() {
		return annotation;
	}
}
