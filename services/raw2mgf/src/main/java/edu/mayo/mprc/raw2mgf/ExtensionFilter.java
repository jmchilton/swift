package edu.mayo.mprc.raw2mgf;

import java.io.File;
import java.io.FilenameFilter;

class ExtensionFilter implements FilenameFilter {
	private String extension;

	public ExtensionFilter(final String extension) {
		this.extension = extension;
	}

	public boolean accept(final File dir, final String name) {
		return (name.endsWith(extension));
	}
}


