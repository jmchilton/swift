package edu.mayo.mprc.scaffoldparser.spectra;

import com.google.common.base.Joiner;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.io.KeyedTsvReader;
import edu.mayo.mprc.utilities.StringUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A parser for Scaffold spectrum report - tab separated file with a line for each spectrum.
 * Loads all the data into memory so the access can be sped up.
 */
public final class ScaffoldSpectraReader implements Iterable<String>, KeyedTsvReader {

	/**
	 * Default extension - as Scaffold produces it.
	 */
	public static final String EXTENSION = ".spectra.txt";

	private Map<String/*spectrumName*/, String/*entire line about spectrum except spectrumName*/> mapSpectrumNameToScaffoldSpectraInfo = new HashMap<String, String>();
	private String[] header;
	private String emptyLine;
	private static final String SPECTRUM_NAME_COLUMN = "Spectrum name";
	private static final String EXPERIMENT_NAME_COLUMN = "Experiment name";
	// Scaffold files are terminated with this marker
	private static final String END_OF_FILE = "END OF FILE";
	private static final String STARRED_COLUMN = "Starred";
	private String scaffoldVersion;

	public ScaffoldSpectraReader(File scaffoldSpectraFile, String scaffoldVersion) {
		this.scaffoldVersion = scaffoldVersion;
		try {
			BufferedReader br = new BufferedReader(new FileReader(scaffoldSpectraFile));
			// Skip the header portion of the file, process the header line
			String line;
			while (true) {
				line = br.readLine();
				if (line == null) {
					throw new MprcException("End of file reached before we could find the header line in Scaffold spectra file [" + scaffoldSpectraFile.getAbsolutePath() + "].");
				}
				if (line.startsWith(EXPERIMENT_NAME_COLUMN + "\t")) {
					break;
				}
			}

			int spectrumNameColumn = processHeader(scaffoldSpectraFile, line);
			loadContents(scaffoldSpectraFile, br, spectrumNameColumn);
		} catch (Exception t) {
			throw new MprcException("Cannot parse Scaffold spectra file [" + scaffoldSpectraFile.getAbsolutePath() + "].", t);
		}
	}

	private void loadContents(File file, BufferedReader reader, int spectrumNameColumn) throws IOException {
		String line;
		StringBuilder sb = new StringBuilder(1000);
		while (true) {
			line = reader.readLine();
			if (line == null) {
				throw new MprcException("End of file reached before finding Scaffold's " + END_OF_FILE + " marker [" + file.getAbsolutePath() + "].");
			}
			if (END_OF_FILE.equals(line)) {
				break;
			}
			sb.setLength(0);
			int columnNumber = 0;
			int spectrumNameStart = 0;
			String spectrumName = null;
			for (int i = 0; i < line.length(); i++) {
				if (line.charAt(i) == '\t') {
					columnNumber++;
					if (columnNumber == spectrumNameColumn) {
						// This is the column we are skipping
						// Append everything till now to the output
						sb.append(line, 0, i);
						spectrumNameStart = i + 1;
					} else if (columnNumber == spectrumNameColumn + 1) {
						// We are past the column to skip
						// Spectrum name is in between
						spectrumName = line.substring(spectrumNameStart, i);

						// Append everything from here to the end of the string (including the tab)
						sb.append(line, i, line.length());
						break;
					}
				}
			}
			mapSpectrumNameToScaffoldSpectraInfo.put(spectrumName, fixCommaSeparatedThousands(sb.toString()));
		}
	}

	/**
	 * @return Version of Scaffold used to generate information for these spectra.
	 */
	public String getScaffoldVersion() {
		return scaffoldVersion;
	}

	private String fixCommaSeparatedThousands(String s) {
		return s.replaceAll(",(\\d\\d\\d)", "$1");
	}

	/**
	 * @return Index of the column to skip.
	 */
	private int processHeader(File file, String line) {
		// Extract everything except the spectrum column name
		String[] tempHeader = line.split("\t");
		// Scaffold 2.06.01 has a bug - one column is added extra before the last starred/hidden. We give this column an explicit name "Blank Column"
		header = new String[tempHeader.length];
		emptyLine = null;
		int spectrumNameColumn = 0;
		int columnOffset = 0;
		for (int i = 0; i < tempHeader.length; i++) {
			if (SPECTRUM_NAME_COLUMN.equals(tempHeader[i])) {
				spectrumNameColumn = i;
				columnOffset++; // We are skipping this column
				continue;
			}
			if (STARRED_COLUMN.equals(tempHeader[i])) {
				header[i - columnOffset] = "Blank Column";
				columnOffset--; // We are inserting a "Blank Column"
			}
			header[i - columnOffset] = tempHeader[i];
		}
		if (spectrumNameColumn == 0) {
			throw new MprcException("Wrong Scaffold spectra file format for file [" + file.getAbsolutePath() + "] - header column missing [" + SPECTRUM_NAME_COLUMN + "].");
		}
		return spectrumNameColumn;
	}

	/**
	 * @return Tab-separated header line for all the data provided. The header does not include spectrumName.
	 */
	@Override
	public String getHeaderLine() {
		return Joiner.on("\t").join(header);
	}

	/**
	 * @return A sequence of tabs that matches the length of the header-1. Used to output missing information.
	 */
	@Override
	public String getEmptyLine() {
		if (emptyLine == null) {
			emptyLine = StringUtilities.repeat('\t', header.length - 1);
		}
		return emptyLine;
	}

	/**
	 * @param key Name of the .dta file. This corresponds to Scaffolds 'spectrum' attribute in the PepXML export.
	 * @return Information for given spectrum, tab-separated. The <code>spectrumName</code> itself is not included.
	 */
	@Override
	public String getLineForKey(String key) {
		return mapSpectrumNameToScaffoldSpectraInfo.get(key);
	}

	@Override
	public Iterator<String> iterator() {
		return mapSpectrumNameToScaffoldSpectraInfo.keySet().iterator();
	}
}
