package edu.mayo.mprc.peaks;

import edu.mayo.mprc.config.DependencyResolver;
import edu.mayo.mprc.config.ResourceConfig;

import java.util.Map;
import java.util.TreeMap;

/**
 * Configuration for the factory
 */
public class PeaksConfig implements ResourceConfig {
	private String baseURI;
	private String userName;
	private String password;

	public PeaksConfig() {
	}

	public PeaksConfig(final String baseURI, final String userName, final String password) {
		this.baseURI = baseURI;
		this.userName = userName;
		this.password = password;
	}

	public final String getUserName() {
		return userName;
	}

	public final String getPassword() {
		return password;
	}

	public final String getBaseURI() {
		return baseURI;
	}

	public final Map<String, String> save(final DependencyResolver resolver) {
		final Map<String, String> map = new TreeMap<String, String>();
		map.put("baseURI", baseURI);
		map.put("userName", userName);
		map.put("password", password);
		return map;
	}

	public final void load(final Map<String, String> values, final DependencyResolver resolver) {
		baseURI = values.get("baseURI");
		userName = values.get("userName");
		password = values.get("password");
	}

	@Override
	public final int getPriority() {
		return 0;
	}
}
