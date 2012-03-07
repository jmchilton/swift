package edu.mayo.mprc.swift.ui.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import edu.mayo.mprc.GWTServiceExceptionFactory;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.ServletIntialization;
import edu.mayo.mprc.common.client.GWTServiceException;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.dbundeploy.DatabaseUndeployerCaller;
import edu.mayo.mprc.dbundeploy.DatabaseUndeployerProgress;
import edu.mayo.mprc.msmseval.MSMSEvalParamFile;
import edu.mayo.mprc.swift.SwiftWebContext;
import edu.mayo.mprc.swift.db.SearchEngine;
import edu.mayo.mprc.swift.db.SwiftDao;
import edu.mayo.mprc.swift.dbmapping.SearchRun;
import edu.mayo.mprc.swift.dbmapping.SwiftSearchDefinition;
import edu.mayo.mprc.swift.params2.ParamName;
import edu.mayo.mprc.swift.params2.ParamsDao;
import edu.mayo.mprc.swift.params2.SavedSearchEngineParameters;
import edu.mayo.mprc.swift.params2.SearchEngineParameters;
import edu.mayo.mprc.swift.params2.mapping.ParamsInfo;
import edu.mayo.mprc.swift.params2.mapping.ParamsValidations;
import edu.mayo.mprc.swift.search.SwiftSearcherCaller;
import edu.mayo.mprc.swift.ui.client.Service;
import edu.mayo.mprc.swift.ui.client.rpc.*;
import edu.mayo.mprc.swift.ui.client.rpc.files.*;
import edu.mayo.mprc.utilities.NotHiddenFilter;
import edu.mayo.mprc.utilities.exceptions.ExceptionUtilities;
import edu.mayo.mprc.workspace.User;
import edu.mayo.mprc.workspace.WorkspaceDao;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lists files and folders for given path (relative to specified root).
 * The result is returned as a tree of objects.
 * <p/>
 * Supports the concept of "expanded paths" - those paths are recursed and returned fully,
 * otherwise the result of a single call lists just contents of a single directory without recursion.
 */
public final class ServiceImpl extends RemoteServiceServlet implements Service {
	private static final long serialVersionUID = 20071220L;
	private static final Logger LOGGER = Logger.getLogger(ServiceImpl.class);

	// String of allowed extensions, separated by | signs. The extensions are case insensitive.
	private static final String ALLOWED_EXTENSIONS = ".RAW|.raw|.mgf";
	public static final InputFileFilter FILTER = new InputFileFilter(ALLOWED_EXTENSIONS, false);
	private static final InputFileFilter FILTER_DIRS = new InputFileFilter(ALLOWED_EXTENSIONS, true);
	// After 10 seconds without hearing from the other side the search attempt timeouts
	private static final int SEARCH_TIMEOUT = 10 * 1000;

	private static final ClientUser[] EMPTY_USER_LIST = new ClientUser[0];
	private static final Pattern BAD_TITLE_CHARACTER = Pattern.compile("[^a-zA-Z0-9-+._()[\\\\]{}=# ]");

	public ServiceImpl() {
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletIntialization.initServletConfiguration(config);
	}


	public Entry listFiles(String relativePath, String[] expandedPaths) throws GWTServiceException {
		try {
			Entry rootEntry = new DirectoryEntry("(root)");
			File[] expandedFiles;
			if (expandedPaths == null) {
				expandedFiles = EMPTY_EXPANDED_FILES;
			} else {
				expandedFiles = new File[expandedPaths.length];
				for (int i = 0; i < expandedPaths.length; i++) {
					expandedFiles[i] = new File(getBrowseRoot(), expandedPaths[i]);
				}
			}
			listDirectoryContents(rootEntry, new File(getBrowseRoot(), relativePath), expandedFiles);
			return rootEntry;
		} catch (Exception t) {
			LOGGER.error("Could not list files", t);
			throw GWTServiceExceptionFactory.createException("Could not list files", t);
		}
	}

