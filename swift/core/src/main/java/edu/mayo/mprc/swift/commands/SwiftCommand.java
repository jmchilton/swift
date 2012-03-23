package edu.mayo.mprc.swift.commands;

/**
 * @author Roman Zenka
 */
public interface SwiftCommand {
	/**
	 * @return Name of the command (what user enters on the command line).
	 */
	String getName();

	/**
	 * @return A longer description of the command.
	 */
	String getDescription();

	/**
	 * Executes the command within Swift's environment.
	 *
	 * @param environment Swift environment to run the command within.
	 */
	void run(SwiftEnvironment environment);
}
