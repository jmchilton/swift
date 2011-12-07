package edu.mayo.mprc.daemon;

/**
 * Creates a fully configured worker. This is used as a parameter to {@link SimpleRunner}.
 */
public interface WorkerFactory {
	/**
	 * @return Fully configured worker.
	 */
	Worker createWorker();

	/**
	 * @return Description of the workers to be created.
	 */
	String getDescription();
}
