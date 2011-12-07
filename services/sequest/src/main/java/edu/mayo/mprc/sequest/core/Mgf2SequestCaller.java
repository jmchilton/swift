package edu.mayo.mprc.sequest.core;

/**
 * This is the script uses to call sequest
 * as input it takes the following
 * <ul>
 * <li> output directory - tar file will end up here, with prefix from mgf filename and extension '.tar.gz'
 * </li>
 * <li> sequest param file name
 * </li>
 * <li> mgf file name
 * </li>
 * </ul>
 */

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.sequest.SequestDeploymentService;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.MaxCommandLine;
import edu.mayo.mprc.utilities.StreamRegExMatcher;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.getProperty;

public final class Mgf2SequestCaller implements Mgf2SequestInterface {

	private static final Logger LOGGER = Logger.getLogger(Mgf2SequestCaller.class);
	public static final int MAX_HEADER_FILE_LENGTH = SequestDeploymentService.MAX_SEQUEST_INDEX_LENGTH;

	private static final int MAX_CALL_LENGTH_PADDING = 1000;
	private static final int MIN_MAX_CALL_LENGTH_TO_PAD = 10 * 1000;

	private String sequestExe;
	/**
	 * location of the pvm hosts configuration file, usually /etc/pvmhosts
	 */
	private File hostsFile;

	/**
	 * the max command line length to start with
	 * this will be used to start the calculation
	 */
	private int maxCommandLineLength;

	/**
	 * last calculated max call length
	 */
	private static long lastMaxCommandLineLength;
	/**
	 * start the max call length search here
	 */
	private static final long MAX_CALL_LENGTH_SEED = 100 * 1000;

	static {
		resetMaxCallLength();

	}

	/**
	 * dynamically determine the maximum call length
	 *
	 * @return the sequest call length
	 */
	public static synchronized long resetMaxCallLength() {
		Date startTime = new Date();
		long maxCallLength = MaxCommandLine.findMaxCallLength(MAX_CALL_LENGTH_SEED, null);
		Date endTime = new Date();
		long runTime = endTime.getTime() - startTime.getTime();

		LOGGER.debug("time to find max call length =" + runTime);

		// allow some space for max call length
		if (maxCallLength > MIN_MAX_CALL_LENGTH_TO_PAD) {
			maxCallLength -= MAX_CALL_LENGTH_PADDING;
		}
		LOGGER.debug("max commandline length=" + maxCallLength);
		lastMaxCommandLineLength = maxCallLength;
		return lastMaxCommandLineLength;
	}

	public static synchronized long getMaxCallLength() {
		return lastMaxCommandLineLength;
	}

	/**
	 * Make the call to sequest
	 *
	 * @param tarFile         - name of the tar file, full path
	 * @param paramsFile      - params file name full path, this needs replacement done on the database name
	 * @param mgfFile         - mgf file name
	 * @param startTimeOut    - the start time out
	 * @param watchDogTimeOut - the watch dog time out
	 */
	public void callSequest(File tarFile, File paramsFile, File mgfFile, long startTimeOut, long watchDogTimeOut, File hdrFile) {
		assert hostsFile != null : "Path to pvm_hosts file is not set.";
		validateInputsToSequestCaller(mgfFile, paramsFile, hdrFile);

		// create a temporary folder for the dta files
		// and .out files
		File tempFolder = FileUtilities.createTempFolder();
		String tempFolderName = tempFolder.getAbsolutePath();

		File searchParamsFile = prepareParamsFile(paramsFile, hdrFile, tempFolderName);

		File outputDir = this.getOutputDir(tarFile);

		// find the max allowable command line length and reduce it by 1000
		if (this.maxCommandLineLength == 0) {
			this.maxCommandLineLength = (int) getMaxCallLength();
		}

		File tarFileName = prepareTarFileLocation(tarFile);
		SequestSubmitterInterface s = new SequestSubmit(maxCommandLineLength, searchParamsFile, outputDir, tarFileName, hostsFile);

		SequestRunner sc = new SequestRunner(tempFolder, paramsFile, new ArrayList<String>(), hostsFile);

		sc.setSequestExe(this.sequestExe);
		sc.setWatchDogTimeOut(watchDogTimeOut);
		sc.setStartTimeOut(startTimeOut);
		sc.setSearchResultsFolder(outputDir.getAbsolutePath());


		s.setSequestCaller(sc);


		IonsModellerInterface i = new MgfIonsModeller();

		i.setWorkingDir(tempFolderName);


		MgfToDtaFileParser parser = new MgfToDtaFileParser(s, i, tempFolderName);


		parser.setMgfFileName(mgfFile.getAbsolutePath());

		// set the sequest output directory so can shorten the full call
		BufferedReader br = null;
		BufferedInputStream b = null;
		try {
			b = new BufferedInputStream(new FileInputStream(mgfFile));
			br = new BufferedReader(new InputStreamReader(b));

			LOGGER.debug("starting to parse mgf for dta sections");

			parser.getDTAsFromFileWithBlockReads(br);
		} catch (Exception t) {
			throw new MprcException(t);
		} finally {
			FileUtilities.closeQuietly(br);
			FileUtilities.closeQuietly(b);
		}

	}

