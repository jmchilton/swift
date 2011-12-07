package edu.mayo.mprc.dbcurator.model.persistence;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.FastaSource;
import edu.mayo.mprc.dbcurator.model.HeaderTransform;
import edu.mayo.mprc.dbcurator.model.SourceDatabaseArchive;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that handles data access related to Curation instances.  Others should use CuratorPersistenceManager.  It should
 * be noted that all objects returned from this detached and are not tied to the persistent store.
 * <p/>
 * A singleton accessed by the get() method
 */
public final class CurationDaoImpl extends DaoBase implements CurationDao {
	private List<Curation> allCurationList = null;
	private Change legacyCurationChange = null;

	public CurationDaoImpl(DatabasePlaceholder databasePlaceholder) {
		super(databasePlaceholder);
	}

	/**
	 * gets a curation with the given ID.  This also loads all associated objects.
	 *
	 * @param curationID the id of the curation that you want (this is probably too low of level for the public to use...
	 * @return the curation with the given id or null of not curation with that id was found
	 */
	public Curation getCuration(final int curationID) {
		Session session = getSession();
		Curation curation = null;
		try {
			curation = (Curation) session.get(Curation.class, curationID);
		} catch (Exception t) {
			throw new MprcException("Could not obtain curation ", t);
		}
		return curation;
	}

	/**
	 * gets the curation by a short name
	 *
	 * @param shortName the name that we can look up by
	 * @return the curation with that short name
	 */
	public Curation getCurationByShortName(final String shortName) {
		Curation example = new Curation();
		example.setShortName(shortName);
		List<Curation> matches = this.getMatchingCurations(example, null, null);
		if (matches == null || matches.size() == 0) {
			return null;
		} else {
			return matches.get(0);
		}
	}

	/**
	 * try to get the unique name out since it might contain ${DBPath:uniquename} decorations. If it does not,
	 * name is returned verbatim.
	 *
	 * @param possibleUniquename
	 * @return
	 */
	static String extractShortName(String possibleUniquename) {
		Matcher uniquenameMatch = Pattern.compile("(?:\\$\\{)(?:DBPath:|DB:)([^}]*)(?:})").matcher(possibleUniquename);
		if (uniquenameMatch.matches()) {
			return uniquenameMatch.group(1); //extract the uniquename from given one.  This should always match I think...
		} else {
			return possibleUniquename;
		}
	}

	/**
	 * Try to get the short name (we could not obtain unique name). A short name occurs either in the ${DB:???_LATEST}
	 * format, or if we are directly given a FASTA file name, it is the part before the date part in 20090101A format.
	 * If both assumptions fail, the name is returned verbatim.
	 *
	 * @param name Name of the database.
	 * @return Extracted short name.
	 */
	static String extractShortname(String name) {
		Matcher latestMatch = Pattern.compile("(?:\\$\\{)(?:DBPath:|DB:)(.*)_LATEST(?:\\})").matcher(name);
		if (latestMatch.matches()) {
			return latestMatch.group(1); //we have a _LATEST that we can extract shortname from
		}

		Matcher uniquenameMatch = Pattern.compile("^(.*)\\d{8}\\D\\.(fasta|FASTA)$").matcher(name);
		if (uniquenameMatch.matches()) {
			return uniquenameMatch.group(1); //we have a uniquename we can extract a shortname from
		}

		//we presumably already have a shortname
		return name;
	}

	/**
	 * Takes a name that was supplied and tries to find a Curation for it.
	 * Will return the most recently run curation with the given shortname.
	 *
	 * @param name is the name either shortname, or a shortname with _LATEST appended.
	 * @return
	 */
	public Curation findCuration(String name) {
		name = name.trim();
		//try to see if we have a match based on short name
		Curation match = getCurationByShortName(extractShortName(name));
		if (match != null) {
			return match;
		}

		// if there wasn't a match based on unqiue name then we are apprently working on a shortname in which case
		// we will see if the request is for the latest and if so will give the latest of a particular shortname.
		// So we will try to see if there are any matches on the shortname and find the most recently run curation with that shortname.
		List<Curation> allMatches = getCurationsByShortname(extractShortname(name));

		if (allMatches.size() == 0) {
			match = null;
		} else if (allMatches.size() == 1) {
			match = allMatches.get(0);
			if (match.getCurationFile() == null) {
				match = null;
			}
		} else {
			//sort the list based on the run date of the curation descending
			Collections.sort(allMatches, new Comparator<Curation>() {
				public int compare(Curation o1, Curation o2) {
					if (o1.getRunDate() == null && o2.getRunDate() == null) {
						return 0;
					}
					if (o1.getRunDate() == null) {
						return 1;
					}
					if (o2.getRunDate() == null) {
						return -1;
					}
					return -o1.getRunDate().compareTo(o2.getRunDate());
				}
			});
			for (Curation curation : allMatches) {
				if (curation.getCurationFile() != null) {
					match = curation;
					break;
				}
			}
		}

		if (match == null) {
			throw new MprcException("Could not find a Curation for the given token: " + name);
		}
		return match;
	}

