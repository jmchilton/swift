package edu.mayo.mprc.dbcurator.model.curationsteps;

import edu.mayo.mprc.dbcurator.model.CurationExecutor;
import edu.mayo.mprc.dbcurator.model.CurationStatus;
import edu.mayo.mprc.dbcurator.model.CurationStep;
import edu.mayo.mprc.dbcurator.model.StepValidation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.fasta.DBInputStream;
import edu.mayo.mprc.fasta.DBOutputStream;
import edu.mayo.mprc.fasta.filter.ReversalStringManipulator;
import edu.mayo.mprc.fasta.filter.ScrambleStringManipulator;
import edu.mayo.mprc.fasta.filter.StringManipulator;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A CurationStep that takes a CurationExecutor and randomizes the sequence.  If isOverwriteMode() returns true that
 * means that we are set to remove the original sequence otherwise both the original and randomized sequence will be
 * included. <br> All randomized sequences will have a "&lt;randomized&gt;" tag appended to the end of the header
 * <p/>
 *
 * @author Eric J. Winter Date: Apr 10, 2007 Time: 12:03:54 PM
 */
public class MakeDecoyStep implements CurationStep {
	private static final long serialVersionUID = 20071220L;

	/**
	 * we want a reversal to be performed so the output sequence will be the reverse of the input sequence
	 */
	public static final int REVERSAL_MANIPULATOR = 1;

	/**
	 * we want the characters to be randomized within each sequence
	 */
	public static final int SCRAMBLE_MANIPULATOR = 2;

	// Helps inserting Reversed_ and (Reversed) in the header.
	private static final Pattern HEADER_TRANSFORM = Pattern.compile("^>\\s*(\\S+\\s*)(.*)$");

	/**
	 * the id of the step that is used for persisent storage.  Null if not persisted
	 */
	private Integer id;

	/**
	 * whether this object will retain the original sequence or remove the original sequence
	 */
	private boolean overwriteMode = true;

	/**
	 * the string manipulator that is used.  This is not persisted but is created based on the MANIPULATOR type
	 */
	private transient StringManipulator manipulator;

	/**
	 * the manipulator type to associate with this object.  This is perstent and indicates which type of step this is
	 */
	private int manipulatorType;

	/**
	 * the validation that was created the last time this step object was run.  this will be null if the step has not been run
	 */
	private transient StepValidation lastRunValidation;

	/**
	 * null Ctor defaults to Ovewrite Mode (true)
	 */
	public MakeDecoyStep() {

	}

	/**
	 * determines if we are in overwrite mode or in append mode. Append Mode (false) indicates that the original
	 * sequence will also be retained Overwrite Mode (true) indicates that the original sequence will be removed.
	 *
	 * @return true if we will effectively erase the original sequence
	 */
	public boolean isOverwriteMode() {
		return this.overwriteMode;
	}

	/**
	 * gets the appropriate StringManipulator based on the set manipulatorType
	 *
	 * @return
	 */
	private StringManipulator getManipulator() {
		if (this.manipulator == null) {
			if (manipulatorType == REVERSAL_MANIPULATOR) {
				this.manipulator = new ReversalStringManipulator();
			} else if (manipulatorType == SCRAMBLE_MANIPULATOR) {
				this.manipulator = new ScrambleStringManipulator();
			}
		}
		return this.manipulator;
	}

	/**
	 * the enumerated type of manipulator that we are set to use (see this classes *_MANIPULATOR enumerations)
	 *
	 * @return
	 */
	public int getManipulatorType() {
		return this.manipulatorType;
	}

	/**
	 * the enumerated type of manipulator we are set to use (see this classes *_MANIPULATOR enumerations)
	 *
	 * @param type
	 */
	public void setManipulatorType(int type) {
		this.manipulatorType = type;
	}


	/**
	 * set the mode you want
	 *
	 * @param mode true if you want to be in overwrite mode else false
	 */
	public void setOverwriteMode(boolean mode) {
		this.overwriteMode = mode;
	}

	/**
	 * perfom the step on a given local database.  If the step could not be performed then a CurationStepException is
	 * thrown.  This indicates that the PostValidation will be unsuccessful and will contain a message indicating why it
	 * was unsuccesfull.
	 * <p/>
	 * There are obviously a wide variety of things that could go wrong with a call to perform step.
	 *
	 * @param exec the CurationExecutor that we are performing this step for
	 * @return the post validation.  This is the same object that will be returned by a call to postValidate()
	 */
	public StepValidation performStep(CurationExecutor exec) {
		return performStep(exec.getCurrentInStream(), exec.getCurrentOutStream(), exec.getStatusObject());
	}

