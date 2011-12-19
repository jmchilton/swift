package edu.mayo.mprc.sequest.core;

import com.google.common.base.Joiner;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.CollectingLogMonitor;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ProcessCaller;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


final class PvmUtilities {
	private static final Logger LOGGER = Logger.getLogger(PvmUtilities.class);
	/**
	 * daemon for sequest slave
	 */
	private static final String SEQUEST_SLAVE = "sequest27_slave";

	/**
	 * daemon for sequest master
	 */
	private static final String SEQUEST_MASTER = "sequest27_master";

	/**
	 * how many bytes to log from the tail of the pvm log file
	 */
	public static final int TEMP_FILE_TAIL_TO_LOG = 100;

	private static final Pattern HOST_NAME_PATTERN = Pattern.compile("^\\w\\S*\\b");
	private static final int PVM_RESTART_TIMEOUT = 60 * 1000;


	/**
	 * find the hostnames in the file
	 *
	 * @param pvmHostFileName - the pvmhosts file, contains names of the slaves
	 */
	public static List<String> getSlaveNodes(String pvmHostFileName) {
		List<String> hosts = new ArrayList<String>();
		File f = new File(pvmHostFileName);
		if (!f.exists()) {
			throw new MprcException("file does not exist : " + pvmHostFileName);
		}
		List<String> lines = FileUtilities.readLines(f);
		for (String line : lines) {

			if (line == null) {
				continue;
			}
			line = line.trim();
			if (line.equals("")) {
				continue;
			}
			if (line.charAt(0) == '#') {
				continue;
			}
			Matcher m = HOST_NAME_PATTERN.matcher(line);
			if (m.find()) {
				hosts.add(m.group(0));
			}
		}
		return hosts;
	}


	/**
	 * look up the pvmhosts filenmae,
	 * then grab the node names
	 */
	public static List<String> getSlaveNodes(File hostsFile) {
		return getSlaveNodes(hostsFile.getAbsolutePath());
	}


	private static final Pattern PROCESS_ID_PATTERN = Pattern.compile("^\\w(\\S|\\d)*\\s*(\\d+)\\s.*");

	/**
	 * find the pvm process id in an output from 'ps -auwx | grep username'
	 * expected format of line is
	 * <username> <pid> .... <exe>
	 *
	 * @param lines  - the result from ssh
	 * @param pvmExe - the pvm command
	 */
	public static long getProcessID(List<String> lines, String pvmExe) {
		long id = 0;
		// find the line that contains pvmd
		for (String line : lines) {
			if (line.contains(pvmExe)) {
				id = parseProcessId(line, pvmExe);
			}
		}
		return id;
	}

	private static long parseProcessId(String line, String exe) {
		long id = 0;
		if (line.contains(exe)) {
			Matcher match = PROCESS_ID_PATTERN.matcher(line);
			if (match.matches()) {
				String pid = match.group(2);
				id = new Long(pid);
			}
		}
		return id;
	}


	/**
	 * find the pvm process id in an output from 'ps -auwx | grep username'
	 * expected format of line is
	 * <username> <pid> .... <exe>
	 *
	 * @param lines - the result from ssh
	 * @param exe   - the pvm command
	 */
	public static List<Long> getProcessIDs(List<String> lines, String exe) {
		long id = 0;
		List<Long> ids;
		ids = new ArrayList<Long>();
		// find the line that contains pvmd
		for (String line : lines) {

			if (line.contains(exe)) {
				id = parseProcessId(line, exe);
				if (id != 0) {
					ids.add(id);
				}
			}
		}

		return ids;
	}


	/**
	 * call ssh and return the output
	 *
	 * @param hostName - are calling  to
	 * @param call     - the call to pass to ssh
	 *                 Note : essentially are constructing <ssh> <hostname> <call>
	 */
	public static List<String> getSshResult(String hostName, String call) {
		List<String> args = new ArrayList<String>();
		args.add("ssh");
		args.add(hostName);
		args.add(call);

		return getCmdResult(args);


	}

