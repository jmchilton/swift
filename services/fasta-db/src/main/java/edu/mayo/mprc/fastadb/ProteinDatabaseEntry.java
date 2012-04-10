package edu.mayo.mprc.fastadb;

import edu.mayo.mprc.database.PersistableBase;
import edu.mayo.mprc.dbcurator.model.Curation;

/**
 * One entry in a particular protein database.
 *
 * @author Roman Zenka
 */
public final class ProteinDatabaseEntry extends PersistableBase {
	/**
	 * Database this entry belongs to.
	 */
	private Curation database;

	/**
	 * Accession number the entry belongs to.
	 */
	private String accessionNumber;

	/**
	 * Description of the database entry.
	 */
	private String description;

	/**
	 * The protein sequence of the entry.
	 */
	private ProteinSequence sequence;

	public ProteinDatabaseEntry() {
	}

	public ProteinDatabaseEntry(final Curation database, final String accessionNumber, final String description, final ProteinSequence sequence) {
		setDatabase(database);
		setAccessionNumber(accessionNumber);
		setDescription(description);
		setSequence(sequence);
	}

	public Curation getDatabase() {
		return database;
	}

	void setDatabase(final Curation database) {
		this.database = database;
	}

	public String getAccessionNumber() {
		return accessionNumber;
	}

	void setAccessionNumber(final String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public ProteinSequence getSequence() {
		return sequence;
	}

	void setSequence(final ProteinSequence sequence) {
		this.sequence = sequence;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (null == o || !(o instanceof ProteinDatabaseEntry)) {
			return false;
		}

		final ProteinDatabaseEntry that = (ProteinDatabaseEntry) o;

		if (null != getAccessionNumber() ? !getAccessionNumber().equals(that.getAccessionNumber()) : null != that.getAccessionNumber()) {
			return false;
		}
		if (null != getDatabase() ? !getDatabase().equals(that.getDatabase()) : null != that.getDatabase()) {
			return false;
		}
		if (null != getDescription() ? !getDescription().equals(that.getDescription()) : null != that.getDescription()) {
			return false;
		}
		if (null != getSequence() ? !getSequence().equals(that.getSequence()) : null != that.getSequence()) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = null != getDatabase() ? getDatabase().hashCode() : 0;
		result = 31 * result + (null != getAccessionNumber() ? getAccessionNumber().hashCode() : 0);
		result = 31 * result + (null != getDescription() ? getDescription().hashCode() : 0);
		result = 31 * result + (null != getSequence() ? getSequence().hashCode() : 0);
		return result;
	}
}
