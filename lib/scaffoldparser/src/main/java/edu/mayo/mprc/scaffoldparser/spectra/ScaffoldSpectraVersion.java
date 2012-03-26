package edu.mayo.mprc.scaffoldparser.spectra;

/**
 * Determines the version of Scaffold spectra file.
 *
 * @author Roman Zenka
 */
public final class ScaffoldSpectraVersion extends ScaffoldSpectraReader {
	public static final String SCAFFOLD_VERSION_KEY = "Scaffold Version";

	public ScaffoldSpectraVersion() {
	}

	@Override
	public boolean processMetadata(final String key, final String value) {
		if (SCAFFOLD_VERSION_KEY.equalsIgnoreCase(key)) {
			setScaffoldVersion(value);
			return false;
		}
		return true;
	}

	@Override
	public boolean processHeader(final String line) {
		// Stop processing
		return false;
	}

	@Override
	public boolean processRow(final String line) {
		// Stop processing
		return false;
	}
}
