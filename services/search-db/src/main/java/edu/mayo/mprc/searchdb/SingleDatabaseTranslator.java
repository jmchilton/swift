package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.searchdb.dao.ProteinSequence;
import edu.mayo.mprc.searchdb.dao.SearchDbDao;
import edu.mayo.mprc.utilities.FileUtilities;

/**
 * Caches the first database it is asked to translate. Every subsequent database will be checked for identity,
 * if it does not match, error is thrown.
 */
class SingleDatabaseTranslator implements ProteinSequenceTranslator {
    private CurationDao curationDao;
    private SearchDbDao searchDbDao;
    private Curation database;
    private String currentDatabaseSources;

    SingleDatabaseTranslator(SearchDbDao searchDbDao, CurationDao curationDao) {
        this.searchDbDao = searchDbDao;
        this.curationDao = curationDao;
    }

    @Override
    public ProteinSequence getProteinSequence(String accessionNumber, String databaseSources) {
        if (database == null) {
            if (databaseSources.contains(",")) {
                throw new MprcException("Multiple databases per Scaffold file not supported: [" + databaseSources + "]");
            }
            currentDatabaseSources = databaseSources;
            database = curationDao.getCurationByShortName(getDatabaseName(databaseSources));
        } else if (!databaseSources.equals(currentDatabaseSources)) {
            throw new MprcException("Swift supports only a single FASTA database per Scaffold file. Two databases encountered: [" + currentDatabaseSources + "] and [" + databaseSources + "]");
        }
        return searchDbDao.getProteinSequence(database, accessionNumber);
    }

    /**
     * Get database name by trimming the extension (.fasta or .fasta.gz)
     *
     * @param databaseFile Name of the FASTA database file.
     * @return The database name itself, without extension.
     */
    private String getDatabaseName(String databaseFile) {
        String extension = FileUtilities.getGzippedExtension(databaseFile);
        return databaseFile.substring(0, databaseFile.length() - extension.length() - 1);
    }
}
