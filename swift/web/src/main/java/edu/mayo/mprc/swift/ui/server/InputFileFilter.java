package edu.mayo.mprc.swift.ui.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter to find files that are not hidden and end with one of the extensions from a given list.
 */
public final class InputFileFilter implements FilenameFilter, Serializable {
	private static final long serialVersionUID = 20101221L;

	private final Pattern filePattern;
	private static final int PATTERN_BUILDER_CAPACITY = 20;
	private boolean dirsToo;

	public InputFileFilter() {
		filePattern = Pattern.compile(".*");
	}

	/**
	 * @param allowedExtensions Allowed extensions, including the dot (such as ".RAW" or ".mgf")
	 */
	public InputFileFilter(String allowedExtensions, boolean dirsToo) {
		String[] extensions = allowedExtensions.split("\\|");
		StringBuilder pattern = new StringBuilder(PATTERN_BUILDER_CAPACITY);
		// We build a pattern like this: .*\\.ext$|.*\\.ext2$|...
		// It should match any file name that ENDS in given extension.
		for (String extension : extensions) {
			pattern.append("|.*");
			pattern.append(Pattern.quote(extension));
			pattern.append('$');
		}
		filePattern = Pattern.compile(pattern.substring(1));
		this.dirsToo = dirsToo;
	}

	public boolean accept(File dir, String name) {
		boolean result = false;
		File file = new File(dir, name);
		if (!file.isHidden()) {
			if (this.dirsToo && file.isDirectory()) {
				result = true;
			} else if (file.isFile()) {
				Matcher match = filePattern.matcher(name);
				if (match.matches()) {
					result = true;
				}
			}
		}
		return result;
	}
}