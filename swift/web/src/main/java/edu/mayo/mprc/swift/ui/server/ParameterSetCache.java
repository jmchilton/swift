package edu.mayo.mprc.swift.ui.server;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.swift.params2.ParamsDao;
import edu.mayo.mprc.swift.params2.SavedSearchEngineParameters;
import edu.mayo.mprc.swift.params2.SearchEngineParameters;
import edu.mayo.mprc.swift.ui.client.rpc.ClientParamSet;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A cache of parameter sets that are being used by the current user.
 * The cache has two parts - one for persistent parameter sets, the other for temporary parameters.
 * Both parts are stored in HttpSession.
 */
public final class ParameterSetCache {

	// Client token id -> persistent parameter set
	private static final String PERSISTENT_PARAM_SETS = "persistentParamSets";
	// Client token id -> temporary parameter set
	private static final String TEMPORARY_PARAM_SETS = "temporaryParamSets";
	// A list of client tokens
	private static final String TEMPORARY_CLIENT_PARAM_LIST = "temporaryClientParamList";

	private Map<Integer, SearchEngineParameters> persistentCache;
	private Map<Integer, SearchEngineParameters> temporaryCache;

	// Matches parameter sets that are copied
	private static final Pattern COPIED_PARAM_SET = Pattern.compile("^Copy (\\d+ )?of ");

	private final HttpSession session;
	private final ParamsDao paramsDao;

	public ParameterSetCache(HttpSession session, ParamsDao paramsDao) {
		this.session = session;
		this.paramsDao = paramsDao;
	}

	private synchronized void addToCache(ClientParamSet clientParamSet, SearchEngineParameters serverParamSet) {
		Map<Integer, SearchEngineParameters> cache = getCache(clientParamSet.getId());
		if (serverParamSet.getId() != null) {
			throw new MprcException("Cache can only store objects detached from Hibernate");
		}
		cache.put(clientParamSet.getId(), serverParamSet);
		if (clientParamSet.isTemporary()) {
			getTemporaryClientParamList().add(clientParamSet);
		}
	}

	public synchronized void removeFromCache(ClientParamSet clientParamSet) {
		getCache(clientParamSet.getId()).remove(clientParamSet.getId());
		if (clientParamSet.isTemporary()) {
			getTemporaryClientParamList().remove(clientParamSet);
		}
	}

	/**
	 * Return a cached set of search engine parameters. Since the parameters are immutable, the set is not attached to the session
	 *
	 * @param paramSet
	 * @return
	 */
	public synchronized SearchEngineParameters getFromCache(ClientParamSet paramSet) {
		SearchEngineParameters ps = null;
		Map<Integer, SearchEngineParameters> cache = getCache(paramSet.getId());

		Integer key = paramSet.getId();

		if (!cache.containsKey(key)) {

			if (key < 0) {
				throw new MprcException("Can't find temporary search definition " + key);
			}
			SavedSearchEngineParameters saved = paramsDao.getSavedSearchEngineParameters(key);
			if (saved == null) {
				throw new MprcException("Can't load Swift search parameters " + paramSet.getName()
						+ " (" + key + ") from database");
			}

			// We copy the parameters so they are no longer connected to the session.
			ps = saved.getParameters().copy();
			addToCache(paramSet, ps);
		} else {
			ps = cache.get(key);
		}
		return ps;
	}

	/**
	 * Get parameter set from cache. When the parameter set corresponds to a saved parameter set,
	 * load it from database instead of relying on cached value.
	 */
	public SearchEngineParameters getFromCacheHibernate(ClientParamSet paramSet) {
		Integer key = paramSet.getId();

		if (paramSet.isTemporary()) {
			Map<Integer, SearchEngineParameters> cache = getCache(paramSet.getId());

			if (!cache.containsKey(key)) {
				throw new MprcException("Can't find temporary search definition " + key);
			} else {
				return cache.get(key);
			}
		} else {
			SavedSearchEngineParameters saved = paramsDao.getSavedSearchEngineParameters(key);
			if (saved == null) {
				throw new MprcException("Can't load Swift search parameters " + paramSet.getName()
						+ " (" + key + ") from database");
			}
			return saved.getParameters();
		}
	}


