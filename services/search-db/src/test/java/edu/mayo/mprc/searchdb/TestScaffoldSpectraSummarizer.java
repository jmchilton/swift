package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.searchdb.dao.*;
import edu.mayo.mprc.unimod.MockUnimodDao;
import edu.mayo.mprc.unimod.Unimod;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;
import edu.mayo.mprc.utilities.TestingUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Reader;

/**
 * Tests the summary report for the scaffold spectra file.
 *
 * @author Roman Zenka
 */
public class TestScaffoldSpectraSummarizer {

	private static final String SINGLE = "classpath:edu/mayo/mprc/searchdb/single.tsv";
	private static final double EPSILON = 1E-6;

	/**
	 * Should load a report generated from single file experiment.
	 */
	@Test
	public void shouldLoadSingleReport() {
		final Reader reader = ResourceUtilities.getReader(SINGLE, TestScaffoldSpectraSummarizer.class);
		try {
			MockUnimodDao mockUnimodDao = new MockUnimodDao();
			final Unimod unimod = mockUnimodDao.load();
			ScaffoldSpectraSummarizer summarizer = new ScaffoldSpectraSummarizer(unimod);
			summarizer.load(reader, SINGLE, "3");
			final Analysis analysis = summarizer.getAnalysis();
			Assert.assertEquals(analysis.getAnalysisDate(), TestingUtilities.getDate(2011, 12, 16), "Report date");
			Assert.assertEquals(analysis.getScaffoldVersion(), "Scaffold_3.3.1", "Scaffold version");

			Assert.assertEquals(analysis.getBiologicalSamples().size(), 1, "Biological samples");

			final BiologicalSample biologicalSample = analysis.getBiologicalSamples().get(0);
			Assert.assertEquals(biologicalSample.getSampleName(), "test1", "Sample name");
			Assert.assertEquals(biologicalSample.getSearchResults().size(), 1, "Total msms searches");

			final SearchResult searchResult = biologicalSample.getSearchResults().get(0);
			Assert.assertEquals(searchResult.getProteinGroups().size(), 5, "Total protein groups");
			final ProteinGroup teraBovinGroup = searchResult.getProteinGroups().get(3);
			Assert.assertEquals(teraBovinGroup.getNumberOfTotalSpectra(), 26, "Total spectra");
			Assert.assertEquals(teraBovinGroup.getNumberOfUniquePeptides(), 10, "Unique peptides");
			Assert.assertEquals(teraBovinGroup.getNumberOfUniqueSpectra(), 13, "Unique spectra");
			assertNear(teraBovinGroup.getPercentageSequenceCoverage(), 0.145, "Sequence coverage");
			assertNear(teraBovinGroup.getPercentageOfTotalSpectra(), 0.351, "Percentage total");
			assertNear(teraBovinGroup.getProteinIdentificationProbability(), 1.0, "Id probability");
			Assert.assertEquals(teraBovinGroup.getProteinSequences().size(), 1, "One protein only");
			Assert.assertEquals(teraBovinGroup.getPeptideSpectrumMatches().size(), 10, "Peptides assigned to protein");
			final PeptideSpectrumMatch firstPsm = teraBovinGroup.getPeptideSpectrumMatches().get(0);
			Assert.assertEquals(firstPsm.getPeptide().getSequence().getSequence(), "AHVIVMAATNRPNSIDPALR");
			Assert.assertEquals(firstPsm.getPeptide().getModifications().size(), 0);
			Assert.assertEquals(firstPsm.getSpectrumIdentificationCounts().getNumberOfIdentifiedSpectra(), 3);
			Assert.assertEquals(firstPsm.getSpectrumIdentificationCounts().getNumberOfIdentified1HSpectra(), 0);
			Assert.assertEquals(firstPsm.getSpectrumIdentificationCounts().getNumberOfIdentified2HSpectra(), 1);
			Assert.assertEquals(firstPsm.getSpectrumIdentificationCounts().getNumberOfIdentified3HSpectra(), 2);
			Assert.assertEquals(firstPsm.getSpectrumIdentificationCounts().getNumberOfIdentified4HSpectra(), 0);

			final String s = analysis.peptideReport();
			// TODO check against proper report
		} finally {
			FileUtilities.closeQuietly(reader);
		}
	}

	private static void assertNear(double d1, double d2, String message) {
		final double delta = d1 - d2;
		Assert.assertTrue(-EPSILON < delta && delta < EPSILON, message);
	}

}
