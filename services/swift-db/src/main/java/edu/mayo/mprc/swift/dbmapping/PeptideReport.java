package edu.mayo.mprc.swift.dbmapping;

import edu.mayo.mprc.database.PersistableBase;

/**
 * Settings for generating a peptide report. Currently no settings are needed.
 * This class is basically a singleton (although more instances can exist, they are all equal)
 */
public class PeptideReport extends PersistableBase {

	public PeptideReport() {
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof PeptideReport;
	}

	@Override
	public int hashCode() {
		return 1;
	}
}
