package edu.mayo.mprc.utilities;

import edu.mayo.mprc.MprcException;
import joptsimple.OptionSet;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * Utilities for command line parsing.
 */
public final class CommandLine {

	private CommandLine() {
	}

	/**
	 * Fetches a given property file, testing whether it exists and is in proper format. If anything fails, this method
	 * throws an exception.
	 *
	 * @param options         Command line options.
	 * @param paramName       Name of the command line parameter for this file.
	 * @param fileDescription Description of the file for the error messages.
	 * @param defaultValue    Default name of the file in case it is not specified.
	 * @param props           Properties to be filled with the contents of the file. If null, the properties are not loaded.
	 * @return The location of the property file.
	 */
	public static File findPropertyFile(final OptionSet options, final String paramName, final String fileDescription, final String defaultValue, final Properties props) {
		final File file = findFile(options, paramName, fileDescription, defaultValue);
		FileReader reader = null;
		try {
			if (props != null) {
				reader = new FileReader(file);
				props.load(reader);
			}
		} catch (Exception t) {
			throw new MprcException("Cannot parse " + fileDescription + " " + file + ".\n" + t.getMessage(), t);
		} finally {
			FileUtilities.closeQuietly(reader);
		}
		return file;
	}

	/**
	 * Like {@link #findPropertyFile} only without property parsing.
	 *
	 * @param options         Command line options.
	 * @param paramName       Name of the command line parameter for this file.
	 * @param fileDescription Description of the file for the error messages.
	 * @param defaultValue    Default name of the file in case it is not specified.
	 * @return The location of the file.
	 */
	public static File findFile(final OptionSet options, final String paramName, final String fileDescription, final String defaultValue) {
		File file = null;
		if (options.has(paramName)) {
			file = (File) options.valueOf(paramName);
		} else if (defaultValue != null) {
			file = new File(defaultValue).getAbsoluteFile();
			FileUtilities.err("The " + fileDescription + " parameter not specified, trying default " + file.getAbsolutePath());
			FileUtilities.err("You can set path to " + fileDescription + " using the --" + paramName + " switch.");
		} else {
			throw new MprcException("The " + fileDescription + " parameter not specified.\n" +
					"You can set path to " + fileDescription + " using the --" + paramName + " switch.");
		}

		if (file.exists()) {
			if (!file.isFile()) {
				throw new MprcException("The specified " + fileDescription + " " + file + " is not a file.");
			}
			if (!file.canRead()) {
				throw new MprcException("Cannot read " + fileDescription + " " + file + ".");
			}
		} else {
			throw new MprcException("The " + fileDescription + " " + file.getAbsolutePath() + " does not exist.");
		}
		return file;
	}
}
