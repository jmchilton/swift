package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.searchdb.dao.*;

/**
 * A key uniquely identifying a {@link PeptideSpectrumMatch}.
 *
 * @author Roman Zenka
 */
final class PeptideSpectrumMatchKey {
	private final BiologicalSample biologicalSample;
	private final SearchResult searchResult;
	private final ProteinGroup proteinGroup;
	private final IdentifiedPeptide identifiedPeptide;

	PeptideSpectrumMatchKey(BiologicalSample biologicalSample, SearchResult searchResult, ProteinGroup proteinGroup, IdentifiedPeptide identifiedPeptide) {
		this.biologicalSample = biologicalSample;
		this.searchResult = searchResult;
		this.proteinGroup = proteinGroup;
		this.identifiedPeptide = identifiedPeptide;
	}

	public BiologicalSample getBiologicalSample() {
		return biologicalSample;
	}

	public SearchResult getSearchResult() {
		return searchResult;
	}

	public ProteinGroup getProteinGroup() {
		return proteinGroup;
	}

	public IdentifiedPeptide getIdentifiedPeptide() {
		return identifiedPeptide;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PeptideSpectrumMatchKey that = (PeptideSpectrumMatchKey) o;

		if (!biologicalSample.equals(that.biologicalSample)) return false;
		if (!identifiedPeptide.equals(that.identifiedPeptide)) return false;
		if (!proteinGroup.equals(that.proteinGroup)) return false;
		if (!searchResult.equals(that.searchResult)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = biologicalSample.hashCode();
		result = 31 * result + searchResult.hashCode();
		result = 31 * result + proteinGroup.hashCode();
		result = 31 * result + identifiedPeptide.hashCode();
		return result;
	}
}
