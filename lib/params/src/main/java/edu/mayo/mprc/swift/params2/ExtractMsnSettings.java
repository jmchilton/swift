package edu.mayo.mprc.swift.params2;

import edu.mayo.mprc.database.PersistableBase;

/**
 * Settings for extract_msn
 */
public class ExtractMsnSettings extends PersistableBase {

	private String commandLineSwitches;

	public static final ExtractMsnSettings DEFAULT = new ExtractMsnSettings("-Z -V -MP100.00 -F1 -L20000 -EA100 -S1 -I10 -G1");

	public ExtractMsnSettings() {
	}

	public ExtractMsnSettings(final String commandLineSwitches) {
		this.commandLineSwitches = commandLineSwitches;
	}

	public String getCommandLineSwitches() {
		return commandLineSwitches;
	}

	public void setCommandLineSwitches(final String commandLineSwitches) {
		this.commandLineSwitches = commandLineSwitches;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof ExtractMsnSettings)) {
			return false;
		}

		final ExtractMsnSettings that = (ExtractMsnSettings) o;

		if (getCommandLineSwitches() != null ? !getCommandLineSwitches().equals(that.getCommandLineSwitches()) : that.getCommandLineSwitches() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getCommandLineSwitches() != null ? getCommandLineSwitches().hashCode() : 0;
	}

	public ExtractMsnSettings copy() {
		final ExtractMsnSettings msnSettings = new ExtractMsnSettings(this.getCommandLineSwitches());
		msnSettings.setId(this.getId());
		return msnSettings;
	}
}
