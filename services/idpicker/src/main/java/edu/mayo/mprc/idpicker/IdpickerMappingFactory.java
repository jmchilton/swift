package edu.mayo.mprc.idpicker;

import edu.mayo.mprc.swift.params2.mapping.MappingFactory;
import edu.mayo.mprc.swift.params2.mapping.Mappings;

/**
 * @author Roman Zenka
 */
public final class IdpickerMappingFactory implements MappingFactory {

	private static final long serialVersionUID = 20110711L;
	public static final String MYRIMATCH = "MYRIMATCH";

	@Override
	public Mappings createMapping() {
		return new IdpickerMappings();
	}

	@Override
	public String getSearchEngineCode() {
		return MYRIMATCH;
	}

	/**
	 * @return Typical name for the param file storing parameters for this mapping.
	 */
	@Override
	public String getCanonicalParamFileName() {
		return "myrimatch.cfg";
	}
}
