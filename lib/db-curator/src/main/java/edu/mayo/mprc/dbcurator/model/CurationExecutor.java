package edu.mayo.mprc.dbcurator.model;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.fasta.DBInputStream;
import edu.mayo.mprc.fasta.DBOutputStream;
import edu.mayo.mprc.fasta.FASTAInputStream;
import edu.mayo.mprc.fasta.FASTAOutputStream;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.StringUtilities;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A CurationExecutor is a class class than handles the work behind executing a curation.
 * <p/>
 * It takes a Curation and goes though the steps necessary to complete the execution.  You can get a CurationStatus
 * object before calling execute() and when the execution is done then {@link CurationStatus#isInProgress()} will return false.
 * <p/>
 * Execution can complete successfully or it can fail and the {@link CurationStatus} is a way to report both the status of the
 * execution while it happens as well as report what had happened during execution.
 * <p/>
 * To call this method you must execute it as a thread and monitor the {@link CurationStatus#isInProgress()} method and wait
 * until it returns false to ensure the the curation process is complete.
 */
public final class CurationExecutor implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(CurationExecutor.class);

	/**
	 * the stream that is currently acting as an input file
	 */
	private DBInputStream inStream;

	/**
	 * the stream that is currently acting as an output file
	 */
	private DBOutputStream outStream;

	/**
	 * the curation that is being executed
	 */
	private Curation curation;

	/**
	 * determines if any side effects should be held over or if we are just testing and should not leave any traces
	 */
	private boolean retainArtifacts = true;

	/**
	 * the status object that we will want to use to communicate any issues we are having with the UI or other interface.
	 */
	private final MyCurationStatus status = new MyCurationStatus();

	/**
	 * the directory where we are storing all of the temporary files
	 */
	private File tempDirectory = null;

	private CurationDao curationDao;

	private File fastaFolder;

	private File localTempFolder;
	private File fastaArchiveFolder;


	/**
	 * Creates a new CurationExecutor given the curator you want to execute.  We also initialize the Status object that
	 * will be reported by this execution and pass in the indicator object (An AtomicInteger) since we want to be the
	 * only class that updates the proress.
	 *
	 * @param curation the curation that you want to execute
	 */
	public CurationExecutor(Curation curation, boolean retainArtifacts, CurationDao curationDao,
	                        File fastaFolder, File localTempFolder, File fastaArchiveFolder
	) {
		this.curation = curation;
		this.fastaFolder = fastaFolder;
		this.retainArtifacts = retainArtifacts;
		this.curationDao = curationDao;
		this.localTempFolder = localTempFolder;
		this.fastaArchiveFolder = fastaArchiveFolder;
	}

	/**
	 * @return Fasta file name - simply the shortname + .fasta
	 */
	public File getFastaFileName(Curation c) {
		return new File(fastaFolder, c.getShortName() + ".fasta");
	}

	/**
	 * Executes the curation in a separate thread, opening and closing a session.
	 */
	public void run() {
		try {
			curationDao.begin();

			if (executeCuration()) {
				return;
			}

			curationDao.commit();
		} catch (Exception t) {
			curationDao.rollback();
			LOGGER.error(t);
		} finally {
			FileUtilities.deleteNow(tempDirectory);
			this.status.setToDone();
		}
	}

	/**
	 * Executes the curation without any transactional logic.
	 *
	 * @return true if there was an error
	 */
	public boolean executeCuration() {
		//get the steps to keep around so we don't need to make many calls to getCurationSteps()
		//mainly because that method returns an unmodifiableList which will take some resources
		//to repeatedly create.
		List<CurationStep> steps = getCuration().getCurationSteps();

		//if we are performing the first step create a temporary fold based on the current time
		if (this.status.getCurrentStepNumber() < 1) {
			File resultFolder = localTempFolder;
			do {
				this.tempDirectory = new File(resultFolder
						, "curationRun_" + new SimpleDateFormat("yyyyMMdd-hhmmss").format(new Date()));
			} while (this.tempDirectory.exists());

			try {
				FileUtilities.ensureFolderExists(this.tempDirectory);
			} catch (Exception t) {
				this.status.setToDone(); //make sure that listening loops break.
				throw new MprcException("Could not create a temporary folder for running the curation: " + this.tempDirectory.getAbsolutePath(), t);
			}
		}

		//for each step in the curation
		for (CurationStep step : steps) {
			//if we have been interupted then exit.  This is pretty course grained interuption
			//but it would take a lot more work to make it finer grained.
			if (this.status.isInterrupted()) {
				this.status.addMessage("User interrupted");
				break;
			}

			//increment the step counter
			this.status.incrementStep();

			this.status.addMessage("Step " + this.status.getCurrentStepNumber() + " has begun");

			//set the inStream to be the same file as the previous outStream file
			try {
				if (outStream == null) {
					inStream = null;
				} else {
					inStream = new FASTAInputStream(outStream.getFile());
				}

				//if we couldn't find the output stream file object then it must have been deleted on us
			} catch (Exception e) {
				inStream = null; //this shouldn't happen unless there wasn't an outstream
				this.status.addMessage("Error setting up an output stream on step change");
				break; //break out of the step loop
			}

			//create a new outstream using the same test
			File newOutFile = new File(this.tempDirectory.getPath(), String.valueOf(this.status.getCurrentStepNumber()));
			try {
				this.outStream = new FASTAOutputStream(newOutFile);
			} catch (IOException e) {
				status.addMessage("Error setting up the next output file after step " + newOutFile);
				LOGGER.error(e);
			}
			//have the step perform itself
			StepValidation postValidation = step.performStep(this);
			//close the streams that were previously open
			if (this.inStream != null) {
				this.inStream.close();
			}
			if (this.outStream != null) {
				this.outStream.close();
			}
			if (postValidation.isOK()) {
				this.status.addCompletedStepValidation(postValidation);
			} else {
				for (String msg : postValidation.getMessages()) {
					this.status.addMessage("Step failed: " + msg);
				}
				this.status.addFailedStepValidation(postValidation);
				this.status.setToDone(); //break out of the step loop
				return true;
			}
			this.status.addMessage("Step " + this.status.getCurrentStepNumber() + " completed with " +
					postValidation.getCompletionCount() + " sequences");
		}

		//if the resulting fasta file is not valid then we want to say something in the status but we should probably just complete anyway
		if (this.outStream == null || !FASTAInputStream.isFASTAFileValid(this.outStream.getFile())) {
			this.status.addMessage("Error: The resulting .fasta file is not valid!");
		} else if (retainArtifacts) {
			//if we want to keep artifacts then move them to a safe place and update the curation to indicate that it has been run
			File finalPlace = getFastaFileName(this.curation);

			File substitutableFile = null;

			this.status.addMessage("Determining if there is an identical file already (may take a moment)");
			substitutableFile = FileUtilities.findSingleSimilarFile(this.outStream.getFile(), finalPlace.getParentFile());

			if (substitutableFile != null) {
				LOGGER.info("There already was an identical file so we will use it: " + substitutableFile.getAbsolutePath());
				finalPlace = substitutableFile;
				//just leave the original final place in the disposable folder it will be deleted.
				this.status.addMessage("There already was an identical file so we will use that " + finalPlace);
			} else {
				if (!this.outStream.getFile().renameTo(finalPlace)) {
					this.status.addMessage("Copying result file to final place (may take a few moments): " + finalPlace);
					FileUtilities.copyFile(this.outStream.getFile(), finalPlace, false);
				} else {
					this.status.addMessage("Moving result file to final place: " + finalPlace);
				}
			}

			this.curation.setCurationFile(finalPlace);

			//set the rundate of the just ran curation
			curation.setRunDate(new DateTime());

			if (curation.getDeploymentDate() == null) {
				curation.setDeploymentDate(new DateTime());
			}

			curationDao.addCuration(curation);
		}
		return false;
	}

	/**
	 * gets the status object of this curation.  You might want to get it before you run execute because it will allow
	 * you to get feedback as far as progress is concerned.  This feature will require some concurrency but it shouldn't
	 * be too difficult to provide real-time progress.
	 *
	 * @return the CurationStatus object that is permenantly associated with this object
	 */
	public CurationStatus getStatusObject() {
		return this.status;
	}

	/**
	 * gets the curent DBInputStream.  This is bound to change at any moment and should only be modified by this object
	 *
	 * @return the stream that is being used as input
	 */
	public DBInputStream getCurrentInStream() {
		return inStream;
	}

	/**
	 * gets the current DBOutputStream.  This also could change at any moment
	 *
	 * @return the steam that is being used by output
	 */
	public DBOutputStream getCurrentOutStream() {
		return outStream;
	}

	/**
	 * gets the curation that this executor is working on
	 *
	 * @return the curation that we are set to execute
	 */
	private Curation getCuration() {
		return curation;
	}

	/**
	 * a convenience way of starting an asychronous execution and getting back the status object
	 *
	 * @return the CurationStatus that keeps track of the status of the execution
	 */
	public CurationStatus execute() {
		CurationStatus retStatus = getStatusObject();
		Thread runner = new Thread(this);
		runner.setName("CurationExecutor: " + curation.getShortName());
		runner.start();
		return retStatus;
	}

	public CurationDao getCurationDao() {
		return curationDao;
	}

	public File getFastaArchiveFolder() {
		return fastaArchiveFolder;
	}

	/**
	 * An implementation of CurationStatus that only this class will be aware of.  Only CurationExecutor will need
	 * certain functionality and the only source of an instance of this Interface should be through a CurationExecutor
	 *
	 * @author Eric J. Winter Date: Apr 6, 2007 Time: 9:56:48 AM
	 */
	private class MyCurationStatus implements CurationStatus {
		/**
		 * a list of step validations representing failed steps
		 */
		private final List<String> messages = new ArrayList<String>();

		/**
		 * a list of step validations that completed succesfully
		 */
		private List<StepValidation> completedStepValidations = new ArrayList<StepValidation>();

		/**
		 * a list of the step validation representing steps that failed
		 */
		private List<StepValidation> failedStepValidations = new ArrayList<StepValidation>();

		/**
		 * a progress indicator between 0 and 100
		 */
		private float currentStepProgress = 0f;

		/**
		 * a counter indicating which step we are currently on
		 */
		private int whichStep = 0;

		/**
		 * set to true when we are done performing the execution
		 */
		private boolean executionComplete = false;

		/**
		 * set to true when we want to interrupt the execution
		 */
		private boolean interrupt = false;

		/**
		 * create a new status object
		 */
		public MyCurationStatus() {
			super();
		}

		/**
		 * get the messages that have been added the messages are then removed form so they won't be hurd from again
		 *
		 * @return the list of messages that have been added
		 */
		public synchronized List<String> getMessages() {
			List<String> retMessages = new ArrayList<String>();
			for (String s : this.messages) {
				retMessages.add(s);
			}
			this.messages.clear();
			return retMessages;
		}

		/**
		 * Add a message to this status object.
		 *
		 * @param toAdd message to add
		 */
		public synchronized void addMessage(String toAdd) {
			this.messages.add(toAdd);
		}

		/**
		 * get the percent of progress that has been made on the currently executing step (no guarenttee :))
		 *
		 * @return the progress of the currently executing step
		 */
		public synchronized float getCurrentStepProgress() {
			return this.currentStepProgress;
		}

		/**
		 * set the progress of the current step
		 *
		 * @param progress the progress you want to set
		 */
		public synchronized void setCurrentStepProgress(float progress) {
			if (progress > 100f) {
				progress = 100f;
			}
			if (progress < 0f) {
				progress = 0f;
			}
			this.currentStepProgress = progress;

		}

		/**
		 * get a list of the validations of the completed steps
		 *
		 * @return a list of validations of successfully completed steps
		 */
		public synchronized List<StepValidation> getCompletedStepValidations() {

			return Collections.unmodifiableList(completedStepValidations);
		}

		/**
		 * add a step that be completed successfully.  It is up to the caller to check to make sure the step was
		 * succesfull
		 *
		 * @param toAdd the step that was completed successfully (it is up to caller to check)
		 */
		public synchronized void addCompletedStepValidation(StepValidation toAdd) {
			if (this.completedStepValidations == null) {
				this.completedStepValidations = new ArrayList<StepValidation>();
			}
			this.completedStepValidations.add(toAdd);
		}

		/**
		 * add a setp to the list of failed steps.  The caller is responsible of make sure it is a failed step
		 *
		 * @param toAdd - the step that was failed
		 */
		public synchronized void addFailedStepValidation(StepValidation toAdd) {
			if (this.failedStepValidations == null) {
				this.failedStepValidations = new ArrayList<StepValidation>();
			}
			this.failedStepValidations.add(toAdd);
		}

		/**
		 * @return
		 */
		public synchronized List<StepValidation> getFailedStepValidations() {
			//if (this.failedStepValidations == null || this.failedStepValidations.size() == 0) return null;
			return Collections.unmodifiableList(failedStepValidations);
		}

		/**
		 * A method that can be called to see if this executor is still executing or not
		 *
		 * @return true if the executor is still running else false
		 */
		public synchronized boolean isInProgress() {
			return !this.executionComplete && !this.interrupt;
		}

		/**
		 * Sets the executor to a done state.
		 */
		public synchronized void setToDone() {
			this.executionComplete = true;
			this.notifyAll();
		}

		/**
		 * call this to interrupt execution of the curation
		 */
		public synchronized void causeInterrupt() {
			this.interrupt = true;
			this.notifyAll();
		}

		/**
		 * call this to dermine if an interrupt was requested
		 *
		 * @return true if an interrupt has been asked for
		 */
		public synchronized boolean isInterrupted() {
			return this.interrupt;
		}

		/**
		 * increment the counter which indicates which step we are currently on
		 */
		public synchronized void incrementStep() {
			this.currentStepProgress = 0;
			this.whichStep++;
		}

		/**
		 * determines which step is currently being executed
		 *
		 * @return the step that is currently being executed
		 */
		public synchronized int getCurrentStepNumber() {
			return this.whichStep;
		}

		/**
		 * the number of steps that were present in the curation after the last step was complete
		 *
		 * @return
		 */
		public synchronized int getLastStepSequenceCount() {
			if (this.completedStepValidations.size() == 0) {
				return -1;
			}
			return this.completedStepValidations.get(this.completedStepValidations.size() - 1).getCompletionCount();
		}
	}

	/**
	 * Turns a list of validations into a nice debug output.
	 *
	 * @param failedValidations Validations that failed.
	 * @return String describing the exceptions and their stack traces that lead to the failure.
	 */
	public static String failedValidationsToString(List<StepValidation> failedValidations) {
		StringBuilder sb = new StringBuilder();
		for (StepValidation failedValidation : failedValidations) {
			for (String msg : failedValidation.getMessages()) {
				sb.append(msg);
				if (failedValidation.getWrappedExceptions() != null && failedValidation.getWrappedExceptions().size() > 0) {
					for (Exception e : failedValidation.getWrappedExceptions()) {
						sb.append("\n\t* ");
						sb.append(e.getMessage());
						sb.append(" ----------------");
						sb.append('\n');
						StringWriter writer = new StringWriter();
						e.printStackTrace(new PrintWriter(writer));
						sb.append(StringUtilities.appendTabBeforeLines(writer.toString()));
						sb.append("\n");
					}
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
