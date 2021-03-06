package edu.mayo.mprc.sequest.core;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.tar.TarReader;
import edu.mayo.mprc.tar.TarWriter;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.GZipUtilities;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is reponsible for packaging dtas to send to sequest
 */
final class SequestSubmit implements SequestSubmitterInterface {

	private static final Logger LOGGER = Logger.getLogger(SequestSubmit.class);

	/**
	 * list of dta files to pass to sequest
	 */
	private List<String> sequestDtaFiles;

	/**
	 * the accumulated length of filenames in segment
	 */
	private int accumulatedLength;


	/**
	 * max command line length
	 */
	private int maxLineLength;


	/**
	 * the ions section count always goes up
	 */
	private int n;

	/**
	 * the search params for sequest
	 */
	private File paramsFile;

	/**
	 * result output directory
	 */
	private File outputDir;

	/**
	 * The tar file.
	 */
	private File tarFile;
	/**
	 * Exception
	 */
	private Throwable exceptionThrown;

	private SequestCallerInterface sequestCaller;

	private int submitCount;

	private File hostsFile;

	private static final String SEQUEST_LOG = "sequest.log";

	private long creationTime;

	public SequestSubmit(final long maxLineLength, File paramsFile, File workingDir, File tarFile, File hostsFile) {
		// this needs to be grabbed from the system
		this.maxLineLength = (int) maxLineLength;
		this.paramsFile = paramsFile;
		sequestDtaFiles = new ArrayList<String>();
		outputDir = workingDir;
		this.tarFile = tarFile;
		this.hostsFile = hostsFile;
		creationTime = new Date().getTime();
	}


	/**
	 * add a dta file for submission
	 */
	public void addDtaFile(String fileName, boolean forced) {
		if (n == 0) {
			this.creationTime = new Date().getTime();
		}
		this.n++;
		this.sequestDtaFiles.add(fileName);
		this.accumulatedLength = this.accumulatedLength + ((new File(fileName)).getName()).length() + 1;
		if (this.accumulatedLength >= maxLineLength || forced) {
			if (forced) {
				LOGGER.debug("forced set");
			}

			submitFilesToSequest();
			this.creationTime = new Date().getTime();
		}
	}

	private boolean haveSequestDtaFiles() {
		return (sequestDtaFiles != null && sequestDtaFiles.size() > 0);
	}

	/**
	 * force cleanup by forcing a submit to sequest of all file in the queue
	 */
	public void forceSubmit() {
		if (haveSequestDtaFiles()) {
			submitFilesToSequest();
			this.creationTime = new Date().getTime();
		}

		// do the cleanup
		// now create the zip file
		File zipFile = new File(tarFile + ".gz");
		Date startZip = new Date();
		FileUtilities.quietDelete(zipFile);
		try {
			GZipUtilities.compressFile(this.tarFile, zipFile);
		} catch (IOException e) {
			throw new MprcException("gzip failed for " + this.tarFile, e);
		}
		Date endZip = new Date();
		long zipTime = endZip.getTime() - startZip.getTime();
		LOGGER.debug("ziptime = " + zipTime);

		// remove the tar file as no longer needed
		FileUtilities.quietDelete(this.tarFile);
	}

	private List<String> getSequestDtaFiles() {
		return this.sequestDtaFiles;
	}

