package edu.mayo.mprc.utilities;

/**
 * Monitors output of a process ran by {@link ProcessCaller}.
 */
public interface LogMonitor {
	void line(String line);
}