	/**
	 * @param cmd - command to run
	 */
	public static List<String> getCmdResult(List<String> cmd) {
		ProcessBuilder builder = new ProcessBuilder(cmd);
		ProcessCaller caller = new ProcessCaller(builder);
		final CollectingLogMonitor outputMonitor = new CollectingLogMonitor();
		caller.setOutputMonitor(outputMonitor);
		caller.run();
		return outputMonitor.getLines();
	}

	private static final Pattern PVM_PATTERN = Pattern.compile("pvm[dl]\\.\\d+");

	/**
	 * lookup the pvm temporary file names within the file system
	 *
	 * @param hostName - host to look on
	 * @param folder   - the folder to look in
	 * @param userName - for this user
	 * @return the names of temporary files for pvm that were found
	 */
	public static List<String> getPvmTempFileNamesviaSsh(String hostName, String folder, String userName) {
		List<String> tempFiles = null;
		List<String> fileNames = new ArrayList<String>();
		try {
			tempFiles = getSshResult(hostName, "ls -AlL " + folder + " | grep " + userName);
		} catch (Exception t) {
			if (t.getMessage().contains("not found") || t.getMessage().contains("No such")) {
				// ignore it
				LOGGER.debug("failure getting temporary file names", t);
				return tempFiles;
			}
		}
		return parsePvmFileNames(fileNames, tempFiles, userName);
	}

	private static List<String> parsePvmFileNames(List<String> fileNames, List<String> tempFiles, String userName) {

		if (tempFiles == null) {
			return tempFiles;
		}
		getFileNamesForUser(fileNames, tempFiles, userName);

		List<String> pvmTempFiles = new ArrayList<String>();
		// from these only want those that have pattern 'pvm[d,l].\d+'
		getPvmFileNames(fileNames, pvmTempFiles);

		return pvmTempFiles;
	}

	private static void getFileNamesForUser(List<String> fileNames, List<String> tempFiles, String userName) {

		if (tempFiles != null) {
			for (String line : tempFiles) {
				// the user name must be in a separate field
				Pattern p = Pattern.compile("\\b" + userName + "\\b");
				Matcher matches = p.matcher(line);
				if (!matches.find()) {
					continue;
				}
				String[] pieces = line.split(" ");
				fileNames.add(pieces[pieces.length - 1]);

			}
		}
	}

	private static void getPvmFileNames(List<String> fileNames, List<String> pvmTempFiles) {
		for (String fileName : fileNames) {
			Matcher m = PVM_PATTERN.matcher(fileName);
			if (m.matches()) {
				pvmTempFiles.add(fileName);
			}
		}
	}

	// get pvm temp files, local

	public static List<String> getPvmTempFileNames(String folder, String userName) {

		// this needs to use /bin/bash -c
		List<String> tempFiles = null;
		List<String> fileNames = new ArrayList<String>();

		try {
			List<String> args = Arrays.asList("ls", "-AlL", folder);
			List<String> grepArgs = Arrays.asList("grep", userName);
			tempFiles = processPipedCommand(args, grepArgs);
		} catch (Exception e) {
			// if no files an exception would be thrown  say 'not found'
			if (e.getMessage().contains("not found") || e.getMessage().contains("No such")) {
				// ignore it
				return tempFiles;
			}
		}
		return parsePvmFileNames(fileNames, tempFiles, userName);
	}

	private static void getProcessesForUser(List<String> processes, List<String> tempProcesses, String userName) {

		if (tempProcesses != null) {
			for (String line : tempProcesses) {
				// the user name must be in a separate field
				Pattern p = Pattern.compile("(\\b" + userName + "\\b)");
				Matcher matches = p.matcher(line);
				if (!matches.find()) {
					continue;
				}
				processes.add(line);

			}
		}
	}


	/**
	 * find the process id for a given hostname, exe
	 * and user
	 */
	public static long findProcessIDforExe(String hostName, String userName, String exe) {
		List<String> psResult;
		// find the pvm process id on the node

		psResult = getSshResult(hostName, "ps -auwx | grep " + userName);

		return parseProcessListforPid(psResult, exe, userName);
	}

