package edu.mayo.mprc.dbcurator.server;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.common.base.Supplier;

import edu.mayo.mprc.GWTServiceExceptionFactory;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.common.client.GWTServiceException;
import edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStub;
import edu.mayo.mprc.dbcurator.client.curatorstubs.HeaderTransformStub;
import edu.mayo.mprc.dbcurator.client.services.CommonDataRequester;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.FastaSource;
import edu.mayo.mprc.dbcurator.model.HeaderTransform;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.utilities.FileUtilities;

public class CommonDataRequesterDelegateImpl implements CommonDataRequester {
	private static final Logger LOGGER = Logger.getLogger(CommonDataRequesterDelegateImpl.class);

	private CurationDao curationDao;
	
	private Supplier<HttpSession> sessionSupplier;
	
	public CommonDataRequesterDelegateImpl(CurationDao curationDao, Supplier<HttpSession> sessionSupplier) {
		this.curationDao = curationDao;
		this.sessionSupplier = sessionSupplier;
	}
	
	public List<HeaderTransformStub> getHeaderTransformers() {
		List<HeaderTransform> transforms;
		if (curationDao == null) {
			return new ArrayList<HeaderTransformStub>(0);
		}

		List<HeaderTransformStub> abstractList = null;
		curationDao.begin();
		try {
			transforms = curationDao.getCommonHeaderTransforms();

			abstractList = new ArrayList<HeaderTransformStub>();
			if (transforms != null) {
				for (HeaderTransform transform : transforms) {
					HeaderTransformStub stub = new HeaderTransformStub();
					stub.description = transform.getName();
					stub.matchPattern = transform.getGroupString();
					stub.subPattern = transform.getSubstitutionPattern();
					abstractList.add(stub);
				}
			}
			curationDao.commit();
		} catch (Exception t) {
			curationDao.rollback();
			throw new MprcException("Cannot get header transformer list", t);
		}
		return abstractList;
	}

	public Map<String, String> getFTPDataSources() {
		if (curationDao == null) {
			return new HashMap<String, String>();
		}

		Map<String, String> sourceMap = null;
		curationDao.begin();
		try {
			List<FastaSource> sources = curationDao.getCommonSources();

			sourceMap = new HashMap<String, String>();

			for (FastaSource source : sources) {
				sourceMap.put(source.getName(), source.getUrl());
			}
			curationDao.commit();
		} catch (Exception t) {
			curationDao.rollback();
			throw new MprcException("Could not obtain FTP data sources", t);
		}

		return sourceMap;
	}

	public Boolean isShortnameUnique(String toCheck) {
		curationDao.begin();
		try {
			Boolean result = isShortnameUniqueBody(toCheck);
			curationDao.commit();
			return result;
		} catch (Exception t) {
			curationDao.rollback();
			throw new MprcException("Could not determine if short curation name is unique", t);
		}
	}

	private Boolean isShortnameUniqueBody(String toCheck) {
		return (0 == curationDao.getCurationsByShortname(toCheck, /*ignoreCase*/true).size());
	}

	/**
	 * Takes a CurationStub that we want to have updated with status from the server and performs that update returning
	 * the updated curation.  It is up to the client to use the returned stub and swap the old stub with the new stub
	 * and then perform any updating that may be required.
	 *
	 * @param toUpdate the stub that you want to have updated
	 * @return the updated stub
	 */
	public CurationStub performUpdate(CurationStub toUpdate) {
		curationDao.begin();
		try {
			CurationHandlerI handler = getHandler();
			final CurationStub curationStub = handler.syncCuration(toUpdate);
			curationDao.commit();
			return curationStub;
		} catch (Exception t) {
			curationDao.rollback();
			throw new MprcException(t);
		}
	}

	public CurationStub lookForCuration() {
		curationDao.begin();
		try {
			final CurationStub cachedCurationStub = getHandler().getCachedCurationStub();
			curationDao.commit();
			return cachedCurationStub;
		} catch (Exception t) {
			curationDao.rollback();
			throw new MprcException(t);
		}
	}

	public CurationStub getCurationByID(Integer id) {
		curationDao.begin();
		try {
			final CurationStub curationStub = getHandler().getCurationByID(id);
			curationDao.commit();
			return curationStub;
		} catch (Exception t) {
			curationDao.rollback();
			throw new MprcException(t);
		}
	}


