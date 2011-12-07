package edu.mayo.mprc.raw2mgf;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a .dta file name into components
 */
public class DtaName {
	private static final String DTA_FILE_NAME_PATTERN = "^(.+?)\\.(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?\\.dta$";
	private static final Pattern DTA_PATTERN = Pattern.compile(DTA_FILE_NAME_PATTERN);

	private final Matcher matcher;

	public DtaName(File dtaFile) {
		this(dtaFile.getName());
	}

	public DtaName(String dtaFileName) {
		matcher = DTA_PATTERN.matcher(dtaFileName);
	}

	public boolean matches() {
		return matcher.matches();
	}

	public String getSearchName() {
		return matcher.group(1);
	}

	public String getFirstScan() {
		return matcher.group(2);
	}

	public String getSecondScan() {
		return matcher.group(3);
	}

	public String getCharge() {
		return matcher.group(4);
	}

	public String getExtras() {
		return matcher.group(5);
	}
}
