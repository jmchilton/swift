package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.searchdb.dao.BiologicalSample;

/**
 * Key identifying a search result.
 *
 * @author Roman Zenka
 */
final class SearchResultKey {
	private final BiologicalSample biologicalSample;
	private final String massSpectrometrySampleName;

	public SearchResultKey(BiologicalSample biologicalSample, String massSpectrometrySampleName) {
		this.biologicalSample = biologicalSample;
		this.massSpectrometrySampleName = massSpectrometrySampleName;
	}

	public BiologicalSample getBiologicalSample() {
		return biologicalSample;
	}

	public String getMassSpectrometrySampleName() {
		return massSpectrometrySampleName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SearchResultKey that = (SearchResultKey) o;

		if (!biologicalSample.equals(that.biologicalSample)) return false;
		if (!massSpectrometrySampleName.equals(that.massSpectrometrySampleName)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = biologicalSample.hashCode();
		result = 31 * result + massSpectrometrySampleName.hashCode();
		return result;
	}
}