	public CurationStub copyCurationStub(CurationStub toCopy) {
		curationDao.begin();
		try {
			getHandler().syncCuration(toCopy);
			Curation curation = getHandler().getCachedCuration();
			Curation retCuration = curation.createCopy(false);
			final CurationStub stub = getHandler().createStub(retCuration);
			curationDao.commit();
			return stub;
		} catch (Exception t) {
			curationDao.rollback();
			throw new MprcException(t);
		}
	}

	private CurationHandlerI getHandler() {
		return CurationHandler.getInstance(sessionSupplier.get());
	}

	/**
	 * Takes a CurationStub and returns a list of curations stubs that at least match the stub that was passed in.  So the properties
	 * of the returned CurationStubs will be equal to the set properties in the passed in stub.
	 *
	 * @param toMatch the CurationStub that you want to find matches for
	 * @return a list of matching CurationStubs
	 */
	public List<CurationStub> getMatches(CurationStub toMatch) {
		curationDao.begin();
		try {
			final List<CurationStub> list = this.getMatches(toMatch, null, null);
			curationDao.commit();
			return list;
		} catch (Exception t) {
			curationDao.rollback();
			throw new MprcException(t);
		}
	}

	/**
	 * Does the same as the getMatches(CurationStub) but also allows bracketing of the run date property so only run dates
	 * between certain dates will be returned.
	 *
	 * @param toMatch     the curationstub you want to match
	 * @param earliestRun the earliest run date you want returned
	 * @param latestRun   the latest run date you want returned
	 * @return a list of CurationStubs that meet the criteria
	 */
	public List<CurationStub> getMatches(CurationStub toMatch, Date earliestRun, Date latestRun) {
		curationDao.begin();
		try {
			CurationHandlerI handler = getHandler();
			final List<CurationStub> curations = handler.getMatchingCurations(toMatch, earliestRun, latestRun);
			curationDao.commit();
			return curations;
		} catch (Exception t) {
			curationDao.rollback();
			throw new MprcException(t);
		}
	}

	/**
	 * Runs a curation on the server.  This will execute a curation.  If you want to get the status of a curation call getStatus()
	 *
	 * @param toRun the curation you want to have run
	 */
	public CurationStub runCuration(CurationStub toRun) {
		curationDao.begin();
		try {
			if (!isShortnameUniqueBody(toRun.getShortName())) {
				if (toRun.getErrorMessages() == null) {
					toRun.setErrorMessages(new ArrayList<String>());
				}
				toRun.getErrorMessages().add("The shortname is not unique");
				return toRun;
			}

			CurationHandlerI handler = getHandler();
			final CurationStub curationStub = handler.executeCuration(toRun);
			curationDao.commit();
			return curationStub;
		} catch (Exception t) {
			curationDao.rollback();
			throw new MprcException(t);
		}
	}

	public String testPattern(String pattern) {
		try {
			Pattern.compile(pattern);
			return "";
		} catch (PatternSyntaxException e) {
			return e.getMessage();
		}
	}

	public String[] getLines(String sharedPath, int startLineInclusive, int numberOfLines, String pattern) throws GWTServiceException {
		RandomAccessFile raf = null;
		try {
			this.setCancelMessage(false);
			this.clearResults();

			Pattern compiledPattern = null;
			if (pattern != null && pattern.length() > 0) {
				pattern = ".*" + pattern + ".*";
				pattern = pattern.replace("\\", "\\\\");
				compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			}

			SortedMap<Integer, Long> currentPositionMap = getCurrentPositionMap(sharedPath, pattern);

			raf = new RandomAccessFile(new File(sharedPath), "r");
			Long startLinePosition = currentPositionMap.get(startLineInclusive);
			if (startLinePosition == null) {
				//todo: find the position of this line
				SortedMap<Integer, Long> headMap = currentPositionMap.headMap(startLineInclusive);

				Integer closestLessThanLine = 0;
				if (headMap.size() == 0) {
					startLinePosition = 0L;
				} else {
					closestLessThanLine = Collections.max(headMap.keySet());
				}


				if (closestLessThanLine == null || closestLessThanLine == 0) {
					startLinePosition = 0L;
					closestLessThanLine = 0;
				} else {
					startLinePosition = headMap.get(closestLessThanLine);
				}

				raf.seek(startLinePosition);

				int matchCounter = closestLessThanLine;
				while (closestLessThanLine < startLineInclusive) {
					long position = raf.getFilePointer();
					String line = raf.readLine();
					if (line == null) {
						break;
					}
					if (compiledPattern == null || compiledPattern.matcher(line).matches()) {
						currentPositionMap.put(matchCounter++, position);
						closestLessThanLine++;
					}
				}
				//now we are at the startLine
			} else {
				raf.seek(startLinePosition);
			}

			//todo: probably just want to go through until we fill up the array taking the pattern into account.

			int resultCounter = 0;
			int fromStart = 0;
			while (resultCounter < numberOfLines && !getCancelMessage()) {
				Long cachedPosition = currentPositionMap.get(startLineInclusive + fromStart);
				fromStart++;
				if (cachedPosition != null) {
					raf.seek(cachedPosition);
					addResult(raf.readLine());
					resultCounter++;
				} else {
					long position = raf.getFilePointer();
					String line = raf.readLine();
					if (line == null) { //if we have reached the end of the file then just don't get any more.
						break;
					}
					if (compiledPattern == null || compiledPattern.matcher(line).matches()) {
						currentPositionMap.put(startLineInclusive + resultCounter, position);
						addResult(line);
						resultCounter++;
					}
				}
			}
			return getResults();
		} catch (Exception e) {
			LOGGER.error("Could not obtain lines", e);
			throw GWTServiceExceptionFactory.createException("Could not obtain lines", e);
		} finally {
			FileUtilities.closeQuietly(raf);
		}

	}