	private static long parseProcessListforPid(List<String> psResult, String exe, String userName) {


		if (psResult == null) {
			return 0;
		}
		List<String> processes = new ArrayList<String>();
		getProcessesForUser(processes, psResult, userName);

		long pidSeq = 0;
		try {
			pidSeq = getProcessID(processes, exe);
		} catch (Exception ignore) {
			// SWALLOWED : do nothing since process may have already shutdown
		}
		return pidSeq;
	}

	/**
	 * find the process id for a given  exe
	 * and user
	 */
	public static long findProcessIDforExe(String userName, String exe) {
		List<String> psResult = listAllProcessesByUser(userName);
		// find the pvm process id on the node

		return parseProcessListforPid(psResult, exe, userName);
	}

	/**
	 * TODO: Do this without grep and pipe. ps can do it on its own.
	 * <p/>
	 * Equivalent to doing:
	 * <p/>
	 * {@code ps auwx | grep $username}
	 *
	 * @param userName Name of the user whose processes to list
	 * @return List of processes (in the ps format)
	 */
	private static List<String> listAllProcessesByUser(String userName) {
		List<String> psArgs = Arrays.asList("ps", "auwx");
		List<String> grepArgs = Arrays.asList("grep", userName);
		return processPipedCommand(psArgs, grepArgs);
	}

	/**
	 * is pvm daemon (pvmd) running
	 */
	public static long isPVMRunning(String userName) {
		return findProcessIDforExe(userName, "pvmd");

	}

	private static final Pattern PVM_NUM_HOSTS_PATTERN = Pattern.compile(".*(?:\\D|^)(\\d+)\\s+hosts?,\\s+\\d+\\s+data format");

	/**
	 * is pvm running with state matching configuration
	 */
	public static boolean isPVMRunningCorrectly(String userName, String hostFileName) {
		long pid = isPVMRunning(userName);
		if (pid == 0) {
			LOGGER.debug("pvmd3 daemon is not running");
			return false;
		}
		// now see if the running state matches the configuration
		// using echo conf | pvm
		List<String> psResult;

		try {
			List<String> echoArgs = Arrays.asList("echo", "conf");
			List<String> bashArgs = Arrays.asList("/bin/bash", "-c", "pvm");
			psResult = processPipedCommandwithPipe(echoArgs, bashArgs);
		} catch (Exception ignore) {
			//  will swallow the exception here as is a PVM down one
			// and the goal is to check if pvm down
			return false;

		}

		int numHosts = -1;
		for (String line : psResult) {
			Matcher k = PVM_NUM_HOSTS_PATTERN.matcher(line);

			if (k.find()) {
				numHosts = Integer.parseInt(k.group(1));
			}
		}
		if (numHosts == -1) {
			throw new MprcException("Can't determine number of hosts from pvm output: " + Joiner.on("\n").join(psResult));
		}
		// now see is numhosts same as  in hostfile
		List<String> slaves = getSlaveNodes(hostFileName);
		int numReqHosts = slaves.size() + 1;

		boolean running_properly = (numHosts == numReqHosts);

		if (running_properly) {
			return true;
		} else {
			LOGGER.debug("pvm is not running properly, numhosts found=" + numHosts + ", num required=" + numReqHosts);
			// it is a good idea to dump the pvm console full output, so can see which nodes are down
			LOGGER.debug("here is pvm console output, each line prefixed with '>>>' ");
			for (String line : psResult) {
				LOGGER.debug(">>> " + line);

			}
			return false;
		}
	}


