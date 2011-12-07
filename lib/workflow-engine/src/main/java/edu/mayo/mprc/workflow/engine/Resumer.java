package edu.mayo.mprc.workflow.engine;

public interface Resumer {
	/**
	 * Async call has returned, please resume the operation.
	 */
	void resume();
}
