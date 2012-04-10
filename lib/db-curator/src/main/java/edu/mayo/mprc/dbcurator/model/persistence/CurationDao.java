package edu.mayo.mprc.dbcurator.model.persistence;

import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.Dao;
import edu.mayo.mprc.database.Evolvable;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.FastaSource;
import edu.mayo.mprc.dbcurator.model.HeaderTransform;
import edu.mayo.mprc.dbcurator.model.SourceDatabaseArchive;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

public interface CurationDao extends Dao {
	/**
	 * gets a curation with the given ID.  This also loads all associated objects.
	 *
	 * @param curationID the id of the curation that you want (this is probably too low of level for the public to use...
	 * @return the curation with the given id or null of not curation with that id was found
	 */
	Curation getCuration(final int curationID);

	void addCuration(Curation curation);

	void save(SourceDatabaseArchive archive);

	void deleteCuration(Curation curation, Change change);

	FastaSource getDataSourceByName(String urlString);

	FastaSource getDataSourceByUrl(String urlString);

	/**
	 * checks to see if there is a database from the given url
	 *
	 * @param s          the url that the file was downloaded from
	 * @param urlLastMod the date the file was downloaded
	 * @return an archive that from that url on that date or null if none were found
	 */
	SourceDatabaseArchive findSourceDatabaseInExistence(String s, DateTime urlLastMod);

	HeaderTransform getHeaderTransformByUrl(String url);

	HeaderTransform getHeaderTransformByName(String name);

	/**
	 * Gets a list of Curations that seems to match the given templateCuration.  If you passed a curation.  You can use this
	 * as a query by example.
	 * If you want all curations you can just pass in all null values.
	 * <p/>
	 * There is an optimization for queries returing all curations. If that is the case, a count(Curation) query is ran
	 * against the database to figure out whether the amount of curations changed. If not, cached value is used.
	 *
	 * @param templateCuration the curation that has the properties that you want to match in the set of returned curations
	 * @param earliestRunDate  the earliest run date you want returned or null if
	 * @param latestRunDate    latest run date you want returned
	 * @return the set of curations that have the same parameters as the curation passed in.
	 */
	List<Curation> getMatchingCurations(Curation templateCuration, Date earliestRunDate, Date latestRunDate);

	/**
	 * Takes a name that was supplied and tries to find a Curation for it.
	 * Will return the most recently run curation with the given shortname.
	 *
	 * @param shortDbName is the name either shortname, or a shortname with _LATEST appended.
	 * @return Matching curation.
	 */
	Curation findCuration(String shortDbName);

	/**
	 * gets the curation by a short name
	 *
	 *
	 * @param uniqueName the name that we can look up by
	 * @return the curation with that short name
	 */
	Curation getCurationByShortName(final String uniqueName);

	/**
	 * Just like {@link #getCurationByShortName(String)}, but if a curation cannot be found,
	 * it looks through the list of the deleted curations of matching name, finds one that was deleted
	 * the latest and returns that information. This is used for matching legacy data.
	 * @param uniqueName Name of the curation.
	 * @return the curation with the short name, including potentially deleted ones. If none found, returns null.
	 */
	Curation getLegacyCuration(final String uniqueName);

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