	public ClientUser[] listUsers() throws GWTServiceException {
		try {
			getWorkspaceDao().begin();
			List<User> users = getWorkspaceDao().getUsers();

			ClientUser[] result;
			if (users != null) {
				result = new ClientUser[users.size()];
				for (int i = 0; i < users.size(); i++) {
					User user = users.get(i);
					result[i] = getClientProxyGenerator().convertTo(user);
				}
			} else {
				result = EMPTY_USER_LIST;
			}
			getWorkspaceDao().commit();
			return result;
		} catch (Exception t) {
			getWorkspaceDao().rollback();
			LOGGER.error("Could not list users", t);
			throw GWTServiceExceptionFactory.createException("Could not list users", t);
		}
	}

	public void startSearch(Service.Token t, ClientSwiftSearchDefinition def) throws GWTServiceException {
		try {
			HttpSession session = compareToken(t);
			getSwiftDao().begin();
			ParameterSetCache cache = new ParameterSetCache(session, getParamsDao());

			SearchEngineParameters parameters = cache.getFromCacheHibernate(def.getParamSet());
			if (parameters.getId() == null) {
				parameters = getParamsDao().addSearchEngineParameters(parameters);
			}

			SwiftSearchDefinition swiftSearch = getClientProxyGenerator().convertFrom(def, parameters);
			final Matcher badTitleMatcher = BAD_TITLE_CHARACTER.matcher(swiftSearch.getTitle());
			if (swiftSearch.getTitle().length() == 0) {
				throw new MprcException("Cannot run Swift search with an empty title");
			}
			if (badTitleMatcher.find()) {
				throw new MprcException("Search title must not contain '" + badTitleMatcher.group() + "'");
			}
			boolean rerunPreviousSearch = false;
			SearchRun previousSearchRun;
			if (def.getPreviousSearchRunId() > 0) {
				// We already ran the search before.
				previousSearchRun = getSwiftDao().getSearchRunForId(def.getPreviousSearchRunId());
				if (previousSearchRun.getTitle().equals(swiftSearch.getTitle())) {
					// The titles match, output folders match, but since this is a reload, it is okay
					rerunPreviousSearch = true;
					final Integer searchId = previousSearchRun.getSwiftSearch();
					final SwiftSearchDefinition searchDefinition = getSwiftDao().getSwiftSearchDefinition(searchId);
					if (searchDefinition == null || !searchDefinition.getOutputFolder().equals(swiftSearch.getOutputFolder())) {
						previousSearchRun = null;
						// Since the folders do not match, we will rerun, but will not hide the previous search run
						// from the list
					}
				} else {
					previousSearchRun = null;
				}
			} else {
				previousSearchRun = null;
			}
			if (!rerunPreviousSearch && getSwiftDao().isExistingTitle(swiftSearch.getTitle(), swiftSearch.getUser())) {
				throw new MprcException("There is already a search titled " + swiftSearch.getTitle() + ".");
			}
			swiftSearch = getSwiftDao().addSwiftSearchDefinition(swiftSearch);
			int searchId = swiftSearch.getId();
			String batchName = swiftSearch.getTitle();
			final int previousSearchRunId = previousSearchRun == null ? 0 : previousSearchRun.getId();
			getSwiftDao().commit(); // We must commit here before we send the search over (it is loaded from the database)
			SwiftSearcherCaller.SearchProgressListener listener = SwiftSearcherCaller.startSearch(searchId, batchName, def.isFromScratch(), previousSearchRunId, getSwiftSearcherDaemonConnection());
			listener.waitForSearchReady(SEARCH_TIMEOUT);
			if (listener.getException() != null) {
				throw listener.getException();
			}

			if (listener.getSearchRunId() == 0) {
				throw new MprcException("The search was not started within timeout.");
			}
		} catch (Exception e) {
			getSwiftDao().rollback();
			LOGGER.error("Search could not be started", e);
			throw GWTServiceExceptionFactory.createException("Search could not be started", e);
		}
	}

