package edu.mayo.mprc.msconvert;

import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Reader;

/**
 * Check that we can correctly determine whether MS2 spectra are in profile mode using msaccess.
 *
 * @author Roman Zenka
 */
public final class TestMsAccessParse {
	@Test
	public void shouldDetectProfileMs2() {
		Assert.assertTrue(checkFile("qe1_2012oct10_10_postk4_blank2.raw.metadata.txt"), "QE1 has profile-mode MS2");
		Assert.assertTrue(checkFile("kj_qe2_2012sep26_12c_phe_inf_us5.RAW.metadata.txt"), "QE2 has profile-mode MS2");
		Assert.assertFalse(checkFile("Lieske_CW_090712_3.raw.metadata.txt"), "Orbitrap Elite has stick MS2");
		Assert.assertFalse(checkFile("o63_12aug03_05_uc_blank4.RAW.metadata.txt"), "Orbitrap has stick MS2");
		Assert.assertFalse(checkFile("testLarge1.RAW.metadata.txt"), "Orbitrap with MS3 data has stick MS2");
	}

	@Test
	public void shouldFailBrokenIndent() {
		try {
			checkFile("corrupted.metadata.txt");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("line 23"), "The exception has to indicate correct line #");
			return;
		}
		Assert.fail("The corrupted file was not detected");
	}

	@Test
	public void shouldFailMissingAnalyzer() {
		try {
			checkFile("corrupted.metadata_missing_analyzer.txt");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("analyzer"), "The exception has to indicate that analyzer section is missing.");
			return;
		}
		Assert.fail("The corrupted file was not detected");
	}


	private boolean checkFile(final String file) {
		final Reader reader = ResourceUtilities.getReader("classpath:edu/mayo/mprc/msconvert/" + file, getClass());
		try {
			final MsaccessMetadataParser parser = new MsaccessMetadataParser(reader);
			parser.process();
			return parser.isOrbitrapForMs2();
		} finally {
			FileUtilities.closeQuietly(reader);
		}
	}
}
