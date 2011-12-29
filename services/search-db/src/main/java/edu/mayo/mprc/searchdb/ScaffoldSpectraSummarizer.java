package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.scaffoldparser.spectra.AbstractScaffoldSpectraReader;

import java.io.File;
import java.io.Reader;

/**
 * @author Roman Zenka
 */
public class ScaffoldSpectraSummarizer extends AbstractScaffoldSpectraReader {
	/**
	 * Summarize a scaffold spectra file.
	 *
	 * @param scaffoldSpectraFile Scaffold file to summarize.
	 * @param scaffoldVersion     See {@link AbstractScaffoldSpectraReader#scaffoldVersion}
	 */
	public ScaffoldSpectraSummarizer(File scaffoldSpectraFile, String scaffoldVersion) {
		super(scaffoldSpectraFile, scaffoldVersion);
	}

	public ScaffoldSpectraSummarizer(Reader reader, String dataSourceName, String scaffoldVersion) {
		super(reader, dataSourceName, scaffoldVersion);
	}

	@Override
	public void processHeader(String line) {
	}

	@Override
	public void processRow(String line) {
	}
}
