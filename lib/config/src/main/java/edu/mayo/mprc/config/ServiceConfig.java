package edu.mayo.mprc.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * The service manager allows the application to send a service a work packet. The service does the work
 * and responds.
 * <p/>
 * A service configuration on its own does not define how is the service provided. It is just an identified endpoint.
 * How the service is run is defined by a runner, which contains a link to the worker itself.
 */
public final class ServiceConfig implements ResourceConfig {
	@XStreamAlias("name")
	@XStreamAsAttribute
	private String name;

	@XStreamAlias("runner")
	private RunnerConfig runner;

	@XStreamAlias("brokerUrl")
	private String brokerUrl;
	public static final String BROKER_URL_PROPERTY = "brokerUrl";

	public ServiceConfig() {
	}

	public ServiceConfig(String name, RunnerConfig runner, String brokerUrl) {
		this.name = name;
		this.runner = runner;
		this.brokerUrl = brokerUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RunnerConfig getRunner() {
		return runner;
	}

	public void setRunner(RunnerConfig runner) {
		this.runner = runner;
	}

	public String getBrokerUrl() {
		return brokerUrl;
	}

	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	public Map<String, String> save(DependencyResolver resolver) {
		return new HashMap<String, String>(0);
	}

	public void load(Map<String, String> values, DependencyResolver resolver) {
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public String toString() {
		return "ServiceConfig{" +
				"name='" + name + '\'' +
				", runner=" + runner +
				", brokerUrl='" + brokerUrl + '\'' +
				'}';
	}
}
