package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.scafml.ScafmlExport;
import edu.mayo.mprc.searchdb.dao.Analysis;
import edu.mayo.mprc.searchdb.dao.BiologicalSample;
import edu.mayo.mprc.searchdb.dao.TandemMassSpectrometrySample;
import edu.mayo.mprc.utilities.ResourceUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Checks we get correct data extraction from .XML files exported by Scaffold via
 * {@link ScafmlExport#appendScaffoldXmlExport}.
 *
 * @author Roman Zenka
 */
public class TestScaffoldXmlExtractor {

	private static final String[] FILE = new String[]{
			"BR05-17087_King_20090922_PreS1",
			"BR05-17087_King_20090922_S1",
			"BR05-17087_King_20090922_S2",
			"BR05-17087_King_20090922_S3"
	};

	/**
	 * Check that we get a good summary on a Scaffold output for a single .RAW file.
	 *
	 * @throws IOException Test file could not be opened.
	 */
	@Test
	public void shouldSummarizeSimpleFile() throws IOException {
		MassSpecDataExtractor extractor = mock(MassSpecDataExtractor.class);
		when(extractor.getTandemMassSpectrometrySample("test1", "test1"))
				.thenReturn(getSample("test1.mgf"));

		final Analysis analysis = load("classpath:edu/mayo/mprc/searchdb/scaffold.xml", extractor);
		checkVersion(analysis, "Scaffold_2.06.00-mayo");
		checkDate(analysis, "2011-12-21 16:23:48 CST");
		final List<BiologicalSample> samples = analysis.getBiologicalSamples();
		Assert.assertEquals(samples.size(), 1);
		Assert.assertEquals(samples.get(0).getSampleName(), "test1");

		verify(extractor).getTandemMassSpectrometrySample("test1", "test1");
		verifyNoMoreInteractions(extractor);
	}

	/**
	 * Check we can summarize old version of the format with multiple .RAW files.
	 *
	 * @throws IOException Test file could not be opened.
	 */
	@Test
	public void shouldSummarizeMultipleRaw() throws IOException {
		MassSpecDataExtractor extractor = mock(MassSpecDataExtractor.class);
		for (String file : FILE) {
			when(extractor.getTandemMassSpectrometrySample(file, file))
					.thenReturn(getSample(file + ".mgf"));
		}

		final Analysis analysis = load("classpath:edu/mayo/mprc/searchdb/scaffold2.xml", extractor);
		checkVersion(analysis, "@@CVS_TAG@@");
		checkDate(analysis, "2009-10-05 15:06:58 CDT");
		final List<BiologicalSample> samples = analysis.getBiologicalSamples();
		Assert.assertEquals(samples.size(), 4);

		for (int i = 0; i < FILE.length; i++) {
			final String file = FILE[i];
			Assert.assertEquals(samples.get(i).getSampleName(), file);
			verify(extractor).getTandemMassSpectrometrySample(file, file);
		}
		verifyNoMoreInteractions(extractor);
	}

	private TandemMassSpectrometrySample getSample(String fileName) {
		final TandemMassSpectrometrySample sample = new TandemMassSpectrometrySample();
		sample.setFile(new File(fileName));
		return sample;
	}

	private Analysis load(String path, MassSpecDataExtractor extractor) {
		ScaffoldXmlExtractor summarizer = new ScaffoldXmlExtractor();
		summarizer.setDataExtractor(extractor);
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
