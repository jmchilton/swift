package edu.mayo.mprc.io.mgf;

/**
 * Immutable class serving as a result of the mgf filter. Provides information of the total amount of spectra filtered,
 * passed and failed spectra.
 */
public final class MgfFilteredSpectraCount {
	private final int totalSpectra;
	private final int acceptedSpectra;
	private final int rejectedSpectra;

	public MgfFilteredSpectraCount(int totalSpectra, int acceptedSpectra, int rejectedSpectra) {
		this.totalSpectra = totalSpectra;
		this.acceptedSpectra = acceptedSpectra;
		this.rejectedSpectra = rejectedSpectra;
	}

	public int getTotalSpectra() {
		return totalSpectra;
	}

	public int getAcceptedSpectra() {
		return acceptedSpectra;
	}

	public int getRejectedSpectra() {
		return rejectedSpectra;
	}
}

