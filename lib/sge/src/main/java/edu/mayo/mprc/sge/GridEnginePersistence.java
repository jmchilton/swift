package edu.mayo.mprc.sge;

public interface GridEnginePersistence {
	/**
	 * Saves SGE job data information, such as standard out log file and error log file, into persistence storage.
	 *
	 * @param id
	 * @param outputLogFilePath
	 * @param errorLogFilePath
	 */
	void storeGridJobData(String id, String outputLogFilePath, String errorLogFilePath);
}