	/**
	 * kill pvm on a slave node
	 * used when pvm went down accidentally
	 * assume
	 *
	 * @param hostName       - name of the host
	 * @param userName       - the username
	 * @param pvmdName       - the name of pvm daemon, usually 'pvmd'
	 * @param nodeTempFolder - the pvm temporary folder on the node, usually '/tmp'
	 * @param master         - indicates if master node, only use if master is to be ssh'ed to
	 */
	public static void killPVMonNode(String hostName, String userName, String pvmdName, String nodeTempFolder, boolean master) {
		List<String> psResult;
		// find the pvm process id on the node
		psResult = getSshResult(hostName, "ps -auwx | grep " + userName);
		List<String> processes = new ArrayList<String>();
		getProcessesForUser(processes, psResult, userName);
		// find the sequest27_ pid and kill it
		String daemon = SEQUEST_SLAVE;
		if (master) {
			daemon = SEQUEST_MASTER;
		}
		List<Long> pidseqs = getProcessIDs(processes, daemon);
		for (long pidseq : pidseqs) {

			if (pidseq != 0) {
				try {
					getSshResult(hostName, "kill -9 " + pidseq);
				} catch (Exception ignore) {
					// SWALLOWED do nothing since process may have already gone down
				}
			}
		}
		long pid = 0;
		try {
			pid = getProcessID(processes, pvmdName);
		} catch (Exception ignore) {
			// SWALLOWED do nothing since process may have already shutdown
		}
		if (pid != 0) {
			try {
				getSshResult(hostName, "kill -9 " + pid);
			} catch (Exception ignore) {
				// SWALLOWED do nothing since process may have already gone down
			}
		}
		// now find the temporary files for pvm
		List<String> tempFiles = null;
		try {
			tempFiles = getPvmTempFileNamesviaSsh(hostName, nodeTempFolder, userName);
		} catch (Exception t) {
			LOGGER.debug("exception for getPvmTempFileNamesviaSsh, hostname=" + hostName + "user=" + userName, t);
			return;
		}
		// and remove the tenporary files for pvm
		if (tempFiles == null || tempFiles.isEmpty()) {
			LOGGER.debug("no pvm temp files found on " + hostName + " for user=" + userName);
			return;
		}

		for (String fileName : tempFiles) {
			if (fileName != null && fileName.length() > 0) {
				deleteFileRemote(nodeTempFolder, fileName, hostName);
			}
		}
	}

	/**
	 * kill pvm on all slave nodes
	 *
	 * @param hostsFile      - the location of the hosts file for pvm
	 * @param userName       - the username to operate as
	 * @param pvmdName       - name of the pvm daemon, usually 'pvmd'
	 * @param nodeTempFolder - location of the temp folder for pvm on the slaves (usually '/tmp)
	 */
	public static void killPVMonSlaveNodes(String hostsFile, String userName, String pvmdName, String nodeTempFolder) {
		List<String> slaves = getSlaveNodes(hostsFile);
		// now do a clean of pvm on each slave
		for (String slave : slaves) {
			killPVMonNode(slave, userName, pvmdName, nodeTempFolder, false);
		}
	}

	/**
	 * kill pvm on master (not via ssh)
	 *
	 * @param userName       - the username to operate as
	 * @param pvmdName       - name of the pvm daemon, usually 'pvmd'
	 * @param nodeTempFolder - location of the temp folder for pvm on the slaves (usually '/tmp)
	 */
	public static void killPVMonMasterNode(String userName, String pvmdName, String nodeTempFolder) {
		List<String> psResult = listAllProcessesByUser(userName);

		List<String> processes = new ArrayList<String>();
		getProcessesForUser(processes, psResult, userName);
		// find the sequest27_ pid and kill it
		String daemon = SEQUEST_MASTER;

		killProcesses(processes, daemon);
		killProcesses(processes, pvmdName);

		// now find the temporary files for pvm
		List<String> tempFiles = getPvmTempFileNames(nodeTempFolder, userName);
		// and remove the tenporary files for pvm

		for (String filename : tempFiles) {

			deleteFile(nodeTempFolder, filename);
		}

	}

	private static void killProcesses(List<String> processes, String daemon) {
		List<Long> pidseqs = getProcessIDs(processes, daemon);
		for (long pidseq : pidseqs) {

			if (pidseq != 0) {
				try {
					List<String> args = Arrays.asList("kill", "-9", "" + pidseq);
					getCmdResult(args);
				} catch (Exception ignore) {
					// SWALLOWED : if kill fails ignore it as process may already be down
				}
			}
		}
	}

