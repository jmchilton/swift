package edu.mayo.mprc.msconvert;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse msaccess output to determine if its MS2 data are in profile mode.
 * Right now we obtain the file-level metadata and check whether the analyzer
 * for the second instrument configuration is Orbitrap, or whether there is only one instrument configuration
 * (QExactive).
 * <p/>
 * This kind of check is most likely completely wrong, but it should work for the kind of .RAW files
 * we are dealing with.
 *
 * @author Roman Zenka
 */
public final class MsaccessMetadataParser {
	private static final Pattern STARTING_SPACES = Pattern.compile("^(\\s*)([^:]*):?\\s*(.*)");
	public static final int SPACES_PER_TAB = 2;

	private final Reader msaccessOutput;
	/**
	 * Will have 'O' if the given configuration was analyzed by Orbitrap, otherwise '.'
	 */
	private String orbitrapConfigs = "";
	private boolean wasAnalyzerSpecified = false;
	private boolean wasOrbitrap = false;
	private String source = "";

	/**
	 * @param msaccessOutput Output from msaccess.
	 */
	public MsaccessMetadataParser(final Reader msaccessOutput) {
		this.msaccessOutput = msaccessOutput;
		source = "<unknown>";
	}

	public MsaccessMetadataParser(final File msaccessFile) {
		try {
			msaccessOutput = new FileReader(msaccessFile);
			source = msaccessFile.getAbsolutePath();
		} catch (FileNotFoundException e) {
			throw new MprcException("Could not parse msaccess file " + msaccessFile.getAbsolutePath(), e);
		}
	}

	public void process() {
		final LineNumberReader reader = new LineNumberReader(msaccessOutput);
		try {
			String line;
			int depth = -1;
			StringBuffer key = new StringBuffer("");

			// Parse the file, create a stack for the current key
			while ((line = reader.readLine()) != null) {
				final Matcher matcher = STARTING_SPACES.matcher(line);
				if (matcher.matches()) {
					final int newDepth = matcher.group(1).length() / SPACES_PER_TAB;
					while (depth >= newDepth) {
						final int newLength = key.lastIndexOf("/");
						key.setLength(newLength >= 0 ? newLength : 0);
						depth--;
					}
					final String newKey = matcher.group(2);
					final String value = matcher.group(3);
					if (key.length() > 1) {
						key.append('/');
					}
					key.append(newKey);
					depth++;
					// We can only jump one level ahead.
					if (depth != newDepth) {
						throw new MprcException("Failed to parse msaccess output at [" + source + "]. Too deep indent at line " + reader.getLineNumber());
					}
					processKeyValue(key.toString(), value);
				}
			}
			processingEnded();
		} catch (IOException e) {
			throw new MprcException("Failed to read msaccess metadata from [" + source + "]", e);
		} finally {
			FileUtilities.closeQuietly(reader);
		}
	}

	private void processingEnded() {
		if (!wasAnalyzerSpecified) {
			throw new MprcException("The msaccess metadata file [" + source + "] is likely corrupted.\ninstrumentConfigurationList.instrumentConfiguration.componentList.analyzer.cvParam field missing.");
		}
		orbitrapConfigs += wasOrbitrap ? "O" : ".";
	}

	public boolean isOrbitrapForMs2() {
		return "O".equals(orbitrapConfigs) || ".O".equals(orbitrapConfigs) || "OO".equals(orbitrapConfigs);
	}

	private void processKeyValue(String key, String value) {
		// Now our stack knows exactly where are we in the tree.
		if ("instrumentConfigurationList/instrumentConfiguration".equals(key)) {
			orbitrapConfigs += wasOrbitrap ? "O" : ".";
			wasOrbitrap = false;
		}
		if ("instrumentConfigurationList/instrumentConfiguration/componentList/analyzer/cvParam".equals(key)) {
			wasAnalyzerSpecified = true;
			if (value.equalsIgnoreCase("orbitrap")) {
				wasOrbitrap = true;
			}
		}
	}
}
