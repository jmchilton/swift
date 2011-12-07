package edu.mayo.mprc.chem;

/**
 * Utility methods for ms spectrum related calculations.
 */
public final class MsSpectra {
	/**
	 * Average distance between isotopes; Horn, J Am Soc Mass Spectrom 2000, 11, 323.;
	 */
	public static final double DISTANCE_BETWEEN_PEAKS = 1.00235;

	private MsSpectra() {
	}

	/**
	 * @param peakMass      Mass of the peak that was measured.
	 * @param isotopeOffset Offset of the measured peak within isotopic cluster. 0 stands for monoisotopic mass.
	 * @param charge        Z of the isotopic cluster.
	 * @return Mass of the monoisotopic peak. Obtained by subtracting the average between peak distance from the observed mass.
	 */
	public static double getMonoisotopicMass(double peakMass, double isotopeOffset, double charge) {
		return peakMass - isotopeOffset * MsSpectra.DISTANCE_BETWEEN_PEAKS / charge;
	}
}