	public static void deleteFile(String nodeTempFolder, String fileName) {
		try {
			if (fileName.length() > 0) {
				File f = new File(new File(nodeTempFolder), fileName);
				if (f.exists()) {
					List<String> lines;

					lines = getLastLines(TEMP_FILE_TAIL_TO_LOG, f);
					//  log the last 10 lines of the pvm log file
					LOGGER.debug("here are the last lines of file=" + fileName + " prefixed with '>>'");
					for (String line : lines) {
						LOGGER.debug(">> " + line);
					}

					LOGGER.debug("deleting filename=" + fileName);

					FileUtilities.quietDelete(f);
				}
			}
		} catch (Exception ignore) {
			// SWALLOWED in case already deleted
		}
	}

	/**
	 *
	 */
	public static void deleteFileRemote(String nodeTempFolder, String fileName, String hostName) {
		if (fileName.length() > 0) {

			List<String> lines;

			try {
				lines = getLastLinesRemote(TEMP_FILE_TAIL_TO_LOG, new File(new File(nodeTempFolder), fileName).getAbsolutePath(), hostName);
				//  log the last 10 lines of the pvm log file
				LOGGER.debug("here are the last lines of file=" + fileName + " prefixed with '>>'");
				for (String line : lines) {
					LOGGER.debug(">> " + line);
				}
			} catch (Exception ignore) {
				// SWALLOWED in case file gone
			}


			LOGGER.debug("deleting filename=" + fileName + " on " + hostName);

			try {
				getSshResult(hostName, "rm " + new File(new File(nodeTempFolder), fileName).getAbsolutePath());
			} catch (Exception ignore) {
				// SWALLOWED : ignore it since it might already be gone
			}
		}
	}

	private static List<String> getLastLinesRemote(long howMany, String fileName, String hostName) {
		List<String> result = new ArrayList<String>();
		try {
			List<String> lines = getSshResult(hostName, "tail -" + howMany + " " + fileName);
			result.addAll(lines);
		} catch (Exception ignore) {
			// SWALLOWED, file might have been deleted by cleanup routines
		}
		return result;
	}

	/**
	 * get the last lines in the file
	 *
	 * @return the lines
	 * @howmany - how many bytes to grab, will be less if file does not have them
	 * @bytes - char array of at least size 'howmany'
	 */
	private static List<String> getLastLines(long howMany, File f) {
		// will navigate the log file to get the last block of bytes
		List<String> lines = new ArrayList<String>();

		String line = null;
		String fileName = null;

		try {
			if (!f.exists() || f.length() == 0) {
				return lines;
			}
			fileName = f.getName();
			RandomAccessFile r = new RandomAccessFile(f, "r");
			long length = r.length() - howMany - 1;

			r.seek(length > 0 ? length : 0);
			try {
				while (true) {
					line = r.readLine();
					if (line == null) {
						break;
					}
					lines.add(line);
				}
			} catch (IOException ioe) {
				throw new MprcException("failure reading the file=" + f.getName(), ioe);
			} finally {
				FileUtilities.closeQuietly(r);
			}

		} catch (Exception t) {
			/* SWALLLOWED: (for a reason)
										   This code is to help with reporting a pvm issue but is not part of the main flow
										   for restarting pvm. Hence don't want failure here to interrupt restart pvm process
										   This failure could be due to pvml file read permission being turned off or the file handle being stale
									   */
			LOGGER.debug("exception reading file=" + fileName, t);

		}

		return lines;

	}


	private static List<String> processPipedCommand(List<String> cmd1, List<String> cmd2) {
		String psIntermResult = processCommandtoString(cmd1);

		// write to a temporary file
		File folder = null;
		List<String> psResult = null;
		File f = null;
		try {
			folder = FileUtilities.createTempFolder();
			String modcommand = cmd1.get(0).replace(File.separatorChar, '_');
			f = File.createTempFile("pvm" + modcommand, "pvm", folder);
			FileUtilities.writeStringToFile(f, psIntermResult, true);

			List<String> moreArgs = new ArrayList<String>(cmd2);
			moreArgs.add(f.getAbsolutePath());
			psResult = getCmdResult(moreArgs);


		} catch (Exception t) {
			// this was being SWALLOWED in earlier release
			throw new MprcException(t);
		} finally {
			try {
				FileUtilities.quietDelete(f);
				FileUtilities.quietDelete(folder);
			} catch (Exception ignored) {
				// SWALLOWED, might already be deleted
			}
		}

		return psResult;
	}