	protected synchronized void clearResults() {
		this.sessionSupplier.get().setAttribute("results", null);
	}

	protected synchronized void addResult(String result) {
		List<String> results;
		Object o = this.sessionSupplier.get().getAttribute("results");
		if (o == null) {
			results = new ArrayList<String>();
			this.sessionSupplier.get().setAttribute("results", results);
		} else {
			results = (List<String>) o;
		}

		results.add(result);
	}

	public synchronized String[] getResults() throws GWTServiceException {
		try {
			List<String> results;
			Object o = this.sessionSupplier.get().getAttribute("results");
			if (o == null) {
				results = new ArrayList<String>();
				this.sessionSupplier.get().setAttribute("results", results);
			} else {
				results = (List<String>) o;
			}

			String[] retArray = new String[results.size()];
			for (int i = 0; i < results.size(); i++) {
				retArray[i] = results.get(i);
			}

			return retArray;
		} catch (Exception t) {
			LOGGER.error("Could not obtain results", t);
			throw GWTServiceExceptionFactory.createException("Could not obtain results", t);
		}
	}

	public void setCancelMessage(boolean cancelMessage) throws GWTServiceException {
		try {
			this.sessionSupplier.get().setAttribute("cancelRequest", (cancelMessage ? true : null));
		} catch (Exception t) {
			LOGGER.error("Could not cancel request", t);
			throw GWTServiceExceptionFactory.createException("Could not cancel request", t);
		}
	}

	protected boolean getCancelMessage() {
		return !(this.sessionSupplier.get().getAttribute("cancelRequest") == null);
	}

	private SortedMap<Integer, Long> getCurrentPositionMap(String filePath, String pattern) {
		Map<String, SortedMap<Integer, Long>> allPositionMaps = getPositionMaps();
		String currentKey = (pattern == null || pattern.length() == 0 ? filePath : filePath + "_" + pattern);

		SortedMap<Integer, Long> currentPositionLookup = null;

		//get rid of the maps we are no longer interested in
		for (Map.Entry<String, SortedMap<Integer, Long>> entry : allPositionMaps.entrySet()) {
			if (!entry.getKey().equals(currentKey)) {
				allPositionMaps.remove(entry.getKey());
			} else {
				currentPositionLookup = entry.getValue();
			}
		}

		if (currentPositionLookup == null) {
			currentPositionLookup = new TreeMap<Integer, Long>();
			allPositionMaps.put(currentKey, currentPositionLookup);
		}
		return currentPositionLookup;
	}

	private Map<String, SortedMap<Integer, Long>> getPositionMaps() {
		final Object attribute = this.sessionSupplier.get().getAttribute("FileLineMap");
		Map<String, SortedMap<Integer, Long>> positionMap = (Map<String, SortedMap<Integer, Long>>) attribute;
		if (positionMap == null) {
			positionMap = new HashMap<String, SortedMap<Integer, Long>>();
			this.sessionSupplier.get().setAttribute("FileLineMap", positionMap);
		}
		return positionMap;
	}

}
