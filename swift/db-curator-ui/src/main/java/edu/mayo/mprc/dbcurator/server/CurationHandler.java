package edu.mayo.mprc.dbcurator.server;

import edu.mayo.mprc.dbcurator.client.CurationValidation;
import edu.mayo.mprc.dbcurator.client.curatorstubs.*;
import edu.mayo.mprc.dbcurator.model.*;
import edu.mayo.mprc.dbcurator.model.curationsteps.*;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.fasta.DatabaseAnnotation;
import edu.mayo.mprc.fasta.filter.MatchMode;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class will take a CurationStub and get the relevant Curation object or take a Curation object and generate a
 * relavent CurationStub.  This is non-trivial since we need to use reflection to determine the types of the steps in order
 * to create the correct CurationStepStubs or the reverse.  This is a ThreadLocal Singleton that will cache a Curation object
 * that it thinks is represented client side and will look in the cache first before looking in persistent store when a
 * an operation is requested.
 * <p/>
 * This class will also handle operations on curations
 */
public final class CurationHandler implements CurationHandlerI {
	/**
	 * looks for a CurationHandler titled "curationHandler" in the given session object and returns it.  If it is not found
	 * then a new one is created and placed on the given session with the given name ("curationHandler").
	 *
	 * @param session the HttpSession that we want to look for the CurationHandler on
	 * @return the curation handler that was on the session or a new one that has been put on the session
	 */
	public static CurationHandlerI getInstance(HttpSession session) {

		CurationHandlerI perSession = null;
		try {
			perSession = (CurationHandlerI) session.getAttribute("curationHandler");
		} catch (Exception e) {
			LOGGER.warn("Error getting a curation handler", e);
			perSession = null;
		}

		if (perSession == null) {
			perSession = new CurationHandler();
			session.setAttribute("curationHandler", perSession);
		}
		return perSession;
	}

	/**
	 * a cache of a single Curation that is used to reduce hits to the database.  Only a single instance is cached to minimize
	 * risk of collision with persisted objects.
	 */
	private Curation cache = new Curation();

	/**
	 * the status object that ws the result of the last execution (or currently running)
	 */
	private CurationStatus lastRunStatus = null;

	private CurationDao curationDao = CurationWebContext.getCurationDAO();
	private File fastaFolder = CurationWebContext.getFastaFolder();
	private File localTempFolder = CurationWebContext.getLocalTempFolder();
	private File fastaArchiveFolder = CurationWebContext.getFastaArchiveFolder();

