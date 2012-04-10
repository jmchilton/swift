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

	public SingleDatabaseTranslator(final FastaDbDao fastaDbDao, final CurationDao curationDao) {
		this.fastaDbDao = fastaDbDao;
		this.curationDao = curationDao;
	}

	@Override
	public ProteinSequence getProteinSequence(final String accessionNumber, final String databaseSources) {
		if (database == null) {
			if (databaseSources.contains(",")) {
				throw new MprcException("Multiple databases per Scaffold file not supported: [" + databaseSources + "]");
			}
			currentDatabaseSources = FileUtilities.stripGzippedExtension(databaseSources);
			database = curationDao.getLegacyCuration(currentDatabaseSources);
			if(database==null) {
				throw new MprcException("Cannot find information about database ["+currentDatabaseSources+"]");
			}
		} else if (!FileUtilities.stripGzippedExtension(databaseSources).equals(currentDatabaseSources)) {
			throw new MprcException("Swift supports only a single FASTA database per Scaffold file. Two databases encountered: [" + currentDatabaseSources + "] and [" + databaseSources + "]");
		}
		return fastaDbDao.getProteinSequence(database, accessionNumber);
	}

}
