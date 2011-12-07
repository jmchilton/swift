package edu.mayo.mprc.sequest;

import edu.mayo.mprc.swift.params2.mapping.MappingFactory;
import edu.mayo.mprc.swift.params2.mapping.Mappings;

public final class SequestMappingFactory implements MappingFactory {
	private static final long serialVersionUID = 20101221L;
	public static final String SEQUEST = "SEQUEST";

	public Mappings createMapping() {
		return new SequestMappings();
	}

	@Override
	public String getSearchEngineCode() {
		return SEQUEST;
	}

	public String getCanonicalParamFileName() {
		return "sequest.params";
	}
}

