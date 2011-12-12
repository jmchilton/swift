package edu.mayo.mprc.mascot;

import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.dbcurator.model.persistence.MockCurationDao;
import edu.mayo.mprc.swift.params2.MockParamsDao;
import edu.mayo.mprc.swift.params2.ParamsDao;
import edu.mayo.mprc.swift.params2.mapping.*;
import edu.mayo.mprc.unimod.*;
import edu.mayo.mprc.utilities.ResourceUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Reader;
import java.io.StringWriter;

public final class TestMascotMappings {

	@Test
	public void shouldSupportPhosphoST() {
		ParamsInfo abstractParamsInfo = getAbstractParamsInfo();
		MascotMappingFactory mappingFactory = new MascotMappingFactory(abstractParamsInfo);
		final Mappings mapping = mappingFactory.createMapping();
		final Unimod unimod = abstractParamsInfo.getUnimod();
		mapping.read(getMascotParamReader());

		MappingContext context = new PhosphoStContext(abstractParamsInfo);

		final ModSpecificity phosphoS = unimod.getSpecificitiesByMascotName("Phospho (S)").get(0);
		ModSet set = new ModSet();
		set.add(phosphoS);
		mapping.mapFixedModsToNative(context, set);

		final ModSpecificity phosphoT = unimod.getSpecificitiesByMascotName("Phospho (T)").get(0);
		set.add(phosphoT);
		mapping.mapFixedModsToNative(context, set);

		final String output = mappingsToString(mapping);

		Assert.assertTrue(output.contains("MODS=Phospho (ST)\n"), "The mods do not match");
	}

	@Test
	public void shouldSupportDeamidatedNTerm() {
		ParamsInfo abstractParamsInfo = getAbstractParamsInfo();
		MascotMappingFactory mappingFactory = new MascotMappingFactory(abstractParamsInfo);
		final Mappings mapping = mappingFactory.createMapping();
		final Unimod unimod = abstractParamsInfo.getUnimod();
		mapping.read(getMascotParamReader());

		MappingContext context = new PhosphoStContext(abstractParamsInfo);

		final ModSpecificity deamidated = unimod.getSpecificitiesByMascotName("Deamidated (Protein N-term F)").get(0);
		final ModSet modSet = new ModSet();
		modSet.add(deamidated);
		mapping.mapFixedModsToNative(context, modSet);

		final String output = mappingsToString(mapping);

		Assert.assertTrue(output.contains("MODS=Deamidated (Protein N-term F)\n"), "The mods do not match");
	}

	public static ParamsInfo getAbstractParamsInfo() {
		CurationDao curationDao = new MockCurationDao();
		UnimodDao unimodDao = new MockUnimodDao();
		ParamsDao paramsDao = new MockParamsDao();
		return new ParamsInfoImpl(curationDao, unimodDao, paramsDao);
	}

	private static String mappingsToString(Mappings mapping) {
		final StringWriter writer = new StringWriter(1000);
		mapping.write(getMascotParamReader(), writer);
		return writer.toString();
	}

	private static Reader getMascotParamReader() {
		return ResourceUtilities.getReader("classpath:edu/mayo/mprc/swift/params/Orbitrap_Sprot_Latest_CarbC_OxM/mascot.params", TestMascotMappings.class);
	}

	private static final class PhosphoStContext extends TestMappingContextBase {
		private PhosphoStContext(ParamsInfo abstractParamsInfo) {
			super(abstractParamsInfo);
		}

		@Override
		public void reportWarning(String message) {
			Assert.assertTrue(message.matches("Mascot will search additional site \\([ST]\\) for modification Phospho \\([ST]\\)"), "Unexpected warning");
		}
	}
}
