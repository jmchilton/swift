package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;
import org.testng.annotations.Test;

import java.io.Reader;

/**
 * Tests the summary report for the scaffold spectra file.
 *
 * @author Roman Zenka
 */
public class TestScaffoldSpectraSummarizer {

	private static final String SINGLE = "classpath:edu/mayo/mprc/searchdb/single.tsv";

	/**
	 * Should load a report generated from single file experiment.
	 */
	@Test
	public void shouldLoadSingleReport() {
		final Reader reader = ResourceUtilities.getReader(SINGLE, TestScaffoldSpectraSummarizer.class);
		try {
			ScaffoldSpectraSummarizer summarizer = new ScaffoldSpectraSummarizer();
			summarizer.load(reader, SINGLE, "3");
		} finally {
			FileUtilities.closeQuietly(reader);
		}
	}
}