	private Map<Integer, SearchEngineParameters> getCache(Integer id) {
		if (id < 0) {
			if (temporaryCache == null) {
				temporaryCache = (Map<Integer, SearchEngineParameters>) session.getAttribute(TEMPORARY_PARAM_SETS);
				if (temporaryCache == null) {
					temporaryCache = new HashMap<Integer, SearchEngineParameters>();
				}
				session.setAttribute(TEMPORARY_PARAM_SETS, temporaryCache);
			}
			return temporaryCache;
		} else {
			if (persistentCache == null) {
				persistentCache = (Map<Integer, SearchEngineParameters>) session.getAttribute(PERSISTENT_PARAM_SETS);
				if (persistentCache == null) {
					persistentCache = new HashMap<Integer, SearchEngineParameters>();
				}
				session.setAttribute(PERSISTENT_PARAM_SETS, persistentCache);
			}
			return persistentCache;
		}
	}

	/**
	 * Temporary parameter set map maps search engine parameters to a client 'token' - a simple reference to the parameter set.
	 * The map is defined on the session. If no map is present a new, empty one is created.
	 */
	public List<ClientParamSet> getTemporaryClientParamList() {
		List<ClientParamSet> clientParamList = (List<ClientParamSet>) session.getAttribute(TEMPORARY_CLIENT_PARAM_LIST);
		if (clientParamList == null) {
			clientParamList = new ArrayList<ClientParamSet>();
			session.setAttribute(TEMPORARY_CLIENT_PARAM_LIST, clientParamList);
		}
		return clientParamList;
	}

	/**
	 * Make a new temporary parameter set based on the already existing one
	 */
	public synchronized ClientParamSet installTemporary(ClientParamSet paramSet) {
		final String paramSetName = paramSet.getName();
		final String paramSetOwnerEmail = paramSet.getOwnerEmail();
		final String paramSetOwnerInitials = paramSet.getInitials();

		SearchEngineParameters orig = getFromCache(paramSet);
		if (orig == null) {
			throw new MprcException("Can't load paramset " + paramSet.getId() + " for cloning to temp");
		}
		SearchEngineParameters serverParamSet = orig.copy();

		return installTemporary(paramSetName, paramSetOwnerEmail, paramSetOwnerInitials, serverParamSet);
	}

	/**
	 * Make a new temporary parameter set from scratch.
	 */
	public ClientParamSet installTemporary(String originalName, String ownerEmail, String ownerInitials, SearchEngineParameters serverParamSet) {
		if (serverParamSet.getId() != null) {
			throw new MprcException("The temporary parameter set must not participate in hibernate");
		}
		final List<ClientParamSet> temporaryClientParamSets = getTemporaryClientParamList();

		String name = getUniqueTemporaryName(originalName, temporaryClientParamSets);

		// Find the minimum temporary id. Set our new id as one less the minimum (--> uniqueness)
		int minId = 0;
		for (ClientParamSet cps : temporaryClientParamSets) {
			if (cps.getId() < minId) {
				minId = cps.getId();
			}
		}

		ClientParamSet clientParamSet = new ClientParamSet(minId - 1, name, ownerEmail, ownerInitials);

		addToCache(clientParamSet, serverParamSet);

		return clientParamSet;
	}

	private String getUniqueTemporaryName(String paramSetName, List<ClientParamSet> temporaryClientParamSets) {
		// Original name is a name without the "Copy # of " before it
		String origName = COPIED_PARAM_SET.matcher(paramSetName).replaceAll("");

		// The name must be different than all saved names
		HashSet<String> persNames;
		final List<SavedSearchEngineParameters> engineParametersList = paramsDao.savedSearchEngineParameters();
		persNames = new HashSet<String>(engineParametersList.size());
		for (SavedSearchEngineParameters saved : engineParametersList) {
			persNames.add(saved.getName());
		}

		// The name must be different from all current temp names
		HashSet<String> tempNames = new HashSet<String>();
		for (ClientParamSet cp : temporaryClientParamSets) {
			tempNames.add(cp.getName());
		}

		// Start with copy 1
		int n = 1;

		// try to ensure a name that doesn't conflict with any other SearchEngineParameters living or dead.
		String name = origName;
		while (persNames.contains(name) || tempNames.contains(name)) {
			name = "Copy " + (n == 1 ? "" : n + " ") + "of " + origName;
			n++;
		}
		return name;
	}

	public ClientParamSet findMatchingTemporaryParamSet(SearchEngineParameters parameters) {
		final List<ClientParamSet> temporaryClientParamList = getTemporaryClientParamList();
		for (ClientParamSet set : temporaryClientParamList) {
			final SearchEngineParameters cache = getFromCache(set);
			if (parameters.equals(cache)) {
				return set;
			}
		}
		return null;
	}
}
