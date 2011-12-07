package edu.mayo.mprc.swift.db;

import com.google.common.base.Preconditions;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.AssignedTaskData;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.progress.ProgressReport;
import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.swift.dbmapping.*;
import edu.mayo.mprc.workflow.persistence.TaskState;
import edu.mayo.mprc.workspace.User;
import edu.mayo.mprc.workspace.WorkspaceDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Restrictions;

import java.io.File;
import java.util.*;

public final class SwiftDaoHibernate extends DaoBase implements SwiftDao {
	private static final Logger LOGGER = Logger.getLogger(SwiftDaoHibernate.class);

	private FileTokenFactory fileTokenFactory;
	private final Object taskStatesLock = new Object();
	private Map<TaskState, TaskStateData> taskStates = null;
	private WorkspaceDao workspaceDao;
	private List<SearchEngine> searchEngines;

	public SwiftDaoHibernate() {
		super(null);
	}

	public SwiftDaoHibernate(DatabasePlaceholder databasePlaceholder) {
		super(databasePlaceholder);
	}

	@Override
	public boolean isExistingTitle(String title, User user) {
		try {
			Number qusers = (Number) getSession().createQuery("select count(*) from edu.mayo.mprc.swift.dbmapping.SearchRun t where t.title=:title and t.submittingUser.id=:userId")
					.setString("title", title)
					.setParameter("userId", user.getId())
					.uniqueResult();
			return qusers.intValue() > 0;
		} catch (Exception t) {
			throw new MprcException("Cannot determine whether title " + title + " exists for user " + user, t);
		}
	}

	@Override
	public List<TaskData> getTaskDataList(final int searchRunId) {
		try {
			return (List<TaskData>) getSession().createQuery("from TaskData t where t.searchRun.id=:searchRunId order by t.startTimestamp desc")
					.setInteger("searchRunId", searchRunId)
					.list();
		} catch (Exception t) {
			throw new MprcException("Cannot obtain task status list", t);
		}
	}

	@Override
	public TaskData getTaskData(final Integer taskId) {
		try {
			return (TaskData) getSession().get(TaskData.class, taskId);
		} catch (Exception t) {
			throw new MprcException("Cannot obtain task data for id " + taskId, t);
		}
	}

	@Override
	public List<SearchRun> getSearchRunList(final SearchRunFilter filter) {
		try {
			Criteria criteria = getSession().createCriteria(SearchRun.class);
			filter.updateCriteria(criteria);
			criteria.setCacheable(true)
					.setReadOnly(true);

			return criteria.list();
		} catch (Exception t) {
			throw new MprcException("Cannot obtain search run status list for filter: " + filter, t);
		}
	}

	@Override
	public int getNumberRunningTasksForSearchRun(final SearchRun searchRun) {
		// Do not hit database for finished search runs. Counting running tasks is costly
		if (searchRun.isCompleted()) {
			return 0;
		}
		try {
			long howmanyrunning = (Long) getSession().createQuery("select count(t) from TaskData t where t.searchRun=:searchRun and t.taskState.description='" + TaskState.RUNNING.getText() + "'")
					.setParameter("searchRun", searchRun)
					.uniqueResult();
			return (int) howmanyrunning;
		} catch (Exception t) {
			throw new MprcException("Cannot determine number of running tasks for search run " + searchRun.getTitle(), t);
		}
	}

