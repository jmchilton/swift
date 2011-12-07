package edu.mayo.mprc.mascot;

import edu.mayo.mprc.swift.params2.mapping.MappingFactory;
import edu.mayo.mprc.swift.params2.mapping.Mappings;
import edu.mayo.mprc.swift.params2.mapping.ParamsInfo;

public final class MascotMappingFactory implements MappingFactory {
	private static final long serialVersionUID = 20121221L;
	public static final String MASCOT = "MASCOT";

	public MascotMappingFactory(ParamsInfo abstractParamsInfo) {
		this.abstractParamsInfo = abstractParamsInfo;
	}

	private ParamsInfo abstractParamsInfo;

	@Override
	public String getSearchEngineCode() {
		return MASCOT;
	}

	public String getCanonicalParamFileName() {
		return "mascot.params";
	}

	public Mappings createMapping() {
		return new MascotMappings(abstractParamsInfo);
	}

}