	/**
	 * get the location where the output files will be written
	 *
	 * @param tarFile - specified tar file name and location
	 * @return - output directory for sequest tar'd search result
	 */
	private File getOutputDir(File tarFile) {
		File outputDir = tarFile.getParentFile();

		if (!outputDir.isDirectory()) {
			throw new MprcException("Output directory " + outputDir.getAbsolutePath() + " not found");
		}
		return outputDir;

	}

	/**
	 * check if the input files needed by sequest exist
	 *
	 * @param mgfFile    - the mgf file name
	 * @param paramsFile - the params file name
	 */
	private void validateInputsToSequestCaller(File mgfFile, File paramsFile, File hdrFile) {

		// validate that mgf file exists
		boolean haveMgf = mgfFile.isFile();
		if (!haveMgf) {
			throw new MprcException(mgfFile + " not found");
		}

		// validate that the params file exists
		boolean haveParams = paramsFile.isFile();
		if (!haveParams) {
			throw new MprcException(paramsFile + " not found");
		}
		// validate that the hdr file exists
		boolean haveHdr = hdrFile.isFile();
		if (!haveHdr) {
			throw new MprcException(hdrFile + " not found");
		}
	}

	/**
	 * Prepare the params file that will be passed to sequest
	 *
	 * @param paramsFile     - the parameters file
	 * @param hdrFile        - the database header file
	 * @param tempFolderName - the temporary folder name
	 * @return the search parameters file
	 */
	private File prepareParamsFile(File paramsFile, File hdrFile, String tempFolderName) {
		// copy the params file to the temporary folder
		File tempParamsFile = new File(new File(tempFolderName), paramsFile.getName());

		FileUtilities.copyFile(paramsFile, tempParamsFile, true);

		return replaceDatabaseInParamFile(tempParamsFile, hdrFile);
	}

	/**
	 * The input tar file location
	 * If there is a gz extension on the tar file name it is stripped off
	 * If there is an existing tar file it is deleted
	 *
	 * @param tarFile - the tar file
	 * @return - the corrected tar file location
	 */
	private File prepareTarFileLocation(File tarFile) {

		// if there is a .gz in the name then strip it
		String tarFileName = tarFile.getAbsolutePath();
		// if contain .gz remove it
		String ext = FileUtilities.getExtension(tarFileName);
		if ("gz".equals(ext)) {
			tarFileName = FileUtilities.stripExtension(tarFileName);
		}
		// if tarFile already exists then delete it
		File existingTar = new File(tarFileName);
		if (existingTar.exists()) {
			FileUtilities.quietDelete(existingTar);
		}
		return existingTar;
	}

	public void setSequestExe(String sequestExe) {
		this.sequestExe = sequestExe;
	}

	public void setHostsFile(File hostsFile) {
		this.hostsFile = hostsFile;
	}

	public void setMaxCommandLineLength(int commandLineLength) {
		this.maxCommandLineLength = commandLineLength;
	}


	/**
	 * Checks if the length of the database hdr file name exceeds specifications for sequest.
	 * Exceeding this length could cause the sequest executable to hang
	 *
	 * @param hdrFile - the database hdr file
	 * @return
	 */
	public static boolean isDataBaseNametooLong(File hdrFile) {
		String hdrFileNameChecked = hdrFile.getName();
		// strip the extension
		String strippedDone = FileUtilities.stripExtension(hdrFileNameChecked);
		// there might be two extensions
		return (FileUtilities.stripExtension(strippedDone).length() > MAX_HEADER_FILE_LENGTH);
	}

	/**
	 * replace the database in the params file
	 *
	 * @param searchParamsFile - the search parameters file
	 * @param hdrFile          - the database hdr file
	 * @return - the corrected params file
	 */
	public static synchronized File replaceDatabaseInParamFile(File searchParamsFile, File hdrFile) {
		LOGGER.debug("searchParamsFile=" + searchParamsFile + ",  hdrFile=" + hdrFile);

		// there might be two extensions
		if (isDataBaseNametooLong(hdrFile)) {
			LOGGER.warn("Database name=" + hdrFile + " may be too long, exceeds " + MAX_HEADER_FILE_LENGTH + ", this may cause sequest to hang");
		}
		//  looking for ${DBPath:...} or ${DB:...}
		try {
			File result = new File(searchParamsFile.getAbsolutePath() + ".replaced");
			if (result.exists()) {
				FileUtilities.quietDelete(result);
			}
			//if(result.exists()){
			//    LOGGER.debug("file already exists="+result.getAbsolutePath());
			//    return result;
			//}
			StreamRegExMatcher replaceEngine = new StreamRegExMatcher(Pattern.compile("\\$\\{(DB|DBPath):[^}]+\\}"), searchParamsFile);
			replaceEngine.replaceAll(Matcher.quoteReplacement(hdrFile.getAbsolutePath()));

			replaceEngine.writeContentsToFile(result, true);
			replaceEngine.close();
			return result;
		} catch (IOException e) {
			// find the hostName
			String hostName = "unknown";
			InetAddress host;
			try {
				host = InetAddress.getLocalHost();
				hostName = host.getHostName();
			} catch (Exception ignore) {
				// ignore here since not the on the main path SWALLOWED
			}
			// find the userName
			String userName = getProperty("user.name");
			// and add these to the message
			throw new MprcException("At hostName=" + hostName + ",userName=" + userName + ", could not replace database in the " + searchParamsFile.getAbsolutePath(), e);
		}
	}
}

