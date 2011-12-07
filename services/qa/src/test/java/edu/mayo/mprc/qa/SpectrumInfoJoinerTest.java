package edu.mayo.mprc.qa;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.mayo.mprc.msmseval.MSMSEvalOutputReader;
import edu.mayo.mprc.scaffoldparser.spectra.ScaffoldSpectraReader;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.TestingUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

public final class SpectrumInfoJoinerTest {

	@Test
	public void generateMgfStatisticsFileTest() throws IOException {

		File tempFolder = FileUtilities.createTempFolder();

		try {
			File mgfFile = TestingUtilities.getNamedFileFromResource("/edu/mayo/mprc/qa/test1.mgf", tempFolder);
			File scaffoldSpectra = TestingUtilities.getNamedFileFromResource("/edu/mayo/mprc/qa/scaffoldSpectra.txt", tempFolder);
			File referenceOutputFile = TestingUtilities.getNamedFileFromResource("/edu/mayo/mprc/qa/Out.tsv", tempFolder);
			File rawDumpFile = TestingUtilities.getNamedFileFromResource("/edu/mayo/mprc/qa/rawdump.tsv", tempFolder);
			File msmsEvalFile = TestingUtilities.getNamedFileFromResource("/edu/mayo/mprc/qa/msmsEval.csv", tempFolder);

			String refOut = Files.toString(referenceOutputFile, Charsets.UTF_8);
			refOut = refOut.replaceAll("<MGF>", Matcher.quoteReplacement(mgfFile.getAbsolutePath()));
			FileUtilities.writeStringToFile(referenceOutputFile, refOut, true);

			File outputFile = new File(tempFolder, "output.tsv");


			ScaffoldSpectraReader spectra = new ScaffoldSpectraReader(scaffoldSpectra, "2");
			RawDumpReader rawDumpReader = new RawDumpReader(rawDumpFile);
			MSMSEvalOutputReader msmsEvalReader = new MSMSEvalOutputReader(msmsEvalFile);

			SpectrumInfoJoiner.joinSpectrumData(mgfFile, spectra, rawDumpReader, msmsEvalReader, null, outputFile, null);

			Assert.assertEquals(TestingUtilities.compareFilesByLine(referenceOutputFile, outputFile, true), null, "Output file content is not as expected.");
		} finally {
			FileUtilities.cleanupTempFile(tempFolder);
		}
	}
}
