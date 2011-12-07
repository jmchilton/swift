package edu.mayo.mprc.msmseval;

import com.google.common.base.Joiner;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.io.KeyedTsvReader;
import edu.mayo.mprc.utilities.StringUtilities;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * A class capable of reading msmsEval output and then responding to queries. For given scan number produces a tab-separated line
 * with msmsEval data. If the input file is null, the class will return empty output for any query.
 * <p/>
 * Right now the class simply loads the full file into memory, in the future (as we scale) we might want to do indexing
 * and serve the data from the disk.
 */
public final class MSMSEvalOutputReader implements KeyedTsvReader {
	private static final Logger LOGGER = Logger.getLogger(MSMSEvalOutputReader.class);

	private Map<String/*Scan ID*/, String/*The full line without scan ID*/> lines;
	private String[] header;
	private static final String SCAN_NUM_HEADER = "Scan #";
	/**
	 * Header without the first {@link #SCAN_NUM_HEADER} column.
	 */
	private static final String[] DEFAULT_HEADER = new String[]{
			"Parent", " NPeaks", "NormTIC", "GoodSegs",
			"IntnRatio1%", "IntnRatio20%", "complements",
			"IsoRatio", "H2ORatio", "AADiffRatio",
			"discriminant", "P(+|D)", "Z_prob"};
	private static final String EMPTY_LINE;

	static {
		// One less tab - we produce tabs only in between values
		EMPTY_LINE = StringUtilities.repeat('\t', DEFAULT_HEADER.length - 1);
	}

	/**
	 * Prepare the reader. In this implementation, the entire file is loaded at once and cached in memory (we
	 * expect the file to have around 10000 lines).
	 *
	 * @param msmsEvalFile msmsEval file to process
	 */
	public MSMSEvalOutputReader(File msmsEvalFile) {
		if (msmsEvalFile == null) {
			// Null files are honored - they will act as if there was no input information
			// Use default header (otherwise we use header obtained from the file).
			header = DEFAULT_HEADER;
		} else {
			try {
				lines = new HashMap<String, String>();
				parse(new BufferedReader(new FileReader(msmsEvalFile)), lines);
			} catch (Exception t) {
				throw new MprcException("Cannot parse msmsEval output file [" + msmsEvalFile.getAbsolutePath() + "]");
			}
		}
	}

	/**
	 * @return A tab-separated header line (sans the Scan number column) for all columns in the data in proper format
	 */
	@Override
	public String getHeaderLine() {
		return Joiner.on("\t").join(header);
	}

	@Override
	public String getEmptyLine() {
		return EMPTY_LINE;
	}

	/**
	 * @param key Scan number to obtain data for.
	 * @return Entire line for a given scan, ready to be embedded in a larger tab-separated scan. The line does not
	 *         contain the scan number. The columns are as specified by {@link #getHeaderLine()}.
	 */
	@Override
	public String getLineForKey(String key) {
		if (lines == null) {
			return EMPTY_LINE;
		}
		String line = lines.get(key);
		if (line == null) {
			return EMPTY_LINE;
		}
		return line;
	}

	private void parse(BufferedReader br, Map<String, String> lines) {
		try {
			String line = br.readLine();
			if (line == null) {
				throw new MprcException("The msmsEval output has no header");
			}
			String[] tmpHeader = line.split(",");
			if (!SCAN_NUM_HEADER.equals(tmpHeader[0])) {
				throw new MprcException("Unknown msmsEval output format (first column should be '" + SCAN_NUM_HEADER + "', was '" + tmpHeader[0] + "'.");
			}
			header = new String[tmpHeader.length - 1];
			System.arraycopy(tmpHeader, 1, header, 0, tmpHeader.length - 1);
			int ignoredLines = 0;
			while (true) {
				line = br.readLine();
				if (line == null) {
					break;
				}

				int firstComma = line.indexOf(',');
				if (firstComma > 0) {
					// We have data
					String tabSeparatedLine = line.replace(',', '\t');
					lines.put(line.substring(0, firstComma), tabSeparatedLine.substring(firstComma + 1));
				} else {
					// Ignore the line
					ignoredLines++;
				}
			}
			if (ignoredLines > 0) {
				LOGGER.info("Ignored lines when parsing msmsEval output file: " + ignoredLines);
			}
		} catch (Exception t) {
			throw new MprcException("Failed to parse msmsEval output file", t);
		}
	}

}
