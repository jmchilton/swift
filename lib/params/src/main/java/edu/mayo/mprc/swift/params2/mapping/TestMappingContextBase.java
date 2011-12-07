package edu.mayo.mprc.swift.params2.mapping;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.swift.params2.ParamName;

public class TestMappingContextBase implements MappingContext {
	private ParamsInfo paramsInfo;

	/**
	 * Null Constructor
	 */
	public TestMappingContextBase(ParamsInfo paramsInfo) {
		this.paramsInfo = paramsInfo;
	}

	@Override
	public ParamsInfo getAbstractParamsInfo() {
		return paramsInfo;
	}

	@Override
	public void startMapping(ParamName paramName) {
		// Do nothing
	}

	@Override
	public void reportError(String message, Throwable t) {
		throw new MprcException(message, t);
	}

	@Override
	public void reportWarning(String message) {
		throw new MprcException(message);
	}

	@Override
	public void reportInfo(String message) {
		throw new MprcException(message);
	}

	@Override
	public boolean noErrors() {
		return true;
	}

	@Override
	public Curation addLegacyCuration(String legacyName) {
		return null;
	}
}
