package edu.mayo.mprc.searchdb.builder;

import edu.mayo.mprc.searchdb.dao.SpectrumIdentificationCounts;

/**
 * @author Roman Zenka
 */
public class SpectrumIdentificationCountsBuilder implements Builder<SpectrumIdentificationCounts> {
	private int numberOfIdentifiedSpectra;
	private int[] numberOfIdentifiedSpectraByCharge = new int[4];

	@Override
	public SpectrumIdentificationCounts build() {
		return new SpectrumIdentificationCounts(numberOfIdentifiedSpectra,
				numberOfIdentifiedSpectraByCharge[0],
				numberOfIdentifiedSpectraByCharge[1],
				numberOfIdentifiedSpectraByCharge[2],
				numberOfIdentifiedSpectraByCharge[3]);
	}

	/**
	 * A new spectrum appeared. Based on the charege, update the spectrum statistics
	 *
	 * @param spectrumCharge Charge of the spectrum.
	 */
	public void addSpectrum(final int spectrumCharge) {
		numberOfIdentifiedSpectra++;
		if (spectrumCharge >= 1 && spectrumCharge <= numberOfIdentifiedSpectraByCharge.length) {
			numberOfIdentifiedSpectraByCharge[spectrumCharge - 1]++;
		}
	}

}
