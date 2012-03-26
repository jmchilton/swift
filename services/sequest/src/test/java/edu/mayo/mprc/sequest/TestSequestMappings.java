package edu.mayo.mprc.sequest;

import edu.mayo.mprc.swift.params2.mapping.MockParamsInfo;
import edu.mayo.mprc.swift.params2.mapping.TestMappingContextBase;
import edu.mayo.mprc.unimod.ModSet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test that Sequest mappings work correctly.
 *
 * @author Roman Zenka
 */
public final class TestSequestMappings {
	private SequestMappings sequestMappings;
	private SequestContext context;

	/**
	 * Setup the environment for the test.
	 */
	@BeforeClass
	public void setup() {
		final SequestMappingFactory mappingFactory = new SequestMappingFactory();
		sequestMappings = (SequestMappings) mappingFactory.createMapping();
		context = new SequestContext();
	}

	@Test
	public void shouldMap() {
		final ModSet modSet = new ModSet();
		addMods(modSet, "Carbamidomethyl (C)");
		sequestMappings.setFixedMods(context, modSet);
		Assert.assertEquals(sequestMappings.getNativeParam("add_C_Cysteine"), "57.021464", "Cysteine did not map");
	}

	/**
	 * @param modSet List of mods to add modifications to.
	 * @param mods   Mascot names of mods to add in "mod_name(residue)" format.
	 */
	public void addMods(final ModSet modSet, final String... mods) {
		for (final String mod : mods) {
			modSet.addAll(context.getAbstractParamsInfo().getUnimod().getSpecificitiesByMascotName(mod));
		}
	}

	/**
	 * Fail if anything unusual happens.
	 */
	private static final class SequestContext extends TestMappingContextBase {
		/**
		 * Create basic context with mocked parameter info.
		 */
		public SequestContext() {
			super(new MockParamsInfo());
		}

		@Override
		public void reportError(final String message, final Throwable t) {
			Assert.fail(message, t);
		}

		@Override
		public void reportWarning(final String message) {
			Assert.fail(message);
		}
	}

}
