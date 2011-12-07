package edu.mayo.mprc.dbcurator.model.persistence;

import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.Dao;
import edu.mayo.mprc.database.Evolvable;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.FastaSource;
import edu.mayo.mprc.dbcurator.model.HeaderTransform;
import edu.mayo.mprc.dbcurator.model.SourceDatabaseArchive;

import java.util.Date;
import java.util.List;

public interface CurationDao extends Dao {
	Curation getCuration(final int curationID);

	void addCuration(Curation curation);

	void save(SourceDatabaseArchive archive);

	void deleteCuration(Curation curation, Change change);

	FastaSource getDataSourceByName(String urlString);

	FastaSource getDataSourceByUrl(String urlString);

	SourceDatabaseArchive findSourceDatabaseInExistence(String s, Date urlLastMod);

	HeaderTransform getHeaderTransformByUrl(String url);

	HeaderTransform getHeaderTransformByName(String name);

	List<Curation> getMatchingCurations(Curation templateCuration, Date earliestRunDate, Date latestRunDate);

	Curation findCuration(String shortDbName);

	Curation getCurationByShortName(final String uniqueName);

	List<Curation> getCurationsByShortname(String curationShortName);

	List<Curation> getCurationsByShortname(final String shortname, final boolean ignoreCase);

	List<FastaSource> getCommonSources();

	void addHeaderTransform(HeaderTransform sprotTrans);

	List<HeaderTransform> getCommonHeaderTransforms();

	long countAll(Class<? extends Evolvable> clazz);

	long rowCount(Class<?> clazz);

	/**
	 * Adds a "legacy" curation entry into the database. This is to support importing legacy data into Swift.
	 * We know the curation name, but not anything else.
	 *
	 * @param legacyName Name of the curation
	 * @return Newly added curation.
	 */
	Curation addLegacyCuration(String legacyName);

	void addFastaSource(FastaSource source);

	void flush();
}