	@Override
	public Set<SearchRun> getSearchRuns(final boolean showSuccess, final boolean showFailure, final boolean showWarnings, final Date updatedSince) {

		final Set<SearchRun> resultSet = new HashSet<SearchRun>();

		Session session = getSession();
		try {
			LogicalExpression timeCriteria;
			if (updatedSince == null) {
				timeCriteria = null;
			} else {
				timeCriteria = Restrictions.or(
						Restrictions.gt("startTimestamp", updatedSince),
						Restrictions.gt("endTimestamp", updatedSince));
			}

			if (showSuccess) {
				Criteria criteriaQuery = session.createCriteria(SearchRun.class);
				if (timeCriteria != null) {
					criteriaQuery.add(timeCriteria);
				}
				criteriaQuery.add(Restrictions.and(Restrictions.isNotNull("endTimestamp"), Restrictions.eq("tasksFailed", 0)));
				resultSet.addAll(criteriaQuery.list());
			}

			if (showFailure) {
				Criteria criteriaQuery = session.createCriteria(SearchRun.class);
				if (timeCriteria != null) {
					criteriaQuery.add(timeCriteria);
				}
				criteriaQuery.add(Restrictions.gt("tasksFailed", 0));
				resultSet.addAll(criteriaQuery.list());
			}

			if (showWarnings) {
				Criteria criteriaQuery = session.createCriteria(SearchRun.class);
				if (timeCriteria != null) {
					criteriaQuery.add(timeCriteria);
				}
				criteriaQuery.add(Restrictions.gt("tasksWithWarning", 0));
				resultSet.addAll(criteriaQuery.list());
			}

		} catch (Exception t) {
			throw new MprcException("Cannot obtain a list search runs from the database.", t);
		}

		return resultSet;
	}

	@Override
	public SearchRun getSearchRunForId(int searchRunId) {
		try {
			SearchRun data = (SearchRun) getSession().get(SearchRun.class, searchRunId);
			if (data == null) {
				throw new MprcException("getSearchRunForId : search run id=" + searchRunId + " was not found.");
			}
			return data;
		} catch (Exception t) {
			throw new MprcException("Cannot obtain search run for id " + searchRunId, t);
		}
	}

	@Override
	public String getSearchRunStatusForUser(final String userName) {
		if (userName == null) {
			return "";
		}
		Session session = getSession();
		try {
			final User user = (User) session.createQuery("from edu.mayo.mprc.workspace.User d where d.userName = :name and d.deletion=null")
					.setParameter("name", userName)
					.uniqueResult();

			String result = "";
			if (user != null) {
				result = "<span class=\"user-name\">" + user.getFirstName() + " " + user.getLastName() + "</span>";

				final SearchRun searchRun = (SearchRun) session.createQuery("from SearchRun t where t.submittingUser=:user order by startTimestamp desc")
						.setParameter("user", user)
						.setMaxResults(1).uniqueResult();

				if (searchRun != null) {
					if (searchRun.getTasksFailed() > 0 || (searchRun.getErrorMessage() != null && searchRun.getErrorMessage().length() == 0)) {
						result += " <span class=\"error\">Error</span>";
					} else if (searchRun.getTasksCompleted() == searchRun.getNumTasks()) {
						result += " <span class=\"success\">Success</span>";
					} else {
						result += " <span class=\"running\">Running</span>";
					}
				}
			}

			return result;
		} catch (Exception ignore) {
			// Swallowed. If anything went wrong, the status is empty
			return "";
		}
	}

	private static Criterion getSearchEngineEqualityCriteria(SearchEngineConfig searchEngineConfig) {
		return DaoBase.nullSafeEq("code", searchEngineConfig.getCode());
	}

	private static Criterion getSearchEngineEqualityCriteria(String code) {
		return DaoBase.nullSafeEq("code", code);
	}

	@Override
	public void addSearchEngineConfig(SearchEngineConfig config, Change change) {
		try {
			save(config, change, getSearchEngineEqualityCriteria(config), false);
		} catch (Exception t) {
			throw new MprcException("Cannot add new search engine config '" + config.getCode() + "'", t);
		}
	}

	@Override
	public SearchEngineConfig getSearchEngineConfig(String code) {
		try {
			return get(SearchEngineConfig.class, getSearchEngineEqualityCriteria(code));
		} catch (Exception t) {
			throw new MprcException("Cannot obtain search engine config for engine " + code, t);
		}
	}

	@Override
	public EnabledEngines addEnabledEngineSet(Iterable<String> searchEngineCodes) {
		try {
			EnabledEngines engines = new EnabledEngines();
			for (String engineCode : searchEngineCodes) {
				final SearchEngineConfig searchEngineConfig = getSearchEngineConfig(engineCode);
				if (searchEngineConfig == null) {
					throw new MprcException("Can not find search engine configuration for engine " + engineCode);
				}
				engines.add(searchEngineConfig);
			}

			return updateSet(engines, engines.getEngineConfigs(), "engineConfigs");

		} catch (Exception t) {
			throw new MprcException("Could not add search engine set", t);
		}
	}

