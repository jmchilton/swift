package edu.mayo.mprc.swift.params2.mapping;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.swift.params2.ParamName;

/**
 * This context collects errors as the mappings progress and stores them in supplied {@link ParamsValidations}.
 */
public class ParamValidationsMappingContext implements MappingContext {
	private ParamsValidations validations;
	private ParamsInfo paramsInfo;
	private ParamName currentParam;
	private boolean noErrors = false;

	public ParamValidationsMappingContext(ParamsValidations validations, ParamsInfo paramsInfo) {
		this.validations = validations;
		this.paramsInfo = paramsInfo;
		if (paramsInfo == null) {
			throw new MprcException("The mapping context cannot be initialized with paramsInfo==null");
		}
	}

	public ParamsInfo getAbstractParamsInfo() {
		return paramsInfo;
	}

	public void reportError(String message, Throwable t) {
		Validation v = new Validation(message, ValidationSeverity.ERROR, currentParam, null, t);
		validations.addValidation(currentParam, v);
		noErrors = false;
	}

	public void reportWarning(String message) {
		Validation v = new Validation(message, ValidationSeverity.WARNING, currentParam, null, null);
		validations.addValidation(currentParam, v);
	}

	public void reportInfo(String message) {
		Validation v = new Validation(message, ValidationSeverity.INFO, currentParam, null, null);
		validations.addValidation(currentParam, v);
	}

	/**
	 * @return True if no errors occured since last call to a mapping method.
	 *         Use this if you want to do an action only if all mappings validated ok.
	 */
	public boolean noErrors() {
		return noErrors;
	}

	public Curation addLegacyCuration(String legacyName) {
		return null;
	}

	public void startMapping(ParamName paramName) {
		currentParam = paramName;
		noErrors = true;
	}
}