	public ClientLoadedSearch loadSearch(Service.Token t, int searchRunId) throws GWTServiceException {
		try {
			HttpSession session = compareToken(t);
			getSwiftDao().begin();
			ParameterSetCache cache = new ParameterSetCache(session, getParamsDao());
			final SearchRun searchRun = getSwiftDao().getSearchRunForId(searchRunId);
			final SwiftSearchDefinition original = getSwiftDao().getSwiftSearchDefinition(searchRun.getSwiftSearch());
			final Resolver resolver = new Resolver(cache);
			ClientSwiftSearchDefinition proxy = getClientProxyGenerator().convertTo(original, resolver);
			final ClientLoadedSearch result = new ClientLoadedSearch(proxy, resolver.isClientParamSetListChanged() ? makeParamSetList(cache) : null);
			getSwiftDao().commit();
			return result;
		} catch (Exception e) {
			getSwiftDao().rollback();
			LOGGER.error("Could not load existing search for id: " + searchRunId, e);
			throw GWTServiceExceptionFactory.createException("Search could not be loaded for id: " + searchRunId, e);
		}
	}

	public List<ClientSearchEngine> listSearchEngines() throws GWTServiceException {
		List<ClientSearchEngine> infos = new ArrayList<ClientSearchEngine>();
		for (SearchEngine engine : getSearchEngines()) {
			if (engine.isEnabled()) {
				infos.add(new ClientSearchEngine(engine.getCode(), engine.getFriendlyName(), engine.isOnByDefault()));
			}
		}
		return infos;
	}

	public List<SpectrumQaParamFileInfo> listSpectrumQaParamFiles() throws GWTServiceException {

		List<SpectrumQaParamFileInfo> paramFiles = new ArrayList<SpectrumQaParamFileInfo>();
		if (SwiftWebContext.getServletConfig().isMsmsEval()) {
			for (MSMSEvalParamFile paramFile : SwiftWebContext.getServletConfig().getSpectrumQaParamFiles()) {
				paramFiles.add(new SpectrumQaParamFileInfo(paramFile.getPath(), paramFile.getDescription()));
			}
		}
		return paramFiles;
	}

	public boolean isScaffoldReportEnabled() throws GWTServiceException {
		return SwiftWebContext.getServletConfig().isScaffoldReport();
	}

	public boolean isQaEnabled() throws GWTServiceException {
		return SwiftWebContext.getServletConfig().isQa();
	}

	public String getUserMessage() throws GWTServiceException {
		// TODO - re-add support for user messages
		return null;
	}

	public FileInfo[] findFiles(String[] relativePaths) throws GWTServiceException {
		try {
			// Filter paths so we never have both parent and a child in the list.
			List<String> filteredPaths = new ArrayList<String>();
			for (int i = 0; i < relativePaths.length; i++) {
				int j;
				for (j = 0; j < relativePaths.length; j++) {
					// Our path is a child, throw it away
					if (i != j && relativePaths[i].startsWith(relativePaths[j])) {
						break;
					}
				}
				// This path is legitimate, not a child of anything
				if (j == relativePaths.length) {
					filteredPaths.add(relativePaths[i]);
				}
			}

			List<FileInfo> list = new ArrayList<FileInfo>();

			for (String path : filteredPaths) {
				addPathToList(new File(getBrowseRoot(), path), list);
			}

			FileInfo[] results = new FileInfo[list.size()];
			return list.toArray(results);
		} catch (Exception t) {
			LOGGER.error("Could not find files", t);
			throw GWTServiceExceptionFactory.createException("Could not find files", t);
		}
	}

	private void addPathToList(File file, List<FileInfo> list) {
		if (!file.exists() || file.isHidden()) {
			return;
		}
		if (file.isDirectory()) {
			// Recursively add the contents
			// TODO: Limit the running time
			File[] files = file.listFiles(FILTER_DIRS);
			Arrays.sort(files);
			for (File path : files) {
				addPathToList(path, list);
			}
		} else {
			if (FILTER.accept(file.getParentFile(), file.getName())) {
				String path = file.getAbsolutePath();
				path = path.substring(getBrowseRoot().getAbsolutePath().length());
				path = path.replaceAll(Pattern.quote(File.separator), "/");
				list.add(new FileInfo(path, file.length()));
			}
		}
	}