	/**
	 * Actual implementation of the performStep method (for easy testability)
	 *
	 * @param in     Input stream.
	 * @param out    Output stream.
	 * @param status The progress is updated here, we also take the amount of sequences from here.
	 * @return Information about how the step performed.
	 */
	StepValidation performStep(DBInputStream in, DBOutputStream out, CurationStatus status) {
		//make sure we meet at least the pre validation criteria, this will also make sure our manipulator is set
		this.lastRunValidation = this.preValidate(null);
		if (!lastRunValidation.isOK()) {
			return lastRunValidation;
		}

		//the the number of sequences we need to manipulate
		int numberOfSequences = status.getLastStepSequenceCount();
		int currentSequence = 0;
		try {
			//if we want to append the sequences we need to first write out he origal sequences
			if (!this.overwriteMode) {
				in.beforeFirst();

				while (in.gotoNextSequence()) {
					status.setCurrentStepProgress(Math.round(50f * ++currentSequence / numberOfSequences));
					out.appendSequence(
							in.getHeader(),
							in.getSequence()
					);
				}
			}
			in.beforeFirst();
			float percentMultiplier = (this.overwriteMode ? 100f : 50f); //if we are doing an append then we only want to multiply the manipulation by 50% not 100%
			while (in.gotoNextSequence()) {
				status.setCurrentStepProgress(percentMultiplier * ++currentSequence / numberOfSequences);

				out.appendSequence(
						modifyHeader(in.getHeader(), this.manipulator.getDescription()), //the modified header
						this.manipulator.manipulateString(in.getSequence()) //the manipulated sequence
				);
			}
			this.lastRunValidation.setCompletionCount(out.getSequenceCount());
			this.setLastRunCompletionCount(out.getSequenceCount());
		} catch (IOException e) {
			this.lastRunValidation.addMessageAndException("Error in performing database IO", e);
		} catch (Exception e) {
			this.lastRunValidation.addMessageAndException(e.getMessage(), e);
		}
		return this.lastRunValidation;

	}

	/**
	 * modify the header so that a description of the modification precedes the original header
	 *
	 * @param header      Header to modify, e.g. <code>&gt;WHATEVER Whatever description</code>
	 * @param description Description of the change, e.g. <code>Reversed</code>
	 * @return Modified header <code>&gt;Reversed_WHATEVER (Reversed) Whatever description</code>
	 */
	static String modifyHeader(String header, String description) {
		String modifiedHeader;
		final Matcher matcher = HEADER_TRANSFORM.matcher(header);
		if (matcher.matches()) {

			modifiedHeader = ">" + description + "_" + matcher.group(1) + (matcher.group(2).length() > 0 ? ("("
					+ description + ") " + matcher.group(2)) : "");
		} else {
			modifiedHeader = header;
		}
		return modifiedHeader;
	}

	/**
	 * Call this method if you want to see if the step is ready to be run and if any issues have been predicted.  NOTE:
	 * succesfull prevalidation can not guarentee<sp> successful processing.
	 *
	 * @param curationDao
	 * @return the @see StepValidation to interrogate for issues
	 */
	public StepValidation preValidate(CurationDao curationDao) {
		StepValidation prevalidation = new StepValidation();

		//make sure we have a valid type set
		if (this.getManipulator() == null) {
			prevalidation.setMessage("Invalid manipulator selected");
		}

		return prevalidation;
	}

	/**
	 * Call this method if you want to see the results of the last performStep call and you didn't keep track of the
	 * returned object
	 *
	 * @return the return value of the last call to performStep()
	 */
	public StepValidation postValidate() {
		return this.lastRunValidation;
	}

	/**
	 * Creates a copy of this step.  Only persistent properties are included in the copy.  The id is not included since
	 * this would make it identical in the database.
	 *
	 * @return a cropy of this step
	 */
	public CurationStep createCopy() {
		MakeDecoyStep copy = new MakeDecoyStep();
		copy.overwriteMode = this.overwriteMode;
		copy.manipulatorType = this.manipulatorType;
		return copy;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * the number of sequences that were present in the curation after this step was last run
	 */
	private Integer lastRunCompletionCount = null;

	public Integer getLastRunCompletionCount() {
		return this.lastRunCompletionCount;
	}

	public void setLastRunCompletionCount(Integer count) {
		this.lastRunCompletionCount = count;
	}

	public String simpleDescription() {
		return this.getManipulator().getDescription();
	}

}
