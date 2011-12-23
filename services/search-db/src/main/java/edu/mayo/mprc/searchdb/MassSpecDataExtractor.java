package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.searchdb.dao.TandemMassSpectrometrySample;

/**
 * For given biological sample name and name of a fraction, obtains a full information about the tandem mass spectrometry
 * sample (.RAW file or .mgf).
 *
 * @author Roman Zenka
 */
public interface MassSpecDataExtractor {
	/**
	 * @param biologicalSampleName Name of the biological sample (corresponds to Scaffold "column").
	 * @param fractionName         Name of the fraction - typically matches the input file name without an extension.
	 * @return Full information about the mass spectrometry sample.
	 */
	TandemMassSpectrometrySample getTandemMassSpectrometrySample(String biologicalSampleName, String fractionName);
}