	@Override
	public EnabledEngines addEnabledEngines(EnabledEngines engines) {
		if (engines.getId() != null) {
			return engines;
		}
		Preconditions.checkNotNull(engines, "Enabled engine list must not be null");
		try {
			return updateSet(engines, engines.getEngineConfigs(), "engineConfigs");
		} catch (Exception t) {
			throw new MprcException("Could not add search engine set", t);
		}
	}

	private Criterion getSpectrumQaEqualityCriteria(SpectrumQa spectrumQa) {
		return Restrictions.and(
				DaoBase.nullSafeEq("engine", spectrumQa.getEngine()),
				DaoBase.nullSafeEq("paramFilePath", spectrumQa.getParamFilePath()));
	}

	@Override
	public SpectrumQa addSpectrumQa(SpectrumQa spectrumQa) {
		try {
			return save(spectrumQa, getSpectrumQaEqualityCriteria(spectrumQa), false);
		} catch (Exception t) {
			throw new MprcException("Could not add spectrum QA", t);
		}
	}

	private Criterion getPeptideReportEqualityCriteria(PeptideReport peptideReport) {
		return Restrictions.isNotNull("id");
	}

	@Override
	public PeptideReport addPeptideReport(PeptideReport peptideReport) {
		try {
			return save(peptideReport, getPeptideReportEqualityCriteria(peptideReport), false);
		} catch (Exception t) {
			throw new MprcException("Could not add peptide report", t);
		}
	}

	private FileSearch addFileSearch(FileSearch fileSearch) {
		try {
			fileSearch.setEnabledEngines(addEnabledEngines(fileSearch.getEnabledEngines()));
			return save(fileSearch, getFileSearchEqualityCriteria(fileSearch), false);
		} catch (Exception t) {
			throw new MprcException("Could not add file search information", t);
		}
	}

	private Criterion getFileSearchEqualityCriteria(FileSearch fileSearch) {
		return Restrictions.conjunction()
				.add(DaoBase.nullSafeEq("inputFile", fileSearch.getInputFile()))
				.add(DaoBase.nullSafeEq("biologicalSample", fileSearch.getBiologicalSample()))
				.add(DaoBase.nullSafeEq("categoryName", fileSearch.getCategoryName()))
				.add(DaoBase.nullSafeEq("experiment", fileSearch.getExperiment()))
				.add(DaoBase.associationEq("enabledEngines", fileSearch.getEnabledEngines()))
				.add(DaoBase.nullSafeEq("swiftSearchDefinitionId", fileSearch.getSwiftSearchDefinitionId()));
	}

	private Criterion getSwiftSearchDefinitionEqualityCriteria(SwiftSearchDefinition definition) {
		return Restrictions.conjunction()
				.add(DaoBase.nullSafeEq("title", definition.getTitle()))
				.add(DaoBase.associationEq("user", definition.getUser()))
				.add(DaoBase.nullSafeEq("outputFolder", definition.getOutputFolder()))
				.add(DaoBase.associationEq("qa", definition.getQa()))
				.add(DaoBase.associationEq("peptideReport", definition.getPeptideReport()));
	}

	@Override
	public SwiftSearchDefinition addSwiftSearchDefinition(SwiftSearchDefinition definition) {
		try {
			if (definition.getId() == null) {
				// We only save search definition that was not previously saved.
				// Once saved, the definition is immutable.

				// Save all the complex objects first, so we can ensure they get stored properly
				if (definition.getQa() != null) {
					definition.setQa(addSpectrumQa(definition.getQa()));
				}
				if (definition.getPeptideReport() != null) {
					definition.setPeptideReport(addPeptideReport(definition.getPeptideReport()));
				}

				List<FileSearch> inputFiles = new ArrayList<FileSearch>();
				for (FileSearch fileSearch : definition.getInputFiles()) {
					inputFiles.add(addFileSearch(fileSearch));
				}
				definition.setInputFiles(inputFiles);
				definition = saveLaxEquality(definition, getSwiftSearchDefinitionEqualityCriteria(definition), false);
			}
			return definition;

		} catch (Exception t) {
			throw new MprcException("Could not add swift search definition", t);
		}
	}

