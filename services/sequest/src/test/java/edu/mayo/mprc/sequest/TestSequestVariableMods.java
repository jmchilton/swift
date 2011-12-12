package edu.mayo.mprc.sequest;

import edu.mayo.mprc.swift.params2.mapping.MockParamsInfo;
import edu.mayo.mprc.swift.params2.mapping.TestMappingContextBase;
import edu.mayo.mprc.unimod.ModSet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Roman Zenka
 */
public class TestSequestVariableMods {
	private SequestMappings sequestMappings;
	private SequestContext context;
	private ModSet variableMods;

	/**
	 * Setup the environment for the test.
	 */
	@BeforeClass
	public void setup() {
		SequestMappingFactory mappingFactory = new SequestMappingFactory();
		sequestMappings = (SequestMappings) mappingFactory.createMapping();
		context = new SequestContext();
	}

	/**
	 * Reset the mapping context and mappings for the next test.
	 */
	@BeforeMethod
	public void reset() {
		context.setExpectedWarning(null);
		resetMappings();
		variableMods = new ModSet();
	}

	/**
	 * Take three simple mods, set them to Sequest, check everything is ok.
	 */
	@Test
	public void shouldMapSimpleCase() {
		Assert.assertEquals(getVariableMods(), "15.99492 M 57.02146 C 0.000000 X 0.000000 T 0.000000 Y 0.000000 X");

		addMods(variableMods, "Diisopropylphosphate(S)", "GluGlu(E)", "Oxidation(P)");

		sequestMappings.mapVariableModsToNative(context, variableMods);

		Assert.assertEquals(getVariableMods(), "164.060231 S 258.085186 E 15.994915 P 0.0000 X 0.0000 X 0.0000 X");
	}

	private String getVariableMods() {
		return sequestMappings.getNativeParam(SequestMappings.VAR_MODS);
	}

	/**
	 * Take seven phosphorylation mods, see that we get a warning + only six get mapped.
	 */
	@Test
	public void skipSeventhMod() {
		addMods(variableMods, "Phospho(C)", "Phospho(D)", "Phospho(H)", "Phospho(R)", "Phospho(S)", "Phospho(T)", "Phospho(Y)");

		// Last mod (lexicographically) will get skipped
		context.setExpectedWarning("Sequest supports up to 6 variable modifications, skipping Phospho (Y)");
		sequestMappings.mapVariableModsToNative(context, variableMods);

		Assert.assertEquals(getVariableMods(), "79.966331 C 79.966331 D 79.966331 H 79.966331 R 79.966331 S 79.966331 T");
	}

	/**
	 * Take eight mods, make sure two last get skipped
	 */
	@Test
	public void skipTwoLastMods() {
		addMods(variableMods, "Oxidation(M)", "Phospho(C)", "Phospho(D)", "Phospho(H)", "Phospho(R)", "Phospho(S)", "Phospho(T)", "Phospho(Y)");

		// Last mod (lexicographically) will get skipped
		context.setExpectedWarning("Sequest supports up to 6 variable modifications, skipping Phospho (T), Phospho (Y)");
		sequestMappings.mapVariableModsToNative(context, variableMods);

		Assert.assertEquals(getVariableMods(), "15.994915 M 79.966331 C 79.966331 D 79.966331 H 79.966331 R 79.966331 S");
	}

	private void resetMappings() {
		sequestMappings.read(sequestMappings.baseSettings());
	}

	/**
	 * @param modSet List of mods to add modifications to.
	 * @param mods   Mascot names of mods to add in "mod_name(residue)" format.
	 */
	private void addMods(ModSet modSet, String... mods) {
		for (String mod : mods) {
			modSet.addAll(context.getAbstractParamsInfo().getUnimod().getSpecificitiesByMascotName(mod));
		}
	}

	public static final class SequestContext extends TestMappingContextBase {
		private String expectedWarning;

		public void setExpectedWarning(String expectedWarning) {
			this.expectedWarning = expectedWarning;
		}

		/**
		 * Create basic context with mocked parameter info.
		 */
		public SequestContext() {
			super(new MockParamsInfo());
		}

		@Override
		public void reportWarning(String message) {
			Assert.assertEquals(message, expectedWarning, "Got unexpected warning");
		}
	}
}
