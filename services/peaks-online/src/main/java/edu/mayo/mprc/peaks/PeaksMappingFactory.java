package edu.mayo.mprc.peaks;

import edu.mayo.mprc.swift.params2.mapping.MappingFactory;
import edu.mayo.mprc.swift.params2.mapping.Mappings;

import java.util.Map;

public final class PeaksMappingFactory implements MappingFactory {
	private static final long serialVersionUID = 20101221L;

	//Mapping of Swift defined enzymes to Peaks defined enzymes.
	private Map<String, String> enzymeMapping;

	//Mapping of Swift defined instruments to Peaks defined instruments.
	private Map<String, String> instrumentMapping;
	public static final String PEAKS = "PEAKS";

	@Override
	public String getSearchEngineCode() {
		return PEAKS;
	}

	public String getCanonicalParamFileName() {
		return "peaks.params";
	}

	public Mappings createMapping() {
		return new PeaksMappings(enzymeMapping, instrumentMapping);
	}

	public Map<String, String> getEnzymeMapping() {
		return enzymeMapping;
	}

	public void setEnzymeMapping(final Map<String, String> enzymeMapping) {
		this.enzymeMapping = enzymeMapping;
	}

	public Map<String, String> getInstrumentMapping() {
		return instrumentMapping;
	}

	public void setInstrumentMapping(final Map<String, String> instrumentMapping) {
		this.instrumentMapping = instrumentMapping;
	}
}
