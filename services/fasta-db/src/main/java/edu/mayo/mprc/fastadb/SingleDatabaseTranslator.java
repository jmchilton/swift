package edu.mayo.mprc.fastadb;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.utilities.FileUtilities;

/**
 * Caches the first database it is asked to translate. Every subsequent database will be checked for identity,
 * if it does not match, error is thrown.
 */
public class SingleDatabaseTranslator implements ProteinSequenceTranslator {
    private FastaDbDao fastaDbDao;
    private CurationDao curationDao;
    private Curation database;
    private String currentDatabaseSources;

    public SingleDatabaseTranslator(FastaDbDao fastaDbDao, CurationDao curationDao) {
        this.fastaDbDao = fastaDbDao;
        this.curationDao = curationDao;
    }

    @Override
    public ProteinSequence getProteinSequence(String accessionNumber, String databaseSources) {
        if (database == null) {
            if (databaseSources.contains(",")) {
                throw new MprcException("Multiple databases per Scaffold file not supported: [" + databaseSources + "]");
            }
            currentDatabaseSources = getDatabaseName(databaseSources);
            database = curationDao.getCurationByShortName(currentDatabaseSources);
        } else if (!getDatabaseName(databaseSources).equals(currentDatabaseSources)) {
            throw new MprcException("Swift supports only a single FASTA database per Scaffold file. Two databases encountered: [" + currentDatabaseSources + "] and [" + databaseSources + "]");
        }
        return fastaDbDao.getProteinSequence(database, accessionNumber);
    }

    /**
     * Get database name by trimming the extension (.fasta or .fasta.gz)
     *
     * @param databaseFile Name of the FASTA database file.
     * @return The database name itself, without extension.
     */
    private String getDatabaseName(String databaseFile) {
        String extension = FileUtilities.getGzippedExtension(databaseFile);
        return databaseFile.substring(0, databaseFile.length() - extension.length() - (/*dot*/extension.length() > 0 ? 1 : 0));
    }
}