	public List<Curation> getCurationsByShortname(String shortname) {
		return getCurationsByShortname(shortname, false);
	}

	public List<Curation> getCurationsByShortname(final String shortname, final boolean ignoreCase) {
		List<Curation> returnList = new ArrayList<Curation>();
		List genericResults = null;
		try {
			Criteria criteria = allCriteria(Curation.class);
			if (ignoreCase) {
				criteria.add(Restrictions.eq("shortName", shortname).ignoreCase());
			} else {
				criteria.add(Restrictions.eq("shortName", shortname));
			}
			genericResults = criteria.list();
			if (genericResults != null) {
				for (Object o : genericResults) {
					returnList.add((Curation) o);
				}
			}
		} catch (Exception t) {
			throw new MprcException("Could not obtain list of curations by short name " + shortname + " " + (ignoreCase ? "ignoring case" : "not ignoring case"), t);
		}
		return returnList;
	}

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
	public List<Curation> getMatchingCurations(final Curation templateCuration, final Date earliestRunDate, final Date latestRunDate) {
		if (templateCuration == null && earliestRunDate == null && latestRunDate == null) {
			return allCurations();
		}
		return getMatchingCurationsFromDb(templateCuration, earliestRunDate, latestRunDate);
	}

	/**
	 * @return List of all curations from cache, or (if their number changed) from database.
	 */
	private synchronized List<Curation> allCurations() {
		if (this.allCurationList == null) {
			allCurationList = getMatchingCurationsFromDb(null, null, null);
			return allCurationList;
		} else {
			Long count = null;
			try {
				count = (Long) getSession().createQuery("select count(c) from Curation c where c.deletion is null").uniqueResult();
			} catch (Exception t) {
				throw new MprcException("Cannot obtain count of all curations", t);
			}

			if (count != allCurationList.size()) {
				allCurationList = getMatchingCurationsFromDb(null, null, null);
			}

			return allCurationList;
		}
	}

	private List<Curation> getMatchingCurationsFromDb(final Curation templateCuration, final Date earliestRunDate, final Date latestRunDate) {
		List<Curation> returnList = new ArrayList<Curation>();
		List genericResults = null;

		try {
			Criteria criteria = allCriteria(Curation.class);
			if (templateCuration != null) {
				criteria.add(Example.create(templateCuration));
			}
			if (earliestRunDate != null) {
				criteria.add(Expression.ge("runDate", earliestRunDate));
			}
			if (latestRunDate != null) {
				criteria.add(Expression.le("runDate", latestRunDate));
			}
			genericResults = criteria.list();
		} catch (Exception t) {
			throw new MprcException("Cannot get matching curations for database " +
					(templateCuration != null ? templateCuration.getShortName() : "") + " and dates between " + earliestRunDate + " and " + latestRunDate, t);
		}
		if (genericResults != null) {
			for (Object o : genericResults) {
				returnList.add((Curation) o);
			}
		}
		return returnList;
	}

	/**
	 * checks to see if there is a database from the given url
	 *
	 * @param url              the url that the file was downloaded from
	 * @param fileCreationDate the date the file was downloaded
	 * @return an archive that from that url on that date or null if none were found
	 */
	public SourceDatabaseArchive findSourceDatabaseInExistence(final String url, final Date fileCreationDate) {
		List<SourceDatabaseArchive> archiveList = null;

		Session session = getSession();
		try {
			Criteria criteria = session.createCriteria(SourceDatabaseArchive.class);
			criteria.add(Restrictions.eq("sourceURL", url));
			// serverDate has to match with one second precision - never test timestamp for equality
			criteria.add(Restrictions.ge("serverDate", new Date(fileCreationDate.getTime() - 1000)));
			criteria.add(Restrictions.lt("serverDate", new Date(fileCreationDate.getTime() + 1000)));
			archiveList = (List<SourceDatabaseArchive>) criteria.list();
		} catch (Exception t) {
			throw new MprcException("Cannot find source database for url: " + url + " and creation date " + fileCreationDate, t);
		}

		if (archiveList != null && archiveList.size() > 0) {
			for (Object o : archiveList) {
				SourceDatabaseArchive archive = (SourceDatabaseArchive) o;
				if (archive.getArchive() != null && archive.getArchive().exists()) {
					return archive;
				}
			}
		}
		return null;
	}

