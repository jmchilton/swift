package edu.mayo.mprc.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A runner is code that waits for work packets and then passes them to a worker.
 */
@XStreamAlias("runner")
public abstract class RunnerConfig implements ResourceConfig {
	private ResourceConfig workerConfiguration;

	public RunnerConfig() {
	}

	public RunnerConfig(final ResourceConfig workerConfiguration) {
		this.workerConfiguration = workerConfiguration;
	}

	public ResourceConfig getWorkerConfiguration() {
		return workerConfiguration;
	}

	public void setWorkerConfiguration(final ResourceConfig workerConfiguration) {
		this.workerConfiguration = workerConfiguration;
	}

	@Override
	public String toString() {
		return "RunnerConfig{" +
				"workerConfiguration=" + workerConfiguration +
				'}';
	}
}
