package edu.mayo.mprc.workflow.persistence;

public enum TaskState {
	/**
	 * Nobody started anything.
	 */
	UNINITIALIZED("Uninitialized"),
	/**
	 * All our inputs are ready, we can run.
	 */
	READY("Ready"),
	/**
	 * We are running right now.
	 */
	RUNNING("Running"),
	/**
	 * The execution failed.
	 */
	RUN_FAILED("Run Failed"),
	/**
	 * Success!
	 */
	COMPLETED_SUCCESFULLY("Completed Successfully"),
	/**
	 * Could not even start running, because inputs failed.
	 */
	INIT_FAILED("InitializationFailed");

	TaskState(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public static TaskState fromText(String text) {
		for (TaskState state : TaskState.values()) {
			if (state.getText().equals(text)) {
				return state;
			}
		}
		return null;
	}

	private final String text;
}
