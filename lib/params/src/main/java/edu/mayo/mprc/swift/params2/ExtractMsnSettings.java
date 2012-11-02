package edu.mayo.mprc.swift.params2;

import edu.mayo.mprc.database.PersistableBase;

/**
 * Settings for raw->mgf convertor.
 */
public class ExtractMsnSettings extends PersistableBase {

	private String commandLineSwitches;
	private String command; // extract_msn or msconvert?

	public static final String EXTRACT_MSN = "extract_msn";
	public static final ExtractMsnSettings DEFAULT = new ExtractMsnSettings("-Z -V -MP100.00 -F1 -L20000 -EA100 -S1 -I10 -G1", EXTRACT_MSN);

	public ExtractMsnSettings() {
	}

	public ExtractMsnSettings(final String commandLineSwitches, final String command) {
		this.commandLineSwitches = commandLineSwitches;
		this.command = command;
	}

	public String getCommandLineSwitches() {
		return commandLineSwitches;
	}

	public void setCommandLineSwitches(final String commandLineSwitches) {
		this.commandLineSwitches = commandLineSwitches;
	}

	public String getCommand() {
		return command == null ? EXTRACT_MSN : command;
	}

	public void setCommand(String command) {
		this.command = command == null ? EXTRACT_MSN : command;
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
		if (getCommand() != null ? !getCommand().equals(that.getCommand()) : that.getCommand() != null) {
			return false;
		}


		return true;
	}

	@Override
	public int hashCode() {
		int result = commandLineSwitches != null ? commandLineSwitches.hashCode() : 0;
		result = 31 * result + (command != null ? command.hashCode() : 0);
		return result;
	}

	public ExtractMsnSettings copy() {
		final ExtractMsnSettings msnSettings = new ExtractMsnSettings(this.getCommandLineSwitches(), this.getCommand());
		msnSettings.setId(this.getId());
		return msnSettings;
	}
}
