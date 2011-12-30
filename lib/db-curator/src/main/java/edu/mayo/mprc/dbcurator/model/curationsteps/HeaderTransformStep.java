package edu.mayo.mprc.dbcurator.model.curationsteps;

import edu.mayo.mprc.database.PersistableBase;
import edu.mayo.mprc.dbcurator.model.CurationExecutor;
import edu.mayo.mprc.dbcurator.model.CurationStatus;
import edu.mayo.mprc.dbcurator.model.CurationStep;
import edu.mayo.mprc.dbcurator.model.StepValidation;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.fasta.DBInputStream;
import edu.mayo.mprc.fasta.DBOutputStream;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A step where each header in a DBInputStream is passed through a regular expression filter and a prescribed substitution
 * is applied to them.
 * Makes use of @see java.util.regex.Matcher#replaceAll function to perform this subsitution
 *
 * @author Eric Winter
 */
public class HeaderTransformStep extends CurationStepBase {
	private static final long serialVersionUID = 20071220L;

	private static final Logger LOGGER = Logger.getLogger(HeaderTransformStep.class);

	/**
	 * a description on what the subsitution does
	 */
	private String description;
	/**
	 * the pattern that we want to look through and identify classes in
	 */
	private String matchPattern = null;
	/**
	 * the string that represents the substitution we want to make
	 */
	private String substitutionPattern = null;

	/**
	 * the compiled pattern
	 */
	private Pattern compiledPattern = null;

	/**
	 * the StepValidation created the last time the step was run
	 */
	private transient StepValidation lastRunValidation = null;

	/**
	 * This step will take each header in the DBInputStream and apply the pressribed subtitution to all of the headers.
	 * {@inheritDoc}
	 */
	public StepValidation performStep(CurationExecutor exe) {
		//perform pre-validation of the step to make sure we at least pass that.
		lastRunValidation = this.preValidate(exe.getCurationDao());
		if (!lastRunValidation.isOK()) {
			return lastRunValidation;
		}

		DBInputStream in = exe.getCurrentInStream();
		DBOutputStream out = exe.getCurrentOutStream();
		CurationStatus status = exe.getStatusObject();

		//the the number of sequences we need to manipulate
		int numberOfSequences = status.getLastStepSequenceCount();
		int currentSequence = 0;
		String currentHeader = "";
		try {
			in.beforeFirst();
			while (in.gotoNextSequence()) {
				status.setCurrentStepProgress(Math.round(100f * ++currentSequence / numberOfSequences));
				//modify the header so that a description of the modification preceeds the original header
				currentHeader = in.getHeader();
				out.appendSequence(transformString(currentHeader), in.getSequence());
			}
			this.lastRunValidation.setCompletionCount(out.getSequenceCount());
			this.setLastRunCompletionCount(out.getSequenceCount());
		} catch (PatternSyntaxException e) {
			LOGGER.error(e);
			this.lastRunValidation.addMessageAndException("Error applying the transform to the header: " + currentHeader, e);
		} catch (IOException e) {
			LOGGER.error(e);
			this.lastRunValidation.addMessageAndException("Error in performing database IO", e);
		} catch (Exception e) {
			LOGGER.error(e);
			this.lastRunValidation.addMessageAndException(e.getMessage(), e);
		}

		return this.lastRunValidation;
	}

	/**
	 * takes a string and transforms it based on the set matchPattern and subsitutionPattern
	 *
	 * @param toTransform the header to transform
	 * @return the transformed header or the original header of the sequence didn't match the pattern
	 * @throws java.util.regex.PatternSyntaxException
	 *          if there was a problem performing the transformation
	 */
	public String transformString(final String toTransform) throws PatternSyntaxException {
		if (getMatchPattern() == null || getSubstitutionPattern() == null) {
			return toTransform;
		}

		if (compiledPattern == null || !compiledPattern.pattern().equals(this.getMatchPattern())) {
			compiledPattern = Pattern.compile(this.getMatchPattern());
		}

		StringBuffer result = new StringBuffer();

		Matcher match = compiledPattern.matcher(toTransform);

		while (match.find()) {
			match.appendReplacement(result, this.substitutionPattern);
		}
		match.appendTail(result);

		if (result.toString().equalsIgnoreCase(this.getSubstitutionPattern())) {
			LOGGER.info("Pattern not matched in header: " + toTransform);
			return toTransform;
		} else {
			return result.toString();
		}

	}

	/**
	 * Validates by make sure that the necessary properties (matchPattern, substitutionPattern) are set and that the matchPattern compiles.
	 * {@inheritDoc}
	 *
	 * @param curationDao
	 */
	public StepValidation preValidate(CurationDao curationDao) {
		StepValidation preValidation = new StepValidation();

		//if the pattern is not yet compiled as expected try to compile it
		if (compiledPattern == null || !compiledPattern.pattern().equals(getMatchPattern())) {
			try {
				compiledPattern = Pattern.compile(getMatchPattern());
			} catch (PatternSyntaxException e) {
				preValidation.addMessageAndException("Error compiling the regular expression: " + e.getMessage(), e);
			}
		}

		if (this.getSubstitutionPattern() == null) {
			preValidation.addMessageAndException("The subsitution pattern is not set", null);
		}

		return preValidation;
	}

	public StepValidation postValidate() {
		return lastRunValidation;
	}

	public CurationStep createCopy() {
		HeaderTransformStep copy = new HeaderTransformStep();
		copy.setDescription(this.getDescription());
		copy.setMatchPattern(this.getMatchPattern());
		copy.setSubstitutionPattern(this.getSubstitutionPattern());
		return copy;
	}

	/**
	 * a comment on what the transformation does to the header
	 *
	 * @return the description for the transformation
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * a comment on what the transformation does to the header
	 *
	 * @param description to apply to the transformation
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * the pattern that will be used to identify classes that need to be transformed
	 *
	 * @return the pattern that is used for identifying classes
	 */
	public String getMatchPattern() {
		return matchPattern;
	}

	/**
	 * set the pattern for identifying classes this should contain at least 2 classes besides 0th (entire string)
	 *
	 * @param matchPattern the pattern for identifying classes
	 */
	public void setMatchPattern(String matchPattern) {
		this.matchPattern = matchPattern;
	}

	/**
	 * the pattern that should be used to guide the transformation
	 *
	 * @return the pattern use to guide the transformation
	 * @see java.util.regex.Matcher#replaceAll to see the syntax for the patterns
	 */
	public String getSubstitutionPattern() {
		return substitutionPattern;
	}

	/**
	 * the pattern that should be used to guide the transformation
	 *
	 * @param substitutionPattern the pattern that should be used to guide the transformation
	 */
	public void setSubstitutionPattern(String substitutionPattern) {
		this.substitutionPattern = substitutionPattern;
	}

	public String simpleDescription() {
		return null;
	}

}
