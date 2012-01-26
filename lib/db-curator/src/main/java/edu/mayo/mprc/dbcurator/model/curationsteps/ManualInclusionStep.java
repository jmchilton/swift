package edu.mayo.mprc.dbcurator.model.curationsteps;

import edu.mayo.mprc.dbcurator.model.CurationExecutor;
import edu.mayo.mprc.dbcurator.model.CurationStep;
import edu.mayo.mprc.dbcurator.model.StepValidation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.fasta.DBInputStream;
import edu.mayo.mprc.fasta.DBOutputStream;

import java.io.IOException;

/**
 * A CurationStep that takes the database and manually adds a FASTA format sequence to it The user can either enter a
 * header and a sequence or optionally just pass in a header/sequence combination String.  If the entered String is
 * invalid or malformed then it will just sit in the sequence field and any calls to preValidate() will indicate how the
 * sequence is malformed.
 *
 * @author Eric J. Winter Date: Apr 10, 2007 Time: 11:46:11 AM
 */
public class ManualInclusionStep implements CurationStep {
	private static final long serialVersionUID = 20071220L;
	/**
	 * a unqiue identifier for this step
	 */
	protected Integer id;

	/**
	 * the header of the sequence (may be null if no valid header was detected
	 */
	private String header;

	/**
	 * the sequence that was entered.  This may contain a malformed header/footer combination
	 */
	private String sequence;

	/**
	 * the validation that was created when performStep was last performed
	 */
	private transient StepValidation runValidation;

	/**
	 * CTor that takes the header and the sequence in seperate fields.  The header does not need to contain a '>' in the
	 * first character position.
	 */
	public ManualInclusionStep() {
		super();
	}

	/**
	 * perfom the step on a given local database.  If the step could not be performed then a CurationStepException is
	 * thrown.  This indicates that the PostValidation will be unsuccessful and will contain a message indicating why it
	 * was unsuccesfull.
	 * <p/>
	 * There are obviously a wide variety of things that could go wrong with a call to perform step.
	 *
	 * @param exe the executor we are performing the step for
	 * @return the post validation.  This is the same object that will be returned by a call to postValidate()
	 */
	public StepValidation performStep(CurationExecutor exe) {
		this.runValidation = this.preValidate(exe.getCurationDao());

		//run a prevalidation before continuing.  If prevalidation fails return that StepValidation else create a new one
		//for the post validation
		if (!this.runValidation.isOK()) {
			return this.runValidation;
		}

		this.runValidation = new StepValidation();

		DBInputStream in = exe.getCurrentInStream();
		DBOutputStream out = exe.getCurrentOutStream();

		try {
			if (in != null) {
				in.beforeFirst();
				out.appendRemaining(in);
			}
			out.appendSequence(this.getHeader(), this.getSequence());
		} catch (IOException e) {
			this.runValidation.addMessageAndException("Error writing manual inclusion: " + this.getHeader(), e);
			return this.runValidation;
		}

		this.runValidation.setCompletionCount(out.getSequenceCount());
		this.setLastRunCompletionCount(out.getSequenceCount());

		return this.runValidation;
	}

	/**
	 * Call this method if you want to see if the step is ready to be run and if any issues have been predicted.  NOTE:
	 * succesfull prevalidation can not guarentee<sp> successful processing.
	 *
	 * @param curationDao
	 * @return the @see StepValidation to interrogate for issues
	 */
	public StepValidation preValidate(CurationDao curationDao) {
		StepValidation preValidation = new StepValidation();

		if (this.sequence == null) {
			preValidation.setMessage("No sequence has been entered.");
			return preValidation;
		}

		this.sequence = this.sequence.trim();

		if (this.sequence.equals("")) {
			preValidation.setMessage("No sequence has been entered.");
		}

		//if the header is still null after sequence validation then we need to get one
		if (this.header == null) {
			preValidation.setMessage("No header has been entered.");
		} else {
			this.header = this.header.trim();
			if (this.header.equals("")) {
				preValidation.setMessage("No header has been entered.");
			}
		}

		return preValidation;
	}

	/**
	 * Call this method if you want to see the results of the last performStep call and you didn't keep track of the
	 * returned object
	 *
	 * @return the return value of the last call to performStep()
	 */
	public StepValidation postValidate() {
		return this.runValidation;
	}

	/**
	 * Creates a copy of this step.  Only persistent properties are included in the copy.
	 *
	 * @return a cropy of this step
	 */
	public CurationStep createCopy() {
		ManualInclusionStep copy = new ManualInclusionStep();
		copy.setHeader(this.header);
		copy.setSequence(this.sequence);
		return copy;
	}

	/**
	 * the FASTA header (with leading '>') that this step will insert
	 */
	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * the sequence that this step will insert
	 */
	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	/**
	 * the id of this object that can uniquly identify the step
	 */
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
		return "user sequence";
	}
}
