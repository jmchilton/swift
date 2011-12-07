package edu.mayo.mprc.io.mgf;

import org.proteomecommons.io.mgf.MascotGenericFormatPeakList;

/**
 * A generic MGF spectra filter that decides whether a given peaklist should be retained or not.
 *
 * @see edu.mayo.mprc.io.mgf.MGFFilteredFileGenerator
 */
public interface MgfPeakListFilter {
	/**
	 * @param peakList The peaklist to decide upon.
	 * @return true if the peaklist passes the criteria (and should be retained), false if it does not pass (and should be discarded)
	 */
	boolean peakListAccepted(MascotGenericFormatPeakList peakList);
}
