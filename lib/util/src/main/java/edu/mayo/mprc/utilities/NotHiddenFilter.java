package edu.mayo.mprc.utilities;

import java.io.File;
import java.io.FilenameFilter;


/**
 * Filters out everything that is NOT a hidden file.
 */
public class NotHiddenFilter implements FilenameFilter {

	public NotHiddenFilter() {
	}

	public boolean accept(File dir, String name) {
		return !new File(dir, name).isHidden();
	}
}