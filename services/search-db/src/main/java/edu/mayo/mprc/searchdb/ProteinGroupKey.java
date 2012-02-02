package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.searchdb.dao.BiologicalSample;
import edu.mayo.mprc.searchdb.dao.SearchResult;

/**
 * A key identifying a protein group. One protein group should be reported only once
 * per a {@link BiologicalSample}, as all the {@link SearchResult} reports
 * for one {@link BiologicalSample} should be merged together.
 *
 * @author Roman Zenka
 */
final class ProteinGroupKey {
    private final BiologicalSample biologicalSample;
    private final String accessionNumbers;
    private final String databaseSources;

    /**
     * @param biologicalSample Biological sample the protein group belongs to.
     * @param accessionNumbers Accession numbers canonicalized, joined by comma
     */
    ProteinGroupKey(BiologicalSample biologicalSample, String accessionNumbers, String databaseSources) {
        this.biologicalSample = biologicalSample;
        this.accessionNumbers = accessionNumbers;
        this.databaseSources = databaseSources;
    }

    public BiologicalSample getBiologicalSample() {
        return biologicalSample;
    }

    public String getAccessionNumbers() {
        return accessionNumbers;
    }

    public String getDatabaseSources() {
        return databaseSources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProteinGroupKey that = (ProteinGroupKey) o;

        if (!accessionNumbers.equals(that.accessionNumbers)) return false;
        if (!biologicalSample.equals(that.biologicalSample)) return false;
        if (!databaseSources.equals(that.databaseSources)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = biologicalSample.hashCode();
        result = 31 * result + accessionNumbers.hashCode();
        result = 31 * result + databaseSources.hashCode();
        return result;
    }
}