	/**
	 * protect the constructor to ensure ThreadLocal access to instances
	 */
	protected CurationHandler() {
		super();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.mprc.dbcurator.server.CurationHandlerI#syncCuration(edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStub)
	 */
	@Override
	public CurationStub syncCuration(CurationStub toSync) {

		//clear the error messages that may have previously existed
		toSync.getErrorMessages().clear();

		// if we are running then update the progress status of the curation stub.
		if (this.lastRunStatus != null) {
			upgradeProgressInCurationStub(toSync);
		}

		Curation curation = getCurationForStub(toSync);

		//trim and check the length of the shortName and also make sure it doesn't contain space
		toSync.setShortName(toSync.getShortName().trim());
		String errorMessage = CurationValidation.validateShortNameLegalCharacters(toSync.getShortName());
		if (errorMessage != null) {
			toSync.getErrorMessages().add(errorMessage);
		}

		//set all of the mutable fields to match the stub
		curation.setShortName(toSync.getShortName());
		curation.setTitle(toSync.getTitle());
		curation.setNotes(toSync.getNotes());
		curation.setOwnerEmail(toSync.getOwnerEmail());
		curation.setDecoyRegex(toSync.getDecoyRegex());

		// If we know where the resulting file went, we update the curation
		if (toSync.getPathToResult() != null && toSync.getPathToResult().trim().length() > 0) {
			curation.setCurationFile(new File(toSync.getPathToResult().trim()));
		}

		try {
			if (toSync.getLastRunDate() == null || toSync.getLastRunDate().trim().length() == 0) {
				curation.setRunDate(null);
			} else {
				curation.setRunDate(dater.parse(toSync.getLastRunDate()));
			}
		} catch (ParseException e) {
			toSync.getErrorMessages().add("Run date error MM/dd/yyyy expected");
		}

		//sync the list of steps from the stub to the curation

		//create a list to temporarily hold the new list of steps as they are generateed
		List<CurationStep> newStepList = new ArrayList<CurationStep>();

		//iterate through each step stub and create a step for them and add them to the new list of steps
		for (Object stepStub : toSync.getSteps()) {
			CurationStepStub stub = (CurationStepStub) stepStub;
			CurationStep step = createStepFromStub(stub);
			newStepList.add(step);
		}

		//emtpy out the current list of steps in the curation
		curation.clearSteps();

		//put the new list into the curation
		for (CurationStep step : newStepList) {
			curation.addStep(step, -1);
		}

		if (curation.getCurationFile() != null) {
			toSync.setPathToResult(curation.getCurationFile().getAbsolutePath());
		}

		//return the newly updated curation
		return toSync;
	}

	/**
	 * Return clean curation from the database/or a new object if not saved yet that corresponds to given
	 * curation stub. We cache last curation to reduce database traffic.
	 */
	private Curation getCurationForStub(CurationStub toSync) {
		//if we have a stub without an ID then we want to create a new curation
		//else if the sync has an id then we want to retreive the object from the database
		//finally we want to create a new curation if the curation wasn't found in persisetent store.
		if (toSync.getId() == null) {
			cache = null;
		} else if (cache == null || cache.getId() == null || !cache.getId().equals(toSync.getId())) {
			cache = curationDao.getCuration(toSync.getId());
		}
		//if we still have no cache then create a new one.
		if (cache == null) {
			cache = new Curation();
		}

		cache.setId(toSync.getId());
		return cache;
	}

	private void upgradeProgressInCurationStub(CurationStub toSync) {
		//if there are any global error messages then sync them to the stub
		List<String> msgs = this.lastRunStatus.getMessages();
		for (String msg : msgs) {
			toSync.getErrorMessages().add(msg);
		}

		int i = 0;
		for (StepValidation stepValidation : this.lastRunStatus.getCompletedStepValidations()) {
			(toSync.getSteps().get(i++)).setCompletionCount(stepValidation.getCompletionCount());
		}

		//if we have more steps to report on then one must be executing
		if (i < toSync.getSteps().size()) { //we had a failure in the last step
			if (this.lastRunStatus.getFailedStepValidations().size() > 0) {
				StepValidation failedValidation = this.lastRunStatus.getFailedStepValidations().get(0);
				CurationStepStub syncStep = toSync.getSteps().get(i);

				for (String message : failedValidation.getMessages()) {
					syncStep.addMessage(message);
				}
			} else { //we are current performing the next set sp report progress
				(toSync.getSteps().get(i)).setProgress((int) this.lastRunStatus.getCurrentStepProgress());
			}
		}
	}


	/* (non-Javadoc)
	 * @see edu.mayo.mprc.dbcurator.server.CurationHandlerI#createStepFromStub(edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStepStub)
	 */
	@Override
	public CurationStep createStepFromStub(CurationStepStub stub) {
		CurationStep retStep = null;

		//try to find out what type of stub it is and then do the appropriate syncing
		if (stub instanceof NewDatabaseInclusionStub) {
			NewDatabaseInclusionStub concreteStub = (NewDatabaseInclusionStub) stub;
			NewDatabaseInclusion returnStep = new NewDatabaseInclusion();
			returnStep.setUrl(concreteStub.url);
			retStep = returnStep;
		} else if (stub instanceof HeaderFilterStepStub) {
			HeaderFilterStepStub concreteStub = (HeaderFilterStepStub) stub;
			HeaderFilterStep returnStep = new HeaderFilterStep();

			returnStep.setCriteriaString(concreteStub.criteria);

			if (concreteStub.matchMode.equalsIgnoreCase("all")) {
				returnStep.setMatchMode(MatchMode.ALL);
			} else if (concreteStub.matchMode.equalsIgnoreCase("none")) {
				returnStep.setMatchMode(MatchMode.NONE);
			} else {
				//fallback to any
				returnStep.setMatchMode(MatchMode.ANY);
			}

			if (concreteStub.textMode.equalsIgnoreCase("regex")) {
				returnStep.setTextMode(TextMode.REG_EX);
			} else {
				returnStep.setTextMode(TextMode.SIMPLE);
			}

			retStep = returnStep;
		} else if (stub instanceof ManualInclusionStepStub) {
			ManualInclusionStepStub concreteStub = (ManualInclusionStepStub) stub;
			ManualInclusionStep returnStep = new ManualInclusionStep();

			returnStep.setHeader(concreteStub.header);
			returnStep.setSequence(concreteStub.sequence);

			retStep = returnStep;
		} else if (stub instanceof SequenceManipulationStepStub) {
			SequenceManipulationStepStub concreteStub = (SequenceManipulationStepStub) stub;
			MakeDecoyStep returnStep = new MakeDecoyStep();

			returnStep.setOverwriteMode(concreteStub.overwrite);
			if (concreteStub.manipulationType.equalsIgnoreCase(SequenceManipulationStepStub.REVERSAL)) {
				returnStep.setManipulatorType(MakeDecoyStep.REVERSAL_MANIPULATOR);
			} else {
				returnStep.setManipulatorType(MakeDecoyStep.SCRAMBLE_MANIPULATOR);
			}

			retStep = returnStep;
		} else if (stub instanceof DatabaseUploadStepStub) {
			DatabaseUploadStepStub concreteStub = (DatabaseUploadStepStub) stub;
			DatabaseUploadStep returnStep = new DatabaseUploadStep();
			returnStep.setFileName(concreteStub.clientFilePath);
			returnStep.setPathToUploadedFile(new File(concreteStub.serverFilePath));

			retStep = returnStep;
		} else if (stub instanceof HeaderTransformStub) {
			HeaderTransformStub concreteStub = (HeaderTransformStub) stub;
			HeaderTransformStep returnStep = new HeaderTransformStep();
			returnStep.setDescription(concreteStub.description);
			returnStep.setMatchPattern(concreteStub.matchPattern);
			returnStep.setSubstitutionPattern(concreteStub.subPattern);

			retStep = returnStep;
		}

		//set the id no matter what type of stub we had
		if (retStep != null) {
			retStep.setId(stub.getId());
			retStep.setLastRunCompletionCount(stub.getCompletionCount());
		}

		//return the determined step.  This may be null if we couldn't determine a stub type
		return retStep;
	}

	/**
	 * A simple date formatter that will format all dates as MM/dd/yyyy
	 */
	private DateFormat dater = new SimpleDateFormat("MM/dd/yyyy");

	/* (non-Javadoc)
	 * @see edu.mayo.mprc.dbcurator.server.CurationHandlerI#createStub(edu.mayo.mprc.dbcurator.model.Curation)
	 */
	@Override
	public CurationStub createStub(Curation toStubify) {

		//create the stub and fill in all fields
		CurationStub result = new CurationStub();
		result.setId(toStubify.getId());
		result.setShortName(toStubify.getShortName() != null ? toStubify.getShortName() : "");
		result.setTitle(toStubify.getTitle() != null ? toStubify.getTitle() : "");
		result.setDecoyRegex(toStubify.getDecoyRegex() != null ? toStubify.getDecoyRegex() : DatabaseAnnotation.DEFAULT_DECOY_REGEX);
		result.setOwnerEmail(toStubify.getOwnerEmail() != null ? toStubify.getOwnerEmail() : "");
		result.setPathToResult(toStubify.getCurationFile() != null ? toStubify.getCurationFile().getAbsolutePath() : "");

		if (toStubify.getRunDate() != null) {
			result.setLastRunDate(dater.format(toStubify.getRunDate()));
		}

		result.setNotes(toStubify.getNotes());
		if (result.getNotes() == null) {
			result.setNotes("");
		}

		// create a stub for each of the steps in the curation
		for (int i = 0; i < toStubify.stepCount(); i++) {
			result.getSteps().add(getStepStub(toStubify.getStepByIndex(i)));
		}

		//if there are any global error messages then sync them to the stub
		if (this.lastRunStatus != null) {
			List<String> msgs = this.lastRunStatus.getMessages();
			for (String msg : msgs) {
				result.getErrorMessages().add(msg);
			}
		}
		return result;
	}

	/**
	 * creates stub for a given CurationStep.  This uses type casting so update will need to be made if new step types are added.
	 *
	 * @param toStub the step that you want to create a stub for
	 * @return a stub for the toStub step
	 */
	private CurationStepStub getStepStub(CurationStep toStub) {
		CurationStepStub stub = null;

		if (toStub instanceof NewDatabaseInclusion) {
			NewDatabaseInclusionStub concreteStub = new NewDatabaseInclusionStub();
			NewDatabaseInclusion concreteStep = (NewDatabaseInclusion) toStub;
			concreteStub.url = concreteStep.getUrl();
			if (concreteStub.url == null) {
				concreteStub.url = "";
			}
			stub = concreteStub;
		} else if (toStub instanceof HeaderFilterStep) {
			HeaderFilterStepStub concreteStub = new HeaderFilterStepStub();
			HeaderFilterStep concreteStep = (HeaderFilterStep) toStub;

			concreteStub.criteria = concreteStep.getCriteriaString();
			if (concreteStub.criteria == null) {
				concreteStub.criteria = "";
			}
			if (concreteStep.getMatchMode() == MatchMode.ALL) {
				concreteStub.matchMode = "all";
			} else if (concreteStep.getMatchMode() == MatchMode.NONE) {
				concreteStub.matchMode = "none";
			} else {
				concreteStub.matchMode = "any";
			}

			if (concreteStep.getTextMode() == TextMode.REG_EX) {
				concreteStub.textMode = "regex";
			} else {
				concreteStub.textMode = "simple";
			}

			stub = concreteStub;

		} else if (toStub instanceof ManualInclusionStep) {
			ManualInclusionStepStub concreteStub = new ManualInclusionStepStub();
			ManualInclusionStep concreteStep = (ManualInclusionStep) toStub;

			concreteStub.header = concreteStep.getHeader();
			if (concreteStub.header == null) {
				concreteStub.header = "";
			}
			concreteStub.sequence = concreteStep.getSequence();
			if (concreteStub.sequence == null) {
				concreteStub.sequence = "";
			}

			stub = concreteStub;
		} else if (toStub instanceof MakeDecoyStep) {
			SequenceManipulationStepStub concreteStub = new SequenceManipulationStepStub();
			MakeDecoyStep concreteStep = (MakeDecoyStep) toStub;

			concreteStub.overwrite = concreteStep.isOverwriteMode();
			if (concreteStep.getManipulatorType() == MakeDecoyStep.REVERSAL_MANIPULATOR) {
				concreteStub.manipulationType = SequenceManipulationStepStub.REVERSAL;
			} else {
				concreteStub.manipulationType = SequenceManipulationStepStub.SCRAMBLE;
			}

			stub = concreteStub;
		} else if (toStub instanceof DatabaseUploadStep) {
			DatabaseUploadStepStub concreteStub = new DatabaseUploadStepStub();
			DatabaseUploadStep concreteStep = (DatabaseUploadStep) toStub;

			concreteStub.serverFilePath = concreteStep.getPathToUploadedFile().getAbsolutePath();
			concreteStub.clientFilePath = concreteStep.getFileName();

			stub = concreteStub;
		} else if (toStub instanceof HeaderTransformStep) {
			HeaderTransformStub concreteStub = new HeaderTransformStub();
			HeaderTransformStep concreteStep = (HeaderTransformStep) toStub;

			concreteStub.description = concreteStep.getDescription();
			concreteStub.matchPattern = concreteStep.getMatchPattern();
			concreteStub.subPattern = concreteStep.getSubstitutionPattern();

			stub = concreteStub;
		}

		if (stub != null) {
			stub.setId(toStub.getId());
			stub.setCompletionCount(toStub.getLastRunCompletionCount());
		}

		return stub;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.mprc.dbcurator.server.CurationHandlerI#getMatchingCurations(edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStub, java.util.Date, java.util.Date)
	 */
	@Override
	public List<CurationStub> getMatchingCurations(CurationStub stub, Date earliestRunDate, Date latestRunDate) {
		//this.syncCuration(stub);
		//Curation sample = this.getCachedCuration();
		this.lastRunStatus = null;
		Curation sample = new Curation();
		sample.setId(stub.getId());
		sample.setNotes(null);


		List<Curation> matchingCurations = curationDao.getMatchingCurations(sample, earliestRunDate, latestRunDate);

		if (matchingCurations == null || matchingCurations.size() == 0) {
			return new ArrayList<CurationStub>();
		}

		List<CurationStub> stubs = new ArrayList<CurationStub>();

		for (Curation curation : matchingCurations) {
			stubs.add(this.createStub(curation));
		}

		this.lastRunStatus = null;
		return stubs;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.mprc.dbcurator.server.CurationHandlerI#getCurationByID(java.lang.Integer)
	 */
	@Override
	public CurationStub getCurationByID(Integer id) {
		this.cache = null;
		this.lastRunStatus = null;

		if (id == null) {
			this.cache = new Curation();
			return new CurationStub();
		}

		this.cache = curationDao.getCuration(id);

		if (this.cache != null) {
			return this.createStub(this.cache);
		} else {
			this.cache = new Curation();
			return new CurationStub();
		}

	}

	/* (non-Javadoc)
	 * @see edu.mayo.mprc.dbcurator.server.CurationHandlerI#executeCuration(edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStub)
	 */
	@Override
	public CurationStub executeCuration(CurationStub toExecute) {
		//sync the curation to the cache
		this.syncCuration(toExecute);

		// the cache is now set to curation matching our stub
		Curation curation = this.cache;

		CurationExecutor curationExecutor = new CurationExecutor(curation, true, curationDao, fastaFolder, localTempFolder, fastaArchiveFolder);
		this.lastRunStatus = curationExecutor.execute();

		while (this.lastRunStatus.isInProgress()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
		}

		//make sure the cache is set to the curation
		this.cache = curation;

		// do a sync to make sure the stub matches current state
		return this.syncCuration(toExecute);
	}

	private static final Logger LOGGER = Logger.getLogger(CurationHandler.class);

	/* (non-Javadoc)
	 * @see edu.mayo.mprc.dbcurator.server.CurationHandlerI#getCachedCurationStub()
	 */
	@Override
	public CurationStub getCachedCurationStub() {
		return this.createStub(this.getCachedCuration());
	}

	/* (non-Javadoc)
	 * @see edu.mayo.mprc.dbcurator.server.CurationHandlerI#setCachedCuration(edu.mayo.mprc.dbcurator.model.Curation)
	 */
	@Override
	public void setCachedCuration(Curation toSet) {
		this.cache = toSet;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.mprc.dbcurator.server.CurationHandlerI#getCachedCuration()
	 */
	@Override
	public Curation getCachedCuration() {
		return this.cache;
	}
}
