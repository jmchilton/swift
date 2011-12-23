package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.scafml.ScafmlExport;
import edu.mayo.mprc.searchdb.dao.Analysis;
import edu.mayo.mprc.searchdb.dao.BiologicalSample;
import edu.mayo.mprc.utilities.ResourceUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Checks we get correct data extraction from .XML files exported by Scaffold via
 * {@link ScafmlExport#appendScaffoldXmlExport}.
 *
 * @author Roman Zenka
 */
public class TestScaffoldXmlExtractor {
	/**
	 * Check that we get a good summary on a Scaffold output for a single .RAW file.
	 *
	 * @throws IOException Test file could not be opened.
	 */
	@Test
	public void shouldSummarizeSimpleFile() throws IOException {
		final Analysis analysis = load("classpath:edu/mayo/mprc/searchdb/scaffold.xml");
		checkVersion(analysis, "Scaffold_2.06.00-mayo");
		checkDate(analysis, "2011-12-21 16:23:48 CST");
		final List<BiologicalSample> samples = analysis.getBiologicalSamples();
		Assert.assertEquals(samples.size(), 1);
		Assert.assertEquals(samples.get(0).getSampleName(), "test1");
	}

	/**
	 * Check we can summarize old version of the format with multiple .RAW files.
	 *
	 * @throws IOException Test file could not be opened.
	 */
	@Test
	public void shouldSummarizeMultipleRaw() throws IOException {
		final Analysis analysis = load("classpath:edu/mayo/mprc/searchdb/scaffold2.xml");
		checkVersion(analysis, "@@CVS_TAG@@");
		checkDate(analysis, "2009-10-05 15:06:58 CDT");
		final List<BiologicalSample> samples = analysis.getBiologicalSamples();
		Assert.assertEquals(samples.size(), 4);
		Assert.assertEquals(samples.get(3).getSampleName(), "BR05-17087_King_20090922_S3");
	}

	private Analysis load(String path) {
		ScaffoldXmlExtractor summarizer = new ScaffoldXmlExtractor();
		InputStream inputStream = ResourceUtilities.getStream(path, TestScaffoldXmlExtractor.class);
		return summarizer.load(inputStream);
	}

	private Analysis checkVersion(Analysis analysis, String expectedVersion) {
		Assert.assertEquals(analysis.getScaffoldVersion(), expectedVersion);
		return analysis;
	}

	private void checkDate(Analysis analysis, String dateStr) {
		try {
			final Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(dateStr);
			Assert.assertEquals(analysis.getAnalysisDate(), date);
		} catch (ParseException e) {
			throw new MprcException("Could not parse date " + dateStr, e);
		}
	}
}