	/**
	 * see if can submit to sequest
	 */
	private void submitFilesToSequest() {

		LOGGER.debug("start submitting batch of files to sequest, after " + (new Date().getTime() - creationTime) + " ms of preprocessing");

		this.submitCount++;
		// see if a sequest.log exists
		File sequestLog = new File(outputDir, SEQUEST_LOG);
		if (sequestLog.exists()) {
			// move the file to a name incremented one
			FileUtilities.copyFile(sequestLog, new File(outputDir, sequestLog.getName() + "." + submitCount), true);
		}


		boolean lastFileNotProcessed = (this.accumulatedLength > this.maxLineLength);

		// make a new list with one less file to submit to sequest
		List<String> dtaFiles = new ArrayList<String>(this.getSequestDtaFiles());
		if (lastFileNotProcessed) {
			dtaFiles.remove(dtaFiles.size() - 1);
		}

		// recheck the length
		long length = 0;
		for (String dtaFile : dtaFiles) {
			File file = new File(dtaFile);
			length += (file.getName()).length() + 1;
		}
		if (length > this.accumulatedLength) {
			throw new MprcException("sequest dta file combined length = " + length + ", exceeds maxlength=" + maxLineLength);
		}

		SequestRunner sequestRunner = null;
		// make the call to sequest
		if (sequestCaller == null) {
			sequestCaller = new SequestRunner(this.outputDir, this.paramsFile, dtaFiles, hostsFile);
			sequestRunner = (SequestRunner) sequestCaller;
		} else {
			sequestRunner = (SequestRunner) sequestCaller.createInstance(sequestCaller.getWorkingDir(), this.paramsFile, dtaFiles, hostsFile);
			sequestRunner.setStartTimeOut(sequestCaller.getStartTimeOut());
			sequestRunner.setWatchDogTimeOut(sequestCaller.getWatchDogTimeOut());
			sequestRunner.setSearchResultsFolder(sequestCaller.getSearchResultsFolder());
		}

		// blocking call for now

		Thread t = new Thread(sequestRunner);
		t.setUncaughtExceptionHandler(new ProcessExceptionCatcher(this));
		Date startSearch = new Date();
		t.start();

		try {
			t.join();
		} catch (InterruptedException ignore) {
			// SWALLOWED Does not affect the main flow
		}
		Date endSearch = new Date();
		long searchTime = endSearch.getTime() - startSearch.getTime();
		LOGGER.debug("searchtime = " + searchTime);


		if (exceptionThrown != null) {
			throw new MprcException(exceptionThrown);
		}

		// now the tar
		LOGGER.debug("tar file name=" + tarFile);
		TarWriter tt = null;
		try {
			tt = new TarWriter(this.tarFile);
			// .out and .dta files are in the working  dir for sequest
			List<String> DtaToTar = new ArrayList<String>();
			List<String> sequestDtaSnapshot = new ArrayList<String>(this.getSequestDtaFiles());
			for (String aSequestDtaSnapshot : sequestDtaSnapshot) {
				File sequestDtaSnapshotFile = new File(aSequestDtaSnapshot);
				DtaToTar.add((new File(sequestRunner.getWorkingDir(), sequestDtaSnapshotFile.getName())).getAbsolutePath());
			}
			if (lastFileNotProcessed) {
				DtaToTar.remove(DtaToTar.size() - 1);
			}
			// need to tar these files and the corresponding .out files
			Date startTar = new Date();
			Dta2TarWriter dtaWriter = new Dta2TarWriter();
			dtaWriter.writeDtaFilesToTar(DtaToTar, this.outputDir, tt);
			Date endTar = new Date();
			long tarTime = endTar.getTime() - startTar.getTime();
			LOGGER.debug("tartime = " + tarTime);
		} finally {
			if (tt != null) {
				try {
					tt.close();
				} catch (Exception e) {
					cleanTarOnFailure(tt.getTarFile(), e);
				}
			}

			// validate the tar file, if it is corrupted then delete it and throw an exception
			this.validateTarFile(this.tarFile);
		}

		LOGGER.debug("tar file = " + tt.getTarFile() + " has " + TarReader.readNumberHeaders(tt.getTarFile()) + " headers");

		// then remove the files
		int tn = this.sequestDtaFiles.size();
		String last = this.sequestDtaFiles.get(tn - 1);
		this.sequestDtaFiles = new ArrayList<String>();
		if (lastFileNotProcessed) {
			this.sequestDtaFiles.add(last);
			this.accumulatedLength = last.length();
		} else {
			this.accumulatedLength = 0;
		}

	}


	class ProcessExceptionCatcher implements Thread.UncaughtExceptionHandler {
		private SequestSubmitterInterface submitter;

		public ProcessExceptionCatcher(SequestSubmitterInterface sequestSubmitter) {
			submitter = sequestSubmitter;
		}

		public void uncaughtException(Thread t, Throwable e) {
			submitter.setExceptionThrown(e);
			throw new MprcException(e);
		}
	}

	/**
	 * This will throw an exception if the tar file is not readable
	 *
	 * @param tarFile - the tar file
	 */
	void validateTarFile(File tarFile) {

		try {
			TarReader.readNumberHeaders(tarFile);
		} catch (Exception t) {
			cleanTarOnFailure(tarFile, t);
		}
	}

	/**
	 * if the tar file was corrupted an exception would have been triggered reading it
	 * rename it so it will not block the creation of a new tar file
	 *
	 * @param tarFile - the tar file name
	 * @param t       - the exception
	 */
	private void cleanTarOnFailure(File tarFile, Throwable t) {
		String newName = FileUtilities.stripExtension(tarFile.getAbsolutePath()) + "_tar_backup";
		if (tarFile.exists()) {
			FileUtilities.rename(tarFile, new File(newName));
		}
		throw new MprcException("tar file=" + tarFile.getAbsolutePath() + " is corrupted, renamed to " + newName, t);
	}

	public int getHowManyFiles() {
		return this.sequestDtaFiles.size();
	}

	public void setExceptionThrown(Throwable exceptionThrown) {
		this.exceptionThrown = exceptionThrown;
	}

	public SequestCallerInterface getSequestCaller() {
		return sequestCaller;
	}

	public void setSequestCaller(SequestCallerInterface sequestCaller) {
		this.sequestCaller = sequestCaller;
	}

	public int getMaxLineLength() {
		return this.maxLineLength;
	}

	public void setMaxLineLength(int maxLineLength) {
		this.maxLineLength = maxLineLength;
	}

	public int getAccumulatedLength() {
		return this.accumulatedLength;
	}

}
