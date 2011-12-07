package edu.mayo.mprc.workflow.engine;

import edu.mayo.mprc.workflow.persistence.TaskState;

import java.io.File;
import java.util.Date;
import java.util.List;

public interface Task {
	void setWorkflowEngine(WorkflowEngine engine);

	WorkflowEngine getEngine();

	String getDescription();

	void setDescription(String description);

	void setName(String name);

	String getName();

	Date getBecameReady();

	Date getExecutionStarted();

	Date getExecutionFinished();

	String getExecutedOnHost();

	void setExecutedOnHost(String executedOnHost);

	/**
	 * Changes the task state. As the state changes, several things happen:
	 * <ul>
	 * <li>the state transition is validated</li>
	 * <li>timestamps are recorded</li>
	 * <li>the workflow engine is notified of the state change</li>
	 *
	 * @param newState State to switch to.
	 */
	void setState(TaskState newState);

	TaskState getState();

	boolean isFailed();

	boolean isSuccessful();

	boolean isDone();

	boolean stateEquals(TaskState checkAgainst);

	void setError(Throwable error);

	Throwable getLastError();// Resumes given context when file appears.

	void completeWhenFileAppears(File file);

	/**
	 * This task needs input from another task.
	 *
	 * @param task Task we need input from.
	 */
	void addDependency(Task task);

	/**
	 * The given input task just finished execution.
	 *
	 * @param input Input task.
	 */
	void inputDone(Task input);

	/**
	 * @return Will be called once, switching the task to "running" mode.
	 */
	void run();

	/**
	 * @return Who we depend on.
	 */
	List<Task> getInputs();

	/**
	 * @return Who depends on us.
	 */
	List<Task> getOutputs();

	/**
	 * @return &lt;engine_id&gt;.&lt;task_id&gt;
	 */
	String getFullId();
}