	@Override
	public SwiftSearchDefinition getSwiftSearchDefinition(Integer swiftSearchId) {
		if (swiftSearchId == null || swiftSearchId == 0) {
			return null;
		}
		try {
			return (SwiftSearchDefinition) getSession().load(SwiftSearchDefinition.class, swiftSearchId);
		} catch (Exception t) {
			throw new MprcException("Cannot obtain swift search definition for id " + swiftSearchId, t);
		}
	}

	@Override
	public void reportSearchRunProgress(int searchRunId, final ProgressReport progress) {
		try {
			final SearchRun searchRun = getSearchRunForId(searchRunId);
			LOGGER.debug("Persisting search run progress " + searchRun.getTitle() + "\n" + progress.toString());
			if (progress.getSucceeded() + progress.getFailed() == progress.getTotal() && searchRun.getEndTimestamp() == null) {
				searchRun.setEndTimestamp(new Date());
			}
			searchRun.setNumTasks(progress.getTotal());
			searchRun.setTasksCompleted(progress.getSucceeded());
			searchRun.setTasksFailed(progress.getFailed() - progress.getInitFailed());
		} catch (Exception t) {
			throw new MprcException("Cannot persist search run progress", t);
		}
	}

	@Override
	public SearchRun fillSearchRun(final SwiftSearchDefinition swiftSearch) {
		LOGGER.debug("Producing search run");

		try {
			// Lookup user
			final String userName = swiftSearch == null ? null : swiftSearch.getUser().getUserName();
			User user = null;
			if (userName != null) {
				user = (User) getSession().createQuery("from User u where u.userName='" + userName + "' and u.deletion=null").uniqueResult();
				if (user == null) {
					throw new MprcException("Unknown user: " + userName);
				}
			}

			// Lookup unknown report type

			final SearchRun data = new SearchRun(
					swiftSearch == null ? null : swiftSearch.getTitle(),
					user,
					swiftSearch,
					new Date(),
					null,
					0,
					null,
					1,
					0,
					0,
					0,
					false);

			try {
				getSession().saveOrUpdate(data);
			} catch (Exception t) {
				throw new MprcException("Cannot update search run [" + data.getTitle() + "] in the database", t);
			}
			return data;
		} catch (Exception t) {
			throw new MprcException("Cannot fill search run", t);
		}
	}

	@Override
	public TaskData updateTask(final TaskData task) {
		LOGGER.debug("Updating task\t'" + task.getTaskName());
		try {
			getSession().saveOrUpdate(task);
		} catch (Exception t) {
			throw new MprcException("Cannot update task " + task, t);
		}

		return task;
	}

	private void listToTaskStateMap(List<?> list) {
		taskStates = new HashMap<TaskState, TaskStateData>(list.size());
		for (Object o : list) {
			if (o instanceof TaskStateData) {
				TaskStateData stateData = (TaskStateData) o;
				taskStates.put(TaskState.fromText(stateData.getDescription()), stateData);
			}
		}
	}

	@Override
	public TaskStateData getTaskState(Session session, TaskState state) {
		synchronized (taskStatesLock) {
			if (taskStates == null) {
				listToTaskStateMap(session.createQuery("from TaskStateData").list());
			}
			return taskStates.get(state);
		}

	}

	@Override
	public TaskStateData getTaskState(TaskState state) {
		synchronized (taskStatesLock) {
			if (taskStates == null) {
				List<?> list = null;
				try {
					list = (List<?>) getSession().createQuery("from TaskStateData").list();
				} catch (Exception t) {
					throw new MprcException("", t);
				}
				listToTaskStateMap(list);
			}
			return taskStates.get(state);
		}
	}

	@Override
	public TaskData createTask(int searchRunId, final String name, final String descriptionLong, final TaskState taskState) {
		LOGGER.debug("Creating new task " + name + " " + descriptionLong + " " + taskState);
		final Session session = getSession();
		try {
			final SearchRun searchRun = getSearchRunForId(searchRunId);
			TaskData task = new TaskData(
					name,
					/*queueStamp*/ null,
					/*startStamp*/ null,
					/*endStamp*/ null,
					searchRun,
					getTaskState(session, taskState),
					descriptionLong);

			session.saveOrUpdate(task);
			return task;

		} catch (Exception t) {
			throw new MprcException("Cannot create a new task " + name + " (" + descriptionLong + ")", t);
		}
	}

