package edu.mayo.mprc.swift.db;

import edu.mayo.mprc.config.RuntimeInitializer;
import edu.mayo.mprc.daemon.AssignedTaskData;
import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.Dao;
import edu.mayo.mprc.swift.dbmapping.*;
import edu.mayo.mprc.utilities.progress.ProgressReport;
import edu.mayo.mprc.workflow.persistence.TaskState;
import edu.mayo.mprc.workspace.User;
import org.hibernate.Session;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface SwiftDao extends Dao, RuntimeInitializer {
	/**
	 * see if a title already exists
	 *
	 * @param title Title of the search (as the user entered it)
	 * @param user  The user to run the search.
	 * @return true when the given user already entered transaction of given title.
	 */
	boolean isExistingTitle(String title, User user);

	/**
	 * retrieve the tasks data list for given search run id.
	 * <p/>
	 * The tasks are ordered descending by their start time.
	 *
	 * @param searchRunId Hibernate ID of the search run
	 * @return List of tasks
	 */
	List<TaskData> getTaskDataList(int searchRunId);

	TaskData getTaskData(Integer taskId);

	/**
	 * get the status information for all the search runs. The list is considered read-only - do not modify those searches.
	 *
	 * @param filter Filter for the search runs.
	 * @return List of all search runs filtered and ordered as {@link SearchRunFilter specifies}.
	 */
	List<SearchRun> getSearchRunList(SearchRunFilter filter);

	/**
	 * find number of running tasks for a search run
	 *
	 * @param searchRun - the search run record object  @see SearchRun
	 * @return - how many tasks are running
	 */
	int getNumberRunningTasksForSearchRun(SearchRun searchRun);

	/**
	 * This will find any search runs that have either started or ended since a give time.
	 *
	 * @param updatedSince will only return search runs updated since this time
	 * @return
	 */
	Set<SearchRun> getSearchRuns(boolean showSuccess, boolean showFailure, boolean showWarnings, Date updatedSince);

	SearchRun getSearchRunForId(int searchRunId);

	/**
	 * Returns an HTML string representing status information for current user's searches.
	 *
	 * @param userName
	 */
	String getSearchRunStatusForUser(String userName);

	/**
	 * Adds a search engine config. If the config already exists, the object gets updated.
	 *
	 * @param config Search engine config.
	 * @param change Description of the change that caused this addition.
	 */
	void addSearchEngineConfig(SearchEngineConfig config, Change change);

	SearchEngineConfig getSearchEngineConfig(String code);

	EnabledEngines addEnabledEngineSet(Iterable<String> searchEngineCodes);

	EnabledEngines addEnabledEngines(EnabledEngines engines);

	SpectrumQa addSpectrumQa(SpectrumQa spectrumQa);

	PeptideReport addPeptideReport(PeptideReport peptideReport);

	SwiftSearchDefinition addSwiftSearchDefinition(SwiftSearchDefinition definition);

	SwiftSearchDefinition getSwiftSearchDefinition(Integer swiftSearchId);

	void reportSearchRunProgress(int searchRunId, ProgressReport progress);

	/**
	 * Create a new search run, fill it with initial values, put it in the database.
	 *
	 * @param swiftSearch Search to be run.
	 * @return Search run serialized into the database.
	 */
	SearchRun fillSearchRun(SwiftSearchDefinition swiftSearch);

	/**
	 * @param task Task to update in the database.
	 * @return The updated version of the task.
	 */
	TaskData updateTask(TaskData task);

	TaskStateData getTaskState(Session session, TaskState state);

	/**
	 * Loads the entire task state table into a hash map so it can function at reasonable speeed.
	 *
	 * @param state State to translate to {@link TaskStateData}
	 * @return {@link TaskStateData} for given {@link TaskState}.
	 */
	TaskStateData getTaskState(TaskState state);

	TaskData createTask(int searchRunId, String name, String descriptionLong, TaskState taskState);

	ReportData storeReport(int searchRunId, File resultFile);

	/**
	 * Find search report file for given report id. Used to retrieve {@link #storeReport} result
	 * after only the report ID got transfered over the network.
	 *
	 * @param reportDataId Id of the {@link ReportData} object.
	 * @return {@link ReportData} for the given id.
	 */
	ReportData getReportForId(long reportDataId);

	void storeAssignedTaskData(TaskData taskData, AssignedTaskData assignedTaskData);

	void searchRunFailed(int searchRunId, String message);


}
