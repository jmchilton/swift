package edu.mayo.mprc.peaks;

import com.google.common.collect.ImmutableMap;
import edu.mayo.mprc.chem.AminoAcidSet;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.peaks.core.PeaksSearchParameters;
import edu.mayo.mprc.swift.params2.ParamName;
import edu.mayo.mprc.swift.params2.mapping.MappingContext;
import edu.mayo.mprc.swift.params2.mapping.ParamsInfo;
import edu.mayo.mprc.unimod.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashSet;

public final class PeaksMappingsTest {

	private static final Logger LOGGER = Logger.getLogger(PeaksMappingsTest.class);

	private PeaksMappingFactory peaksMappingFactory;
	private MappingContext context;

	private PeaksMappings peaksMappings;

	@BeforeClass
	public void init() {
		peaksMappingFactory = new PeaksMappingFactory();
		peaksMappingFactory.setEnzymeMapping(
				new ImmutableMap.Builder<String, String>()
						.put("Arg-C", "Arg C")
						.put("Arg-N", "Arg N")
						.put("Arg-N_ambic", "Asp N + N-terminal Glu")
						.put("Chymotrypsin", "Chymotrysin")
						.put("CNBr", "CNBr")
						.put("Lys-C (allow P)", "Lys C")
						.put("PepsinA", "Pepsin (pH 1.3)")
						.put("Trypsin (restrict P)", "Trypsin")
						.build());

		peaksMappingFactory.setInstrumentMapping(
				new ImmutableMap.Builder<String, String>()
						.put("Orbi/FT (ESI-FTICR)", "FT-trap")
						.put("LTQ (ESI-TRAP)", "Quad linear-trap")
						.put("ECD (FTMS-ECD)", "FTMS(ecd)")
						.put("4800 (MALDI-TOF-TOF)", "TOF-TOF").build());

		peaksMappings = (PeaksMappings) peaksMappingFactory.createMapping();
		context = new TestMappingContext();
	}

	public static void addMod(final ModSet set, final double massMono, final String site, final String position) {
		String siteChar;
		final Terminus terminus;
		siteChar = String.valueOf(site.charAt(0));
		if (position.endsWith("N-term")) {
			if (site.contains("term")) {
				siteChar = "*";
			}
			terminus = Terminus.Nterm;
		} else if (position.endsWith("C-term")) {
			if (site.contains("term")) {
				siteChar = "*";
			}
			terminus = Terminus.Cterm;
		} else {
			terminus = Terminus.Anywhere;
		}
		final boolean proteinOnly = ModSpecificity.POSITION_PROTEIN_C_TERM.equals(position) || ModSpecificity.POSITION_PROTEIN_N_TERM.equals(position);
		final Mod modification = new Mod("Test mod " + massMono, "Test mod full name", 1, massMono, massMono, "C2H3O4", new HashSet<String>(),
				new SpecificityBuilder("*".equals(siteChar) ? null : AminoAcidSet.DEFAULT.getForSingleLetterCode(siteChar), terminus,
						proteinOnly,
						false, "classification", 1));
		set.add(modification.getModSpecificities().iterator().next());
	}

	@Test
	public void mapVariableModsToNativeTest() {
		final ModSet variableMods = new ModSet();

		addMod(variableMods, 123.5, "A", ModSpecificity.POSITION_ANY_N_TERM);
		addMod(variableMods, 123.5, "G", ModSpecificity.POSITION_ANY_N_TERM);
		addMod(variableMods, 123.5, "W", ModSpecificity.POSITION_ANY_N_TERM);
		addMod(variableMods, 123.5, "Y", ModSpecificity.POSITION_ANY_C_TERM);
		addMod(variableMods, 123.5, "N", ModSpecificity.POSITION_PROTEIN_C_TERM);
		addMod(variableMods, 123.5, "L", ModSpecificity.POSITION_ANYWHERE);
		addMod(variableMods, 123.5, "D", ModSpecificity.POSITION_ANYWHERE);
		addMod(variableMods, 200.5, "N-term", ModSpecificity.POSITION_ANY_N_TERM);
		addMod(variableMods, 200.5, "G", ModSpecificity.POSITION_ANY_N_TERM);
		addMod(variableMods, 200.5, "A", ModSpecificity.POSITION_ANY_C_TERM);
		addMod(variableMods, 400.0, "G", ModSpecificity.POSITION_ANY_C_TERM);
		addMod(variableMods, 400.0, "G", ModSpecificity.POSITION_ANYWHERE);

		peaksMappings.setVariableMods(context, variableMods);

		final String peaksParameterValue = "123.5@DL:A,123.5@NY:C,123.5@AGW:N,200.5@A:C,200.5@ABCDEFGHIKLMNPQRSTVWXYZ:N,400.0@G:A,400.0@G:C";

		Assert.assertEquals(peaksMappings.getNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_INPUTVARIABLEMODIES), peaksParameterValue, "Varaible modifications did not match.");
	}

	@Test(dependsOnMethods = {"mapVariableModsToNativeTest"})
	public void mapFixedModsToNativeTest() {
		final ModSet fixedMods = new ModSet();

		addMod(fixedMods, 123.5, "A", ModSpecificity.POSITION_ANY_N_TERM);
		addMod(fixedMods, 123.5, "G", ModSpecificity.POSITION_ANY_N_TERM);
		addMod(fixedMods, 123.5, "W", ModSpecificity.POSITION_ANY_N_TERM);
		addMod(fixedMods, 123.5, "Y", ModSpecificity.POSITION_ANY_C_TERM);
		addMod(fixedMods, 123.5, "Q", ModSpecificity.POSITION_PROTEIN_C_TERM);
		addMod(fixedMods, 200.5, "C-term", ModSpecificity.POSITION_ANY_C_TERM);
		addMod(fixedMods, 200.5, "G", ModSpecificity.POSITION_ANY_C_TERM);
		addMod(fixedMods, 200.5, "A", ModSpecificity.POSITION_ANY_N_TERM);
		addMod(fixedMods, 400.0, "G", ModSpecificity.POSITION_ANY_C_TERM);
		addMod(fixedMods, 400.0, "Q", ModSpecificity.POSITION_PROTEIN_C_TERM);
		addMod(fixedMods, 400.0, "E", ModSpecificity.POSITION_PROTEIN_C_TERM);

		peaksMappings.setFixedMods(context, fixedMods);

		final String fixed = "123.5@Y:C,123.5@AGW:N,200.5@ABCDEFGHIKLMNPQRSTVWXYZ:C,200.5@A:N,400.0@G:C";

		Assert.assertEquals(peaksMappings.getNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_INPUTFIXEDMODIES), fixed, "Fixed modifications did not match.");

		final String variable = "123.5@DL:A,123.5@NQY:C,123.5@AGW:N,200.5@A:C,200.5@ABCDEFGHIKLMNPQRSTVWXYZ:N,400.0@G:A,400.0@EGQ:C";

		Assert.assertEquals(peaksMappings.getNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_INPUTVARIABLEMODIES), variable, "Variable modifications did not match.");
	}

	class TestMappingContext implements MappingContext {

		public ParamsInfo getAbstractParamsInfo() {
			return null;
		}

		public void startMapping(final ParamName paramName) {
			// Do nothing
		}

		public void reportError(final String message, final Throwable t) {
			LOGGER.log(Level.ERROR, message, t);
		}

		public void reportWarning(final String message) {
			LOGGER.info(message);
		}

		public void reportInfo(final String message) {
			LOGGER.info(message);
		}

		public boolean noErrors() {
			return false;
		}

		public Curation addLegacyCuration(final String legacyName) {
			return null;
		}
	}
}
