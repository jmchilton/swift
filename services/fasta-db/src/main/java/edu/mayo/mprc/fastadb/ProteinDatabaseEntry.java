package edu.mayo.mprc.fastadb;

import edu.mayo.mprc.database.PersistableBase;
import edu.mayo.mprc.dbcurator.model.Curation;

/**
 * One entry in a particular protein database.
 *
 * @author Roman Zenka
 */
public class ProteinDatabaseEntry extends PersistableBase {
    /**
     * Database this entry belongs to.
     */
    private Curation database;

    /**
     * Accession number the entry belongs to.
     */
    private String accessionNumber;

    /**
     * The protein sequence of the entry.
     */
    private ProteinSequence sequence;

    public ProteinDatabaseEntry() {
    }

    public ProteinDatabaseEntry(Curation database, String accessionNumber, ProteinSequence sequence) {
        this.setDatabase(database);
        this.setAccessionNumber(accessionNumber);
        this.setSequence(sequence);
    }

    public Curation getDatabase() {
        return database;
    }

    void setDatabase(Curation database) {
        this.database = database;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public ProteinSequence getSequence() {
        return sequence;
    }

    void setSequence(ProteinSequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProteinDatabaseEntry that = (ProteinDatabaseEntry) o;

        if (getAccessionNumber() != null ? !getAccessionNumber().equals(that.getAccessionNumber()) : that.getAccessionNumber() != null)
            return false;
        if (getDatabase() != null ? !getDatabase().equals(that.getDatabase()) : that.getDatabase() != null)
            return false;
        if (getSequence() != null ? !getSequence().equals(that.getSequence()) : that.getSequence() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getDatabase() != null ? getDatabase().hashCode() : 0;
        result = 31 * result + (getAccessionNumber() != null ? getAccessionNumber().hashCode() : 0);
        result = 31 * result + (getSequence() != null ? getSequence().hashCode() : 0);
        return result;
    }
}