	private static List<String> processPipedCommandwithPipe(List<String> cmd1, List<String> cmd2) {
		String interimResult = processCommandtoString(cmd1);

		BufferedInputStream input;

		List<String> psResult;
		input = new BufferedInputStream(new ByteArrayInputStream(interimResult.getBytes()));
		try {
			psResult = processCommandwithPipe(cmd2, input);
		} finally {
			FileUtilities.closeQuietly(input);
		}

		return psResult;
	}


	private static List<String> processCommandwithPipe(List<String> cmd, BufferedInputStream input) {
		ProcessBuilder builder = new ProcessBuilder(cmd);
		ProcessCaller caller = new ProcessCaller(builder);
		final CollectingLogMonitor outputMonitor = new CollectingLogMonitor();
		caller.setOutputMonitor(outputMonitor);
		caller.setInputStream(input);
		caller.run();
		return outputMonitor.getLines();
	}

	private static String processCommandtoString(List<String> cmd) {
		final List<String> cmdResult = getCmdResult(cmd);
		return Joiner.on('\n').join(cmdResult);
	}

	/**
	 *
	 */
	public static void restartPvm(String pvmdName, String hostFileName) {
		// run <pvmd /etc/pvmhosts> via bash shell
		List<String> result = null;
		try {
			List<String> args = Arrays.asList(
					"/bin/bash",
					"-c",
					"pvm " + hostFileName + " &");
			result = getCmdResult(args);
		} catch (Exception t) {
			LOGGER.debug("error ", t);
		}
		if (result != null) {
			LOGGER.debug(Joiner.on("\n").join(result));
		}
	}

	/**
	 * clean up and restart the pvm daemon
	 *
	 * @param userName
	 * @param hostFileName
	 * @param pvmdName
	 * @param nodeTempFolder
	 */
	private static void pvmRestart(String userName, String hostFileName, String pvmdName, String nodeTempFolder) {

		// kill pvm on master node
		killPVMonMasterNode(userName, pvmdName, nodeTempFolder);
		// kill pvm on the slave nodes
		killPVMonSlaveNodes(hostFileName, userName, pvmdName, nodeTempFolder);
		// now restart pvm
		restartPvm(pvmdName, hostFileName);
	}

	/**
	 * validate that pvm is ok. If not attempt to restart it
	 *
	 * @param userName       - name of the user
	 * @param hostFileName   - path to the host file
	 * @param pvmdName       - the executable name for pvm daemon <pvmd>
	 * @param nodeTempFolder - temporary folder where the pvm temp file appear
	 */
	public static void makeSurePVMOk(String userName, String hostFileName, String pvmdName, String nodeTempFolder) {
		try {
			if (isPVMRunningCorrectly(userName, hostFileName)) {
				// pvm running ok, so nothing to do
				LOGGER.debug("pvm running ok");
				return;
			}

			LOGGER.debug("pvm not running, attempting a restart");
			pvmRestart(userName, hostFileName, pvmdName, nodeTempFolder);
			// validate is running
			if (!isPVMRunningCorrectly(userName, hostFileName)) {
				LOGGER.debug("could not restart pvm, pause and see if it comes back");
				Thread.sleep(PVM_RESTART_TIMEOUT);
				pvmRestart(userName, hostFileName, pvmdName, nodeTempFolder);
				boolean ok_again = isPVMRunningCorrectly(userName, hostFileName);
				if (!ok_again) {
					LOGGER.debug("could not restart pvm");
					throw new MprcException("could not restart pvm automatically");
				}
			}
		} catch (Exception t) {
			throw new MprcException("could not restart pvm automatically", t);
		}
	}

}
