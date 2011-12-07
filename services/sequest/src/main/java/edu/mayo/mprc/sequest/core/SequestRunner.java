package edu.mayo.mprc.sequest.core;


import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.LogMonitor;
import edu.mayo.mprc.utilities.ProcessCaller;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class SequestRunner implements Runnable, SequestCallerInterface {

	private static final Logger LOGGER = Logger.getLogger(SequestRunner.class);
	private File paramsFile;
	private List<String> sequestDtaFiles;
	private static final String SEQUEST_EXE = "/usr/local/bin/sequest27_master";
	private static final String SEQUEST_OPTIONS = "-P";
	public static final String NO_DTA_FILES_PASSED = "No dta files were passed to sequest caller.";
	public static final String NO_PARAMS_FILE_PASSED = "No sequest params file was passed to the sequest caller";


	//static final String PVM_DAEMON = '/usr/share/pvm3/lib/LINUX64/pvmd3'
	private static final String PVM_DAEMON = "pvmd3";


	/**
	 * use this to override the sequest exe (can include path)
	 */
	protected String sequestExe;

	/**
	 * sequest search folder. This is where the final results of the search will be placed
	 */
	private String searchResultsFolder;

	/**
	 * pvm.hosts file location.
	 */
	private File hostsFile;

	private File workingDir;

	private String command;
	private List<String> args;
	private long watchDogTimeOut;
	private long startTimeOut;


	/**
	 * This is used to call the sequest executable
	 *
	 * @param workingdir      - <p>the folder where the sequest executable will do its work reading dta files
	 *                        and writing .out files, and the sequest.log<p>
	 * @param paramsFile      - the params file for sequest input parameters
	 * @param sequestDtaFiles - the list of '.dta' files for this call to the sequest executable
	 * @param hostsFile       - pvm.hosts file location. Needed for checking whether pvm operates ok.
	 */
	public SequestRunner(File workingdir, File paramsFile, List<String> sequestDtaFiles, File hostsFile) {
		setWorkingDir(workingdir);
		this.paramsFile = paramsFile;
		this.sequestDtaFiles = sequestDtaFiles;
		this.hostsFile = hostsFile;

		List<String> newArgs = new ArrayList<String>();

		String paramsPath = this.paramsFile == null ? null : this.paramsFile.getAbsolutePath();

		newArgs.add(SEQUEST_OPTIONS + paramsPath);
		//sequestDtaFiles.each {newargs.add((String) it)}

		for (String dta : sequestDtaFiles) {
			String fileName = new File(dta).getName();
			newArgs.add(fileName);
		}

		LOGGER.debug("sequest caller processing " + sequestDtaFiles.size() + " dta files");

		this.setArgs(newArgs);
	}

	public File getWorkingDir() {
		return workingDir;
	}

	@Override
	public long getWatchDogTimeOut() {
		return watchDogTimeOut;
	}

	@Override
	public void setWatchDogTimeOut(long timeOut) {
		watchDogTimeOut = timeOut;
	}

	@Override
	public long getStartTimeOut() {
		return startTimeOut;
	}

	@Override
	public void setStartTimeOut(long timeOut) {
		startTimeOut = timeOut;
	}

	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	public List<String> getArgs() {
		return args;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getSequestExe() {
		return this.sequestExe;
	}


	public SequestCallerInterface createInstance(File workingdir, File paramsFile, List<String> sequestDtaFiles, File hostsFile) {
		SequestRunner runner = new SequestRunner(workingdir, paramsFile, sequestDtaFiles, this.hostsFile);
		runner.setWatchDogTimeOut(this.getWatchDogTimeOut());
		runner.setStartTimeOut(this.getStartTimeOut());
		if (this.getCommand() != null) {
			runner.setCommand(getCommand());
		}
		if (this.sequestExe != null) {
			runner.sequestExe = this.sequestExe;
		}
		if (this.searchResultsFolder != null) {
			runner.searchResultsFolder = this.searchResultsFolder;
		}
		return runner;
	}

	public void setSequestExe(String sequestExe) {
		this.sequestExe = sequestExe;
		this.setCommand(sequestExe);
	}

	public String getSearchResultsFolder() {
		return this.searchResultsFolder;
	}

	public void setSearchResultsFolder(String folder) {
		this.searchResultsFolder = folder;
	}

	// TODO : need to replace with getCommand and getParameters

	public String getCall() {
		if (sequestDtaFiles == null || sequestDtaFiles.size() == 0) {
			throw new MprcException(NO_DTA_FILES_PASSED);
		}
		if (paramsFile == null || paramsFile.length() == 0) {
			throw new MprcException(NO_PARAMS_FILE_PASSED);
		}
		// use the args and cmd
		StringBuilder cmdString = new StringBuilder();
		cmdString.append(this.getCommand());
		cmdString.append(" ");
		for (String arg : getArgs()) {
			cmdString.append(arg);
			cmdString.append(" ");
		}

		return cmdString.toString();
	}

	public void setParamsFile(File paramsFile) {
		this.paramsFile = paramsFile;
	}

	public File getParamsFile() {
		return this.paramsFile;
	}

	/**
	 * creates the ProcessBuilder object used to run the process
	 *
	 * @return a ProcessBuilder object
	 */
	private ProcessBuilder createProcess() {

		List<String> cmd = new ArrayList<String>();
		String theCmd = getCommand();

		//Process p = R.exec(thecall)
		cmd.add(theCmd.trim());
		for (String arg : args) {
			cmd.add(arg.trim());
		}
		ProcessBuilder pb = new ProcessBuilder(cmd);
		if (workingDir != null) {
			pb.directory(workingDir);
		}

		// get the environment
		Map<String, String> env = System.getenv();
		Map<String, String> pbenv = pb.environment();
		pbenv.putAll(env);

		return pb;
	}


	/**
	 * used to make the call to sequest
	 * Note : grabs the standard out and standard error logs so immediately respond to changes in run
	 */
	public void run() {

		try {
			pvmUp();
		} catch (Exception t) {
			throw new MprcException("failure in validating pvm", t);
		}

		if (getCommand() == null) {
			if (this.sequestExe == null) {
				setCommand(SEQUEST_EXE);
			} else {
				setCommand(this.sequestExe);
			}
		}

		final ProcessBuilder builder = createProcess();
		final ProcessCaller caller = new ProcessCaller(builder);
		// Sequest will get killed after the given timeout unless we do something
		caller.setKillTimeout(getStartTimeOut());
		final SequestLogMonitor outputMonitor = new SequestLogMonitor(caller);
		caller.setOutputMonitor(outputMonitor);
		try {
			caller.run();
		} catch (Exception e) {
			LOGGER.error(caller.getFailedCallDescription(), e);
			throw new MprcException("Sequest call failed", e);
		}
		if (outputMonitor.getErrorDescription() != null) {
			throw new MprcException("Sequest call failed: " + outputMonitor.getErrorDescription());
		}
	}

	/**
	 * check that pvm is up. It attempt to restart pvm if it is down.
	 * The mehod is synchronized so that multiple instances will not attempt simultaneous restarts
	 */
	public synchronized void pvmUp() {
		if (FileUtilities.isWindowsPlatform()) {
			LOGGER.warn("Sequest does not support PVM on Windows");
			return;
		}
		assert hostsFile != null : "Path to pvm_hosts file is not set";
		String userName = System.getProperties().getProperty("user.name");
		LOGGER.debug("validating pvm for user [" + userName + "]");
		PvmUtilities pvm = new PvmUtilities();
		pvm.makeSurePVMOk(userName, hostsFile.getAbsolutePath(), PVM_DAEMON, "/tmp");
	}

	private class SequestLogMonitor implements LogMonitor {
		private String errorDescription;
		private final ProcessCaller caller;

		public SequestLogMonitor(ProcessCaller caller) {
			this.caller = caller;
		}

		@Override
		public void line(String line) {
			if (line.contains("SEQUEST can't open the specified database")) {
				// No database specified. Capture this special error
				// And kill sequest in a second if it does not quit itself
				caller.setKillTimeout(1000);
				errorDescription = "Sequest cannot open the specified database";
			}
			// Sequest produced a line of output.
			// Prolong the inactivity timeout
			caller.setKillTimeout(getWatchDogTimeOut());
		}

		public String getErrorDescription() {
			return errorDescription;
		}
	}
}
