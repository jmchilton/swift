package edu.mayo.mprc.fasta;

import java.io.Serializable;

/**
 * Given a FASTA database, how to determine what is accession, what is description and which proteins are decoys.
 */
public final class DatabaseAnnotation implements Serializable {
	private static final long serialVersionUID = 20110120L;

	/**
	 * How to parse accession from a fasta header
	 */
	public static final String DEFAULT_ACCESSION_REGEX = "^>([^ ]+)";

	/**
	 * How to parse description from a fasta header
	 */
	public static final String DEFAULT_DESCRIPTION_REGEX = "^>[^ ]+ (.*)";

	/**
	 * How to determine which FASTA accession numbers correspond to a decoy protein.
	 */
	public static final String DEFAULT_DECOY_REGEX = "Reversed_";

	private String accessionRegex;
	private String descriptionRegex;
	private String decoyRegex;

	public DatabaseAnnotation() {
		this(DEFAULT_ACCESSION_REGEX, DEFAULT_DESCRIPTION_REGEX, DEFAULT_DECOY_REGEX);
	}

	public DatabaseAnnotation(final String decoyRegex) {
		this(DEFAULT_ACCESSION_REGEX, DEFAULT_DESCRIPTION_REGEX, decoyRegex == null ? DEFAULT_DECOY_REGEX : decoyRegex);
	}

	public DatabaseAnnotation(final String accessionRegex, final String descriptionRegex, final String decoyRegex) {
		this.accessionRegex = accessionRegex;
		this.descriptionRegex = descriptionRegex;
		this.decoyRegex = decoyRegex;
	}

	public String getAccessionRegex() {
		return accessionRegex;
	}

	public String getDescriptionRegex() {
		return descriptionRegex;
	}

	public String getDecoyRegex() {
		return decoyRegex;
	}
}