	@Override
	public void storeReport(int searchRunId, final File resultFile) {
		try {
			ReportData r = new ReportData();

			r.setDateCreated(new Date());
			r.setReportFileId(resultFile);
			final SearchRun searchRun = getSearchRunForId(searchRunId);
			r.setSearchRun(searchRun);
			searchRun.getReports().add(r);
			getSession().saveOrUpdate(r);
		} catch (Exception t) {
			throw new MprcException("Cannot store search run " + searchRunId, t);
		}
	}

	@Override
	public void storeAssignedTaskData(final TaskData taskData, final AssignedTaskData assignedTaskData) {
		try {
			taskData.setGridJobId(assignedTaskData.getAssignedId());
			taskData.setOutputLogDatabaseToken(fileTokenFactory.getDatabaseToken(fileTokenFactory.getLogFileTokenForRemoteToken(assignedTaskData.getOutputLogFileToken())));
			taskData.setErrorLogDatabaseToken(fileTokenFactory.getDatabaseToken(fileTokenFactory.getLogFileTokenForRemoteToken(assignedTaskData.getErrorLogFileToken())));
		} catch (Exception t) {
			throw new MprcException("Cannot store task grid request id " + assignedTaskData.getAssignedId() + " for task " + taskData, t);
		}
	}

	@Override
	public void searchRunFailed(int searchRunId, String message) {
		final SearchRun searchRun = getSearchRunForId(searchRunId);
		searchRun.setErrorMessage(message);
		searchRun.setEndTimestamp(new Date());
	}

	public FileTokenFactory getFileTokenFactory() {
		return fileTokenFactory;
	}

	public void setFileTokenFactory(FileTokenFactory fileTokenFactory) {
		this.fileTokenFactory = fileTokenFactory;
	}

	@Override
	public String check(Map<String, String> params) {
		// First, the workspace has to be defined, with a user
		String workspaceCheck = workspaceDao.check(params);
		if (workspaceCheck != null) {
			return workspaceCheck;
		}

		if (rowCount(TaskStateData.class) == 0) {
			return "The task state enumeration is not initialized";
		}
		if (rowCount(SearchRun.class) == 0) {
			return "There were no searches previously run";
		}
		final long searchEngineCount = countAll(SearchEngineConfig.class);
		if (searchEngineCount == 0) {
			return "No search engines are defined";
		}
		if (searchEngineCount < searchEngines.size()) {
			return "Not all search engines are stored in the database";
		}
		return null;
	}

	@Override
	public void initialize(Map<String, String> params) {
		// Initialize the dependent DAO
		workspaceDao.initialize(params);

		if (rowCount(TaskStateData.class) == 0) {
			LOGGER.info("Initializing task state enumeration");
			for (TaskState state : TaskState.values()) {
				getSession().saveOrUpdate(new TaskStateData(state.getText()));
			}
		}

		final long searchEngineCount = countAll(SearchEngineConfig.class);
		if (searchEngineCount < searchEngines.size()) {
			Change change = new Change(
					searchEngineCount == 0 ?
							"Installing initial list of search engines" :
							"Updating list of search engines", new Date());
			LOGGER.info(change.getReason());
			for (SearchEngine engine : searchEngines) {
				final SearchEngineConfig searchEngineConfig = new SearchEngineConfig(engine.getCode());
				this.addSearchEngineConfig(searchEngineConfig, change);
			}
		}
	}

	public WorkspaceDao getWorkspaceDao() {
		return workspaceDao;
	}

	public void setWorkspaceDao(WorkspaceDao workspaceDao) {
		this.workspaceDao = workspaceDao;
	}

	public List<SearchEngine> getSearchEngines() {
		return searchEngines;
	}

	public void setSearchEngines(List<SearchEngine> searchEngines) {
		this.searchEngines = searchEngines;
	}
}