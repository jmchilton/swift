package edu.mayo.mprc.idpicker;

import edu.mayo.mprc.swift.params2.Instrument;
import edu.mayo.mprc.swift.params2.Protease;
import edu.mayo.mprc.swift.params2.Tolerance;
import edu.mayo.mprc.swift.params2.mapping.MappingContext;
import edu.mayo.mprc.swift.params2.mapping.Mappings;
import edu.mayo.mprc.unimod.ModSet;

import java.io.Reader;
import java.io.Writer;

/**
 * @author Roman Zenka
 */
public final class IdpickerMappings implements Mappings {
	@Override
	public Reader baseSettings() {
		IdpQonvertSettings settings = new IdpQonvertSettings();
		return null;
	}

	@Override
	public void read(Reader isr) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void write(Reader oldParams, Writer out) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setPeptideTolerance(MappingContext context, Tolerance peptideTolerance) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setFragmentTolerance(MappingContext context, Tolerance fragmentTolerance) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setVariableMods(MappingContext context, ModSet variableMods) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setFixedMods(MappingContext context, ModSet fixedMods) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setSequenceDatabase(MappingContext context, String shortDatabaseName) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setProtease(MappingContext context, Protease protease) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setMissedCleavages(MappingContext context, Integer missedCleavages) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setInstrument(MappingContext context, Instrument instrument) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getNativeParam(String name) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setNativeParam(String name, String value) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
