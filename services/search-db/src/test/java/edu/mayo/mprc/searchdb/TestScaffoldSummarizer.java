package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.utilities.ResourceUtilities;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Checks we get correct summaries for Scaffold files.
 *
 * @author Roman Zenka
 */
public class TestScaffoldSummarizer {
	/**
	 * Check that we get a good summary on a Scaffold output for a single .RAW file.
	 *
	 * @throws IOException Test file could not be opened.
	 */
	@Test
	public void shouldSummarizeSimpleFile() throws IOException {
		ScaffoldSummarizer summarizer = new ScaffoldSummarizer();
		InputStream inputStream = ResourceUtilities.getStream("classpath:edu/mayo/mprc/searchdb/scaffold.xml", TestScaffoldSummarizer.class);
		summarizer.load(inputStream);
	}
}
