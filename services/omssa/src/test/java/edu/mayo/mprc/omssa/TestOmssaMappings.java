package edu.mayo.mprc.omssa;

import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.dbcurator.model.persistence.MockCurationDao;
import edu.mayo.mprc.swift.params2.MockParamsDao;
import edu.mayo.mprc.swift.params2.mapping.*;
import edu.mayo.mprc.unimod.MockUnimodDao;
import edu.mayo.mprc.unimod.ModSet;
import edu.mayo.mprc.unimod.ModSpecificity;
import edu.mayo.mprc.unimod.Unimod;
import edu.mayo.mprc.utilities.ResourceUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Reader;
import java.io.StringWriter;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TestOmssaMappings {

	private static final Pattern ONE_MOD_PATTERN = Pattern.compile("\\Q<MSSearchSettings_fixed>\\E\\s*" +
			"\\Q<MSMod value=\"usermod3\">121</MSMod>\\E\\s*" +
			"\\Q</MSSearchSettings_fixed>\\E");

	@Test
	public void shouldSupportLargeAmountOfMods() {
		CurationDao dao = new MockCurationDao();
		ParamsInfo abstractParamsInfo = new ParamsInfoImpl(dao, new MockUnimodDao(), new MockParamsDao());
		OmssaMappingFactory mappingFactory = new OmssaMappingFactory();
		final Mappings mapping = mappingFactory.createMapping();
		final Unimod unimod = abstractParamsInfo.getUnimod();
		mapping.read(getOmssaParamStream());

		MappingContext context = new TestMappingContextBase(abstractParamsInfo);

		final Set<ModSpecificity> allSpecs = unimod.getAllSpecificities(false);
		int i = 0;
		for (ModSpecificity modSpecificity : allSpecs) {
			ModSet set = new ModSet();
			set.add(modSpecificity);
			mapping.setFixedMods(context, set);
			i++;
			if (i >= 50) {
				break;
			}
		}

		final String output = mappingsToString(mapping);
		final Matcher matcher = ONE_MOD_PATTERN.matcher(output);

		Assert.assertTrue(matcher.find(), "The mods do not match");
	}

	private static String mappingsToString(Mappings mapping) {
		final StringWriter writer = new StringWriter(1000);
		mapping.write(getOmssaParamStream(), writer);
		return writer.toString();
	}

	private static Reader getOmssaParamStream() {
		return ResourceUtilities.getReader("classpath:edu/mayo/mprc/swift/params/Orbitrap_Sprot_Latest_CarbC_OxM/omssa.params.xml", TestOmssaMappings.class);
	}
}
