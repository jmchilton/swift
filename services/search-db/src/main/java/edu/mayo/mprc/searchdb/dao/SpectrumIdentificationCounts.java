package edu.mayo.mprc.searchdb.dao;

/**
 * Count of identified spectra, split by the spectrum charge.
 *
 * @author Roman Zenka
 */
public final class SpectrumIdentificationCounts {
	/**
	 * Number of identified spectra (total, not unique)
	 */
	private int numberOfIdentifiedSpectra;

	/**
	 * Number of identified +1H spectra
	 */
	private int numberOfIdentified1HSpectra;

	/**
	 * Number of identified +2H spectra
	 */
	private int numberOfIdentified2HSpectra;

	/**
	 * Number of identified +3H spectra
	 */
	private int numberOfIdentified3HSpectra;

	/**
	 * Number of identified +4H spectra
	 */
	private int numberOfIdentified4HSpectra;

	public SpectrumIdentificationCounts() {
	}

	public SpectrumIdentificationCounts(int numberOfIdentifiedSpectra, int numberOfIdentified1HSpectra, int numberOfIdentified2HSpectra, int numberOfIdentified3HSpectra, int numberOfIdentified4HSpectra) {
		this.numberOfIdentifiedSpectra = numberOfIdentifiedSpectra;
		this.numberOfIdentified1HSpectra = numberOfIdentified1HSpectra;
		this.numberOfIdentified2HSpectra = numberOfIdentified2HSpectra;
		this.numberOfIdentified3HSpectra = numberOfIdentified3HSpectra;
		this.numberOfIdentified4HSpectra = numberOfIdentified4HSpectra;
	}

	public int getNumberOfIdentifiedSpectra() {
		return numberOfIdentifiedSpectra;
	}

	public void setNumberOfIdentifiedSpectra(int numberOfIdentifiedSpectra) {
		this.numberOfIdentifiedSpectra = numberOfIdentifiedSpectra;
	}

	public int getNumberOfIdentified1HSpectra() {
		return numberOfIdentified1HSpectra;
	}

	public void setNumberOfIdentified1HSpectra(int numberOfIdentified1HSpectra) {
		this.numberOfIdentified1HSpectra = numberOfIdentified1HSpectra;
	}

	public int getNumberOfIdentified2HSpectra() {
		return numberOfIdentified2HSpectra;
	}

	public void setNumberOfIdentified2HSpectra(int numberOfIdentified2HSpectra) {
		this.numberOfIdentified2HSpectra = numberOfIdentified2HSpectra;
	}

	public int getNumberOfIdentified3HSpectra() {
		return numberOfIdentified3HSpectra;
	}

	public void setNumberOfIdentified3HSpectra(int numberOfIdentified3HSpectra) {
		this.numberOfIdentified3HSpectra = numberOfIdentified3HSpectra;
	}

	public int getNumberOfIdentified4HSpectra() {
		return numberOfIdentified4HSpectra;
	}

	public void setNumberOfIdentified4HSpectra(int numberOfIdentified4HSpectra) {
		this.numberOfIdentified4HSpectra = numberOfIdentified4HSpectra;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SpectrumIdentificationCounts that = (SpectrumIdentificationCounts) o;

		if (getNumberOfIdentified1HSpectra() != that.getNumberOfIdentified1HSpectra()) {
			return false;
		}
		if (getNumberOfIdentified2HSpectra() != that.getNumberOfIdentified2HSpectra()) {
			return false;
		}
		if (getNumberOfIdentified3HSpectra() != that.getNumberOfIdentified3HSpectra()) {
			return false;
		}
		if (getNumberOfIdentified4HSpectra() != that.getNumberOfIdentified4HSpectra()) {
			return false;
		}
		if (getNumberOfIdentifiedSpectra() != that.getNumberOfIdentifiedSpectra()) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = getNumberOfIdentifiedSpectra();
		result = 31 * result + getNumberOfIdentified1HSpectra();
		result = 31 * result + getNumberOfIdentified2HSpectra();
		result = 31 * result + getNumberOfIdentified3HSpectra();
		result = 31 * result + getNumberOfIdentified4HSpectra();
		return result;
	}
}
