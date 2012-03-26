package edu.mayo.mprc.swift.dbmapping;

import com.google.common.base.Objects;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.database.PersistableBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A set of enabled search engineConfigs. Once saved becomes immutable.
 */
public class EnabledEngines extends PersistableBase {
	private Set<SearchEngineConfig> engineConfigs = new HashSet<SearchEngineConfig>();

	public EnabledEngines() {
	}

	public Set<SearchEngineConfig> getEngineConfigs() {
		return engineConfigs;
	}

	void setEngineConfigs(final Set<SearchEngineConfig> engineConfigs) {
		this.engineConfigs = engineConfigs;
	}

	public void add(final SearchEngineConfig engineConfig) {
		if (this.getId() != null) {
			throw new MprcException("Enabled engine set is immutable once saved to the database");
		}
		engineConfigs.add(engineConfig);
	}

	public boolean isEnabled(final SearchEngineConfig config) {
		return engineConfigs.contains(config);
	}

	public boolean isEnabled(final String code) {
		for (final SearchEngineConfig config : engineConfigs) {
			if (config.getCode().equals(code)) {
				return true;
			}
		}
		return false;
	}

	public List<String> toEngineCodeList() {
		final List<String> result = new ArrayList<String>(getEngineConfigs().size());
		for (final SearchEngineConfig config : getEngineConfigs()) {
			result.add(config.getCode());
		}
		return result;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof EnabledEngines)) {
			return false;
		}

		final EnabledEngines that = (EnabledEngines) o;
		return Objects.equal(that.getEngineConfigs(), this.getEngineConfigs());
	}

	@Override
	public int hashCode() {
		return getEngineConfigs() != null ? getEngineConfigs().hashCode() : 0;
	}
}