	/**
	 * Returns true if at least one of the subs is subfolder (direct or indirect) of dir.
	 * If subs are null, the result is false, if dir is null, the result is true. This
	 * follows the logic, that the subs are a list of "expanded" paths and we are trying to determine,
	 * whether the given folder should be expanded or not.
	 *
	 * @param dir  The folder that has to contain at least one of the subs.
	 * @param subs Array of subfolder paths.
	 * @return True when at least one of the subs is inside dir (directly or not).
	 */
	private boolean isSubfolder(File dir, File[] subs) {
		if (subs == null) {
			return false;
		}
		if (dir == null) {
			return true;
		}
		for (File sub : subs) {
			if (sub.getPath().startsWith(dir.getPath())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Lists contents of given directory and appends them to given file element.
	 * Errors are appended as special "error node".
	 *
	 * @param rootEntry     Entry to insert contents into.
	 * @param root          Root directory.
	 * @param expandedPaths List of paths that have to be expanded in the listing.
	 */
	private void listDirectoryContents(Entry rootEntry, File root, File[] expandedPaths) {
		// find all directories
		File dirs[] = null;
		try {
			dirs = root.listFiles(new NotHiddenFilter());
		} catch (SecurityException e) {
			LOGGER.debug("Could not list contents of " + root.getAbsolutePath(), e);
			rootEntry.addChild(new ErrorEntry(MessageFormat.format("Could not list contents of {0}: {1}", root.getAbsolutePath(), e.getMessage())));
			return;
		}

		if (dirs != null) {
			Arrays.sort(dirs, new FilenameComparator());
			for (File dir : dirs) {
				if (dir.isDirectory()) {
					DirectoryEntry directory = new DirectoryEntry(dir.getName());
					rootEntry.addChild(directory);
					// If this directory should be expanded
					if (isSubfolder(dir, expandedPaths)) {
						listDirectoryContents(directory, dir, expandedPaths);
					}
				}
			}
		}

		// find all the files with allowed extension
		File[] files = null;
		try {
			files = root.listFiles(FILTER);
		} catch (SecurityException e) {
			LOGGER.debug("Could not list contents of " + root.getAbsolutePath(), e);
			rootEntry.addChild(new ErrorEntry(MessageFormat.format("Could not list contents of {0}: {1}", root.getAbsolutePath(), e.getMessage())));
			return;
		}
		if (files != null) {
			Arrays.sort(files, new FilenameComparator());
			for (File file : files) {
				if (!file.isDirectory()) {
					rootEntry.addChild(new FileEntry(file.getName()));
				}
			}
		}
	}

	private static final File[] EMPTY_EXPANDED_FILES = new File[0];

	public Boolean login(String userName, String password) throws GWTServiceException {
		return true;
	}

	public synchronized ClientParamSet save(Service.Token t, ClientParamSet toCopy, String newName, String ownerEmail,
	                                        String ownerInitials,
	                                        boolean permanent) throws GWTServiceException {
		try {
			HttpSession session = compareToken(t);
			getParamsDao().begin();
			ParameterSetCache cache = new ParameterSetCache(session, getParamsDao());
			SearchEngineParameters ps = cache.getFromCacheHibernate(toCopy);
			ClientParamSet ret;
			if (!permanent) {
				ret = cache.installTemporary(toCopy);
			} else {
				final Change change = new Change("Saving parameter set " + newName, new DateTime());

				// Delete if already exists
				SavedSearchEngineParameters params = getParamsDao().findSavedSearchEngineParameters(newName);
				if (params != null) {
					getParamsDao().deleteSavedSearchEngineParameters(params, change);
				}

				ps = getParamsDao().addSearchEngineParameters(ps);
				User user = getWorkspaceDao().getUserByEmail(ownerEmail);

				SavedSearchEngineParameters newParams = new SavedSearchEngineParameters(
						newName, user, ps);

				newParams = getParamsDao().addSavedSearchEngineParameters(newParams, change);

				if (toCopy.isTemporary()) {
					cache.removeFromCache(toCopy);
				}
				ret = new ClientParamSet(newParams.getId(), newName, ownerEmail, ownerInitials);
			}
			getParamsDao().commit();
			return ret;
		} catch (Exception e) {
			getParamsDao().rollback();
			LOGGER.error("Could not save client parameter set", e);
			throw GWTServiceExceptionFactory.createException("Could not save client parameter set", e);
		}
	}


	public ClientParamFile[] getFiles(Service.Token t, ClientParamSet paramSet) throws GWTServiceException {
		try {
			HttpSession session = compareToken(t);
			getParamsDao().begin();
			ParameterSetCache cache = new ParameterSetCache(session, getParamsDao());
			SearchEngineParameters ps = cache.getFromCache(paramSet);
			ClientParamFile[] files = new ClientParamFile[getSearchEngines().size()];

			int i = 0;
			for (SearchEngine engine : getSearchEngines()) {
				final String parameters = engine.writeSearchEngineParameterString(ps, null);
				files[i++] = new ClientParamFile(engine.getFriendlyName(), parameters);
			}
			getParamsDao().commit();
			return files;
		} catch (Exception e) {
			getParamsDao().rollback();
			LOGGER.error("Could not get parameter files", e);
			throw GWTServiceExceptionFactory.createException("Could not get parameter files", e);
		}
	}

	public ClientParamSetValues getParamSetValues(Service.Token t, ClientParamSet paramSet) throws GWTServiceException {
		try {
			HttpSession session = compareToken(t);
			getParamsDao().begin();
			ParameterSetCache cache = new ParameterSetCache(session, getParamsDao());
			SearchEngineParameters ps = cache.getFromCache(paramSet);
			final ParamsValidations paramsValidations = SearchEngine.validate(ps, getSearchEngines());
			final ClientParamSetValues clientParamSetValues = getClientProxyGenerator().convertValues(ps, paramsValidations);
			getParamsDao().commit();
			return clientParamSetValues;
		} catch (Exception e) {
			getParamsDao().rollback();
			LOGGER.error("Could not get parameter set values", e);
			throw GWTServiceExceptionFactory.createException("Could not get parameter set values", e);
		}
	}

	public synchronized ClientParamSetList getParamSetList(Service.Token t) throws GWTServiceException {
		try {
			HttpSession session = compareToken(t);
			getParamsDao().begin();
			ParameterSetCache cache = new ParameterSetCache(session, getParamsDao());
			final ClientParamSetList paramSetList = makeParamSetList(cache);
			getParamsDao().commit();
			return paramSetList;
		} catch (Exception e) {
			getParamsDao().rollback();
			LOGGER.error("Could not obtain parameter set list", e);
			throw GWTServiceExceptionFactory.createException("Could not obtain parameter set list", e);
		}
	}

	private ClientParamSetList makeParamSetList(ParameterSetCache cache) {
		final List<SavedSearchEngineParameters> engineParametersList = getParamsDao().savedSearchEngineParameters();
		return ClientProxyGenerator.getClientParamSetList(engineParametersList, cache.getTemporaryClientParamList());
	}

	public List<List<ClientValue>> getAllowedValues(Service.Token t, ClientParamSet paramSet, String[] params, String[] mappingDatas) throws GWTServiceException {
		try {
			getParamsDao().begin();

			if (params.length != mappingDatas.length) {
				throw new MprcException("params must match mappingDatas");
			}

			List<List<ClientValue>> values = new ArrayList<List<ClientValue>>();
			for (String param : params) {
				ParamName name = ParamName.getById(param);
				values.add(getClientProxyGenerator().getAllowedValues(name, getParamsInfo()));
			}
			getParamsDao().commit();
			return values;
		} catch (Exception e) {
			getParamsDao().rollback();
			LOGGER.error("Could not get parameter set allowed values", e);
			throw GWTServiceExceptionFactory.createException("Could not refresh parameter set", e);
		}
	}

	public ClientParamsValidations update(Service.Token t, ClientParamSet paramSet, String param, ClientValue value) throws GWTServiceException {
		try {
			HttpSession session = compareToken(t);
			getParamsDao().begin();
			ParameterSetCache cache = new ParameterSetCache(session, getParamsDao());

			SearchEngineParameters ps = cache.getFromCache(paramSet);
			try {
				ParamName name = ParamName.getById(param);
				ps.setValue(name, getClientProxyGenerator().convert(value, getParamsInfo().getAllowedValues(name)));
				ParamsValidations validations = SearchEngine.validate(ps, getSearchEngines());
				final ClientParamsValidations validationList = getClientProxyGenerator().convertTo(validations);
				getParamsDao().commit();
				return validationList;
			} catch (ClientProxyGenerator.ConversionException e) {
				return getValidationForException(param, e.getCause());
			} catch (Exception e) {
				return getValidationForException(param, e);
			}
		} catch (Exception e) {
			getParamsDao().rollback();
			LOGGER.error("Could not update parameter set", e);
			throw GWTServiceExceptionFactory.createException("Could not update parameter set", e);
		}
	}

	public void delete(Service.Token t, ClientParamSet paramSet) throws GWTServiceException {
		try {
			HttpSession session = compareToken(t);
			getParamsDao().begin();
			final SavedSearchEngineParameters savedSearchEngineParameters = getParamsDao().findSavedSearchEngineParameters(paramSet.getName());
			if (savedSearchEngineParameters != null) {
				getParamsDao().deleteSavedSearchEngineParameters(savedSearchEngineParameters, new Change("Deleting saved search engine parameters [" + savedSearchEngineParameters.getName() + "] with id " + savedSearchEngineParameters.getId(), new DateTime()));
				ParameterSetCache cache = new ParameterSetCache(session, getParamsDao());
				cache.removeFromCache(paramSet);
			}
			getParamsDao().commit();
		} catch (Exception e) {
			getParamsDao().rollback();
			String errMsg = "Could not delete parameter set " + (paramSet != null ? paramSet.getName() : "");
			LOGGER.error(errMsg, e);
			throw GWTServiceExceptionFactory.createException(errMsg, e);
		}
	}

	private ClientParamsValidations getValidationForException(String param, Throwable e) {
		ClientValidationList list = new ClientValidationList();
		ClientValidation cv = new ClientValidation(e.getMessage());
		cv.setThrowableStackTrace(ExceptionUtilities.stringifyStackTrace(e));
		cv.setParamId(param);
		cv.setSeverity(ClientValidation.SEVERITY_ERROR);
		cv.setThrowableMessage(MprcException.getDetailedMessage(e));
		list.add(cv);
		Map<String, ClientValidationList> map = new HashMap<String, ClientValidationList>();
		map.put(param, list);
		return new ClientParamsValidations(map);
	}

	@Override
	public ClientDatabaseUndeployerProgress undeployDatabase(String dbToUndeploy) throws GWTServiceException {
		try {
			getCurationDao().begin();
			final Curation curation = getCurationDao().findCuration(dbToUndeploy);
			final DatabaseUndeployerProgress progressMessage = DatabaseUndeployerCaller.callDatabaseUndeployer(getDatabaseUndeployerDaemonConnection(), curation);
			getCurationDao().commit();

			return getDbUndeployerProgressMessageProxy(progressMessage);
		} catch (Exception e) {
			getCurationDao().rollback();
			String errMsg = "Could not undeploy database " + dbToUndeploy;
			LOGGER.error(errMsg, e);
			throw GWTServiceExceptionFactory.createException(errMsg, e);
		}
	}

	@Override
	public ClientDatabaseUndeployerProgress getProgressMessageForDatabaseUndeployment(Long taskId) throws GWTServiceException {
		return getDbUndeployerProgressMessageProxy(DatabaseUndeployerCaller.getMessageFromQueue(taskId));
	}

	@Override
	public boolean isDatabaseUndeployerEnabled() throws GWTServiceException {
		return SwiftWebContext.getServletConfig().isDatabaseUndeployerEnabled();
	}

	/**
	 * Compare session id from Cookie with passed in token to prevent CSRF.
	 *
	 * @see <a href="http://groups.google.com/group/Google-Web-Toolkit/web/security-for-gwt-applications">Security for GWT applications</a>
	 * @see <a href="http://en.wikipedia.org/wiki/Cross-site_request_forgery">Cross-site request forgery</a>
	 */
	protected synchronized HttpSession compareToken(Service.Token t) {
		return getThreadLocalRequest().getSession();
		//String sessionId = session.getId();
		//assert(t.getToken().equals(sessionId);
	}

	private ClientDatabaseUndeployerProgress getDbUndeployerProgressMessageProxy(DatabaseUndeployerProgress progressMessage) {
		return new ClientDatabaseUndeployerProgress(progressMessage.getDatabaseUndeployerTaskId(), progressMessage.getProgressMessage(), progressMessage.isLast());
	}

	public Collection<SearchEngine> getSearchEngines() {
		return new ArrayList<SearchEngine>(SwiftWebContext.getServletConfig().getSearchEngines());
	}

	public File getBrowseRoot() {
		return SwiftWebContext.getServletConfig().getBrowseRoot();
	}

	public DaemonConnection getSwiftSearcherDaemonConnection() {
		return SwiftWebContext.getServletConfig().getSwiftSearcherDaemonConnection();
	}

	public SwiftDao getSwiftDao() {
		return SwiftWebContext.getServletConfig().getSwiftDao();
	}

	public WorkspaceDao getWorkspaceDao() {
		return SwiftWebContext.getServletConfig().getWorkspaceDao();
	}

	public CurationDao getCurationDao() {
		return SwiftWebContext.getServletConfig().getCurationDao();
	}

	public ParamsDao getParamsDao() {
		return SwiftWebContext.getServletConfig().getParamsDao();
	}

	public ParamsInfo getParamsInfo() {
		return SwiftWebContext.getServletConfig().getParamsInfo();
	}

	public DaemonConnection getDatabaseUndeployerDaemonConnection() {
		return SwiftWebContext.getServletConfig().getDatabaseUndeployerDaemonConnection();
	}

	public ClientProxyGenerator getClientProxyGenerator() {
		return new ClientProxyGenerator(SwiftWebContext.getServletConfig().getUnimodDao(), getWorkspaceDao(), getSwiftDao(), getBrowseRoot());
	}

	private class Resolver implements ClientParamSetResolver {

		private ParameterSetCache cache;
		private boolean clientParamSetListChanged;

		public Resolver(ParameterSetCache cache) {
			this.cache = cache;
			clientParamSetListChanged = false;
		}

		@Override
		public ClientParamSet resolve(SearchEngineParameters parameters, User user) {
			// Find all saved parameter sets that match these parameters
			final SavedSearchEngineParameters bestSavedSearchEngineParameters = getParamsDao().findBestSavedSearchEngineParameters(parameters, user);
			if (bestSavedSearchEngineParameters != null) {
				return new ClientParamSet(
						bestSavedSearchEngineParameters.getId(),
						bestSavedSearchEngineParameters.getName(),
						bestSavedSearchEngineParameters.getUser().getUserName(),
						bestSavedSearchEngineParameters.getUser().getInitials());
			}

			// We did not find a perfect match within saved search parameters
			// Try the temporary ones
			final ClientParamSet matchingTemporaryParamSet = cache.findMatchingTemporaryParamSet(parameters);
			if (matchingTemporaryParamSet != null) {
				return matchingTemporaryParamSet;
			}

			// We will make a plain temporary parameter set and return that one
			clientParamSetListChanged = true;
			return cache.installTemporary("Previous search parameters", user.getUserName(), user.getInitials(), parameters.copy());
		}

		@Override
		public boolean isClientParamSetListChanged() {
			return clientParamSetListChanged;
		}

	}
}
