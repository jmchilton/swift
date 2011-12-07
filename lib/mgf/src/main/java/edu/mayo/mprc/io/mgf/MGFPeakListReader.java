/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.mprc.io.mgf;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;
import org.proteomecommons.io.GenericPeak;
import org.proteomecommons.io.Peak;
import org.proteomecommons.io.mgf.MascotGenericFormatPeakList;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for reading mgf files. The implementation of this class does not check for file extention to be of the type
 * mgf. Also, this class uses a buffered reader to handle the source file.
 */
public final class MGFPeakListReader implements Closeable {

	private final BufferedReader bufferedReader;
	private final File inputFile;
	private boolean readPeaks;

	/**
	 * Constructor
	 *
	 * @param inputFileName mgf file name.
	 */
	public MGFPeakListReader(String inputFileName) {
		this(new File(inputFileName));
	}

	/**
	 * Constructor
	 *
	 * @param inputFile mgf file name.
	 */
	public MGFPeakListReader(File inputFile) {
		this.inputFile = inputFile;
		this.readPeaks = true;
		bufferedReader = FileUtilities.getReader(inputFile);
	}

	/**
	 * Checks whether the PEPMASS is specified with both m/z and intensity. If so, only the first portion is used.
	 */
	private static final Pattern PEPMASS_INTENSITY = Pattern.compile("(PEPMASS\\s*=\\s*[0-9.+-e]+)\\s+.*");

	/**
	 * Method implementation returns the next peak list in the order as the peak list are in the file.
	 *
	 * @return MascotGenericFormatPeakList object representing the next peak list in the mgf file.
	 * @throws IOException
	 */
	public MascotGenericFormatPeakList nextPeakList() {
		boolean foundPeaks = false;
		String line = null;
		String[] split = null;

		MascotGenericFormatPeakList peaklist = new MascotGenericFormatPeakList();

		LinkedList<Peak> peaks = new LinkedList<Peak>();

		try {
			while ((line = bufferedReader.readLine()) != null) {

				line = line.trim();

				/**
				 * Get next line if blank
				 */
				if (line.equals("")) {
					continue;
				}

				/**
				 * Peptide mass
				 */
				if (line.startsWith("PEPMASS")) {
					peaklist.setTandemCount(2);
					Matcher matcher = PEPMASS_INTENSITY.matcher(line);
					if (matcher.matches()) {
						peaklist.setPepmass(matcher.group(1));
					} else {
						peaklist.setPepmass(line);
					}
					continue;
				}

				/**
				 * Charge
				 */
				if (line.startsWith("CHARGE")) {
					peaklist.setTandemCount(2);
					peaklist.setCharge(line);
				}

				/**
				 * Title
				 */
				if (line.startsWith("TITLE")) {
					String tentativeTitle = line.substring(line.indexOf('=') + 1, line.length());
					if (tentativeTitle != null) {
						tentativeTitle = tentativeTitle.trim();
					}

					peaklist.setTitle(tentativeTitle);
					continue;
				}

				/**
				 * Peak list starts
				 */
				if (line.startsWith("BEGIN IONS")) {
					foundPeaks = true;
				}

				if (line.contains("END IONS")) {
					break;
				}

				/**
				 * Check for the start of the list of peaks
				 */
				if (line.charAt(0) >= '0' && line.charAt(0) <= '9' && foundPeaks) {

					for (; line != null && line.indexOf("END IONS") == -1; line = bufferedReader.readLine()) {
						if (readPeaks) {
							try {
								/**
								 * Array containing the m/z and the intensity
								 */
								split = line.split("\\s+");

								double massOverCharge = Double.parseDouble(split[0]);
								double intensity = Double.parseDouble(split[1]);

								GenericPeak gp = new GenericPeak();
								gp.setMassOverCharge(massOverCharge);
								gp.setIntensity(intensity);
								peaks.add(gp);
							} catch (Exception ignore) {
								// SWALLOWED: Do nothing
							}
						}
					}

					break;
				}
			}
		} catch (IOException e) {
			throw new MprcException("Failure reading peaklists from .mgf file " + inputFile.getAbsolutePath(), e);
		}

		/**
		 * Return null if not peak list is found
		 */
		if (!foundPeaks) {
			return null;
		}

		if (readPeaks) {
			peaklist.setPeaks(peaks.toArray(new Peak[peaks.size()]));
		}

		return peaklist;
	}

	/**
	 * Close the buffered reader that handles this mgf file.
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (bufferedReader != null) {
			bufferedReader.close();
		}
	}

	public boolean isReadPeaks() {
		return readPeaks;
	}

	/**
	 * When set to false, the reader will not parse actual peak data and return just the headers.
	 * That can speed the execution a lot. Default is true.
	 *
	 * @param readPeaks Set to false to skip parsing the peaks themselves.
	 */
	public void setReadPeaks(boolean readPeaks) {
		this.readPeaks = readPeaks;
	}
}
