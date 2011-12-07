package edu.mayo.mprc.swift.params2.mapping;

import java.io.Serializable;

/**
 * Search engine mapping factory interface. Produces {@link Mappings}
 */
public interface MappingFactory extends Serializable {
	/**
	 * @return String code of the search engine.
	 */
	String getSearchEngineCode();

	/**
	 * @return Typical name for the param file storing parameters for this mapping.
	 */
	String getCanonicalParamFileName();

	Mappings createMapping();
}
