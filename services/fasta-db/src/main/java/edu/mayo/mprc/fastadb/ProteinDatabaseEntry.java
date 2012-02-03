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
        this.database = database;
        this.accessionNumber = accessionNumber;
        this.sequence = sequence;
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
}
