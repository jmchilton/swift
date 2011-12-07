package edu.mayo.mprc.sequest.core;


import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * this is used to provide a stub for testing of the Mgf2SequestCaller class without having a
 * sequest executable available.
 */
class Mgf2SequestCallerStubbed implements Mgf2SequestInterface {

	private static final Logger LOGGER = Logger.getLogger(Mgf2SequestCallerStubbed.class);
	private String sequestExe;
	private File hostsFile;
	private int maxCommandLineLength;

	public void callSequest(File tarFile, File paramsFile, File mgfFile, long startTimeOut, long watchDogTimeOut, File hdrFile) {

		// validate that mgf file exists
		boolean havemgf = mgfFile.isFile();
		if (!havemgf) {
			throw new MprcException(mgfFile.getAbsolutePath() + " not found");
		}

		// validate that the params file exists
		boolean haveparams = paramsFile.isFile();
		if (!haveparams) {
			throw new MprcException(paramsFile.getAbsolutePath() + " not found");
		}

		File outputDir = tarFile.getParentFile();

		if (!outputDir.isDirectory()) {
			throw new MprcException("Output directory " + outputDir + " not found");
		}

		// create a temporary folder for the dta files
		// and .out files
		File tempfolder = FileUtilities.createTempFolder();
		String tempFolderName = tempfolder.getAbsolutePath();

		int maxCommandLength = this.maxCommandLineLength;
		if (maxCommandLength == 0) {
			maxCommandLength = 100;
		}

		SequestSubmitterInterface s = new SequestSubmit(100, paramsFile, outputDir, new File(outputDir, "mytar.tar"), hostsFile);

		SequestRunnerStub sc = new SequestRunnerStub(tempfolder, null, new ArrayList<String>(), hostsFile);

		sc.setSequestExe(this.sequestExe);

		s.setSequestCaller(sc);
		sc.setWatchDogTimeOut(watchDogTimeOut);
		sc.setStartTimeOut(startTimeOut);


		IonsModellerInterface i = new MgfIonsModeller();
		i.setWorkingDir(tempFolderName);


		MgfToDtaFileParser parser = new MgfToDtaFileParser(s, i, tempFolderName);

		parser.setMgfFileName(mgfFile.getAbsolutePath());

		//InputStream is = new InputStream(r)
		try {
			BufferedReader br = new BufferedReader(new FileReader(mgfFile));
			LOGGER.debug("starting to process mgf");
			parser.getDTAsFromFile(br);
			FileUtilities.closeQuietly(br);

		} catch (Exception t) {
			LOGGER.error("parser failed", t);
			throw new MprcException(t);
		}

	}

	public void setSequestExe(String sequestexe) {
		this.sequestExe = sequestexe;
	}

	public void setHostsFile(File hostsFile) {
		this.hostsFile = hostsFile;
	}

	public void setMaxCommandLineLength(int commandlinelength) {
		this.maxCommandLineLength = commandlinelength;
	}

}