	public List<FastaSource> getCommonSources() {
		try {
			return getSession().createQuery("from FastaSource ds where ds.common = true").list();
		} catch (Exception t) {
			throw new MprcException("Cannot obtain a list of FASTA database sources", t);
		}
	}

	private Criterion getHeaderTransformEqualityCriteria(HeaderTransform transform) {
		return Restrictions.eq("name", transform.getName());
	}

	@Override
	public void addHeaderTransform(HeaderTransform sprotTrans) {
		this.save(sprotTrans, getHeaderTransformEqualityCriteria(sprotTrans), true);
	}

	public List<HeaderTransform> getCommonHeaderTransforms() {
		try {
			return getSession().createQuery("from HeaderTransform ht where ht.common = true").list();
		} catch (Exception t) {
			throw new MprcException("Cannot obtain a list of common database header transformations", t);
		}
	}

	@Override
	public synchronized Curation addLegacyCuration(String legacyName) {
		if (legacyCurationChange == null) {
			legacyCurationChange = new Change("Adding legacy databases", new Date());
		}

		Curation curation = new Curation();
		curation.setShortName(legacyName);
		curation.setNotes("Legacy database");
		curation.setTitle("Legacy database " + legacyName);
		curation.setOwnerEmail("mprctest@mayo.edu");
		curation.setCurationFile(new File(legacyName + ".fasta"));
		curation = save(curation, legacyCurationChange, getCurationEqualityCriteria(curation), false);
		if (allCurationList != null) {
			allCurationList.add(curation);
		}
		return curation;
	}

	private Criterion getFastaSourceEqualityCriteria(FastaSource source) {
		return Restrictions.eq("name", source.getName());
	}

	@Override
	public void addFastaSource(FastaSource source) {
		save(source, getFastaSourceEqualityCriteria(source), true);
	}

	@Override
	public void flush() {
		getSession().flush();
	}

	public void addCuration(final Curation toSave) {
		try {
			save(toSave, new Change("Adding database " + toSave.getShortName(), new Date()), getCurationEqualityCriteria(toSave), true);
		} catch (Exception t) {
			throw new MprcException("Could not save an object to hibernate.", t);
		}
	}

	@Override
	public void save(SourceDatabaseArchive archive) {
		try {
			getSession().saveOrUpdate(archive);
		} catch (Exception t) {
			throw new MprcException("Could not save source database archive", t);
		}
	}

	@Override
	public void deleteCuration(Curation curation, Change change) {
		delete(curation, change);
	}

	private Criterion getCurationEqualityCriteria(Curation curation) {
		return Restrictions.eq("shortName", curation.getShortName());
	}

	public void delete(Object o) {
		try {
			getSession().delete(o);
		} catch (Exception t) {
			throw new MprcException("Could not delete object from database.", t);
		}
	}

	public FastaSource getDataSourceByName(String name) {
		List<FastaSource> matches = null;
		try {
			matches = getSession()
					.createQuery("from FastaSource ds where ds.name = :name")
					.setParameter("name", name)
					.list();
		} catch (Exception t) {
			throw new MprcException("Cannot get data sources by name " + name, t);
		}

		return matches == null || matches.isEmpty() ? null : matches.get(0);
	}

	public FastaSource getDataSourceByUrl(String url) {
		List<FastaSource> matches = null;
		try {
			matches = getSession()
					.createQuery("from FastaSource ds where ds.url = :url")
					.setParameter("url", url)
					.list();
		} catch (Exception t) {
			throw new MprcException("Cannot get data sources by url " + url, t);
		}

		return matches == null || matches.isEmpty() ? null : matches.get(0);
	}

	public HeaderTransform getHeaderTransformByName(String name) {
		List<HeaderTransform> matches = null;
		try {
			matches = getSession()
					.createQuery("from HeaderTransform ht where ht.name = :name")
					.setParameter("name", name)
					.list();
		} catch (Exception t) {
			throw new MprcException("Cannot get database header transformation named [" + name + "]", t);
		}
		return (matches == null || matches.isEmpty() ? null : matches.get(0));
	}

	public HeaderTransform getHeaderTransformByUrl(String forUrl) {
		List<HeaderTransform> matches = null;
		try {
			matches = getSession()
					.createQuery("select ds.transform from FastaSource ds where ds.url = :forUrl")
					.setParameter("forUrl", forUrl)
					.list();
		} catch (Exception t) {
			throw new MprcException("Cannot get database header transformation by url " + forUrl, t);
		}
		return (matches == null || matches.isEmpty() ? null : matches.get(0));
	}
}
