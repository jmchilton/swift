package edu.mayo.mprc.dbcurator.server;

import java.util.Date;
import java.util.List;

import edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStepStub;
import edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStub;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.CurationStep;

public interface CurationHandlerI {

	/**
	 * takes a CurationStub and synchronizes it to the server side.  This will take the changes from the stub and make the server
	 * side Curation match the stub.  The only exception is messages that will be sync'd from the Curation to the Client since there
	 * are no messages with a Curation object but only with Validations.
	 * <p/>
	 * As a side effect, if we are actually running the curation right now, the messages from the execution will be added
	 * to the input CurationStub.
	 *
	 * @param toSync the stub that you want to synce to a curation
	 * @return the curation that was sync'd
	 */
	CurationStub syncCuration(CurationStub toSync);

	/**
	 * creates a CurationStep from a given CurationStepStub.  It first looks in the possibleContainer by id to see if it
	 * is already in the container.  If so it returns the one that was already in the container else it will create a new
	 * step that has the same field as the stub.  This is a very complex method using type checking
	 * that will needed to be changed if new steps are added.
	 *
	 * @param stub the stub you want to get a server side Step for
	 * @return the step that the given stub could represent
	 */
	CurationStep createStepFromStub(CurationStepStub stub);

	/**
	 * use this method to create a stub for a curation.  Dates are outuput in MM/dd/yyyy format.
	 *
	 * @param toStubify the curation you want to createa  stub for
	 * @return the corresponding stub
	 */
	CurationStub createStub(Curation toStubify);

	/**
	 * takes a curation stub with fields in it and returns an array of CurationStubs that match the given stubs and exist
	 * in persistent store.  The fields that can be used in a search include.  * can be used as a wild card for any of these fields.
	 * - owner email
	 * - short name
	 * - run date (within range)
	 *
	 * @param stub            the stub you want to at least match with returned stubs
	 * @param earliestRunDate the earliest run date (inclusive) for curations you want returned (or null for unbounded)
	 * @param latestRunDate   the latest run date (inclusive) for curations you want returned (or null for unbounded)
	 * @return the matching curation stubs or null if there were no matches or if the sample was overly vague.
	 */
	List<CurationStub> getMatchingCurations(CurationStub stub,
			Date earliestRunDate, Date latestRunDate);

	CurationStub getCurationByID(Integer id);

	/**
	 * takes a curation and executes it.  The status for the execution will be retained for subsequent synchronizations
	 * to the Curation
	 * <p/>
	 *
	 * @param toExecute the CurationStub that you want to execute
	 * @return the curationstub for the curation after it was executed.  This is needed because things may have been changed since the execution occured.
	 */
	CurationStub executeCuration(CurationStub toExecute);

	/**
	 * gets a stub that represents the currently cached curation of this object
	 *
	 * @return the stub for the current cached curation
	 */
	CurationStub getCachedCurationStub();

	/**
	 * set the curation that should be cached with this handler
	 *
	 * @param toSet the curation to be associated with this handler
	 */
	void setCachedCuration(Curation toSet);

	/**
	 * gets the curation cached with this object
	 *
	 * @return the curation cached with this object
	 */
	Curation getCachedCuration();

}