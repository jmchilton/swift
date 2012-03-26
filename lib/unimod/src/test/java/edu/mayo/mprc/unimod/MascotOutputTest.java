package edu.mayo.mprc.unimod;

import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.TestingUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public final class MascotOutputTest {

	@Test
	public void shouldConvertUnimod() throws IOException {
		final Unimod defaultUnimodSet = UnimodTest.getDefaultUnimodSet();
		final Set<ModSpecificity> allModSpecificities = defaultUnimodSet.getAllSpecificities(true);
		final StringBuilder builder = new StringBuilder();
		for (final ModSpecificity modSpecificity : allModSpecificities) {
			builder.append(modSpecificity.toMascotString()).append('\n');
		}
		File unimod = null;
		File myUnimod = null;

		final String s = builder.toString();
		try {
			unimod = TestingUtilities.getTempFileFromResource("/edu/mayo/mprc/unimod/unimod_mascot.txt", true, null);
			myUnimod = File.createTempFile("my_test", "unimod");
			FileUtilities.writeStringToFile(myUnimod, s, true);
			Assert.assertEquals(TestingUtilities.compareFilesByLine(myUnimod, unimod), null, "Unimod parsing differences");
		} finally {
			FileUtilities.quietDelete(unimod);
			FileUtilities.quietDelete(myUnimod);
		}

	}

}
