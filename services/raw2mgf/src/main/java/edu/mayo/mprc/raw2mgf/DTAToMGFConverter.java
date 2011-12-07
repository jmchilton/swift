package edu.mayo.mprc.raw2mgf;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

public final class DTAToMGFConverter {

	private static final Logger LOGGER = Logger.getLogger(DTAToMGFConverter.class);

	private File[] dtaFiles;
	private static final double PROTON_AMU = 1.00727646;
	private File resultFile;

	// If the dta files were produced by wine, we should remove all the 0 characters from them.
	private boolean wineCleanup;

	public DTAToMGFConverter(File[] dtaFiles, File outputMgfFile) {
		this.dtaFiles = dtaFiles;
		this.resultFile = outputMgfFile;
	}

	public boolean isWineCleanup() {
		return wineCleanup;
	}

	public void setWineCleanup(boolean wineCleanup) {
		this.wineCleanup = wineCleanup;
	}

	public void run() throws Exception {
		try {
			convert(resultFile, dtaFiles, this.wineCleanup);
		} finally {
			// now remove the dta files as no longer need them
			for (File file : dtaFiles) {
				if (file != null && file.exists()) {
					boolean isdeleted = file.delete();
					if (!isdeleted) {
						LOGGER.warn("Deletion of dta file failed with name=" + file.getAbsolutePath());
					}
				}
			}
		}
	}

	/**
	 * appends the dta files given to the mgf file
	 *
	 * @param mgf_file - the .mgf file to be produced
	 * @param files    - the dta files
	 * @return Number of dta files combined into the .mgf file
	 * @throws IOException
	 */
	public static long convert(File mgf_file, File[] files, boolean wineCleanup) throws IOException {

		DecimalFormat df = new DecimalFormat("#.############");

		if (files.length == 0) {
			throw new MprcException("No .dta files found. Either the MS2 spectra are missing completely, or they did not pass minimum quality threshold.");
		}

		LOGGER.info("Processing " + files.length + " .dta files.");

		String searchName = null;
		boolean warning = false;
		BufferedWriter output = null;
		int numSpectra = 0;

		try {
			char[] buffer = new char[50 * 1024];
			for (File file : files) {
				DtaName match = new DtaName(file);
				if (file.isHidden() || !match.matches()) {
					continue;
				}
				String thisSearchName = match.getSearchName();
				if (searchName == null) {
					searchName = thisSearchName;
				}
				if (!thisSearchName.equals(searchName)) {
					if (!warning) {
						LOGGER.warn("This directory " +
								"appears to contain DTA files from " +
								"more than one RAW file: " + searchName +
								" " + thisSearchName + "\n");
						warning = true;
					}
				}

				String scan1 = match.getFirstScan();
				String scan2 = match.getSecondScan();
				String extra = match.getExtras();

				BufferedReader input = null;

				if (output == null) {
					output = new BufferedWriter(new FileWriter(mgf_file, true));
				}

				try {
					input = new BufferedReader(new FileReader(file));

					String line = input.readLine();
					if (line == null) {
						throw new MprcException("File " + file.getAbsolutePath() + " length is " + file.length() + ". This may be caused by disk running out of space.");
					}
					if (wineCleanup) {
						line = line.replaceAll("\\x00", "");
					}
					StringTokenizer tokenizer = new StringTokenizer(line);
					if (tokenizer.countTokens() == 2) {
						numSpectra++;
						output.write("BEGIN IONS\n");
						double MH = new Double(tokenizer.nextToken());
						int z = Integer.valueOf(tokenizer.nextToken());
						double mOverZ = (MH + (z - 1) * PROTON_AMU) / z;
						output.write("TITLE=" + searchName + " scan " + scan1 + " " + scan2);
						if (extra != null) {
							output.write(" " + extra);
						}
						output.write(" (" + file.getName() + ")\n");
						output.write("CHARGE=" + z + "+\n");
						output.write("PEPMASS=" + df.format(mOverZ) + "\n");

						if (wineCleanup) {
							// If we ran through wine, we make sure we convert the second line of input (to be safe)
							// removing nulls.
							String secondLine = input.readLine();
							if (secondLine != null) {
								output.write(secondLine.replaceAll("\\x00", ""));
								output.write("\r\n");
							}
						}

						int charsRead = 1;
						while (0 < charsRead) {
							charsRead = input.read(buffer);
							if (charsRead > 0) {
								output.write(buffer, 0, charsRead);
							}
						}
						output.write("\nEND IONS\n\n");
					}
				} finally {
					FileUtilities.closeQuietly(input);
				}
			}
		} finally {
			if (output != null) {
				output.close();
			}
			return numSpectra;
		}
	}

	public File getResultFile() {
		return this.resultFile;
	}
}

