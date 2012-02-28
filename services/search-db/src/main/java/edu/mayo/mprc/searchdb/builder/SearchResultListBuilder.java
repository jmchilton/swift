package edu.mayo.mprc.searchdb.builder;

import edu.mayo.mprc.searchdb.dao.SearchResult;
import edu.mayo.mprc.searchdb.dao.SearchResultList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the list of search results.
 *
 * @author Roman Zenka
 */
public class SearchResultListBuilder implements Builder<SearchResultList> {
	private Map<String, SearchResultBuilder> list = new LinkedHashMap<String, SearchResultBuilder>();

	private BiologicalSampleBuilder biologicalSample;

	public SearchResultListBuilder(BiologicalSampleBuilder biologicalSample) {
		this.biologicalSample = biologicalSample;
	}

	@Override
	public SearchResultList build() {
		List<SearchResult> items = new ArrayList<SearchResult>(list.size());
		for (SearchResultBuilder builder : list.values()) {
			items.add(builder.build());
		}
		return new SearchResultList(items);
	}

	/**
	 * Get the current mass spec sample result test for given biological sample. If a new set is discovered,
	 * it is initialized and added to the biological sample.
	 *
	 * @param msmsSampleName The name of the tandem mass spectrometry sample.
	 * @return Current tandem mass spec search result object.
	 */
	public SearchResultBuilder getTandemMassSpecResult(String msmsSampleName) {
		final SearchResultBuilder searchResult = list.get(msmsSampleName);
		if (searchResult == null) {
			final SearchResultBuilder newSearchResult = new SearchResultBuilder(biologicalSample);
			newSearchResult.setMassSpecSample(
					biologicalSample.getAnalysis().getMassSpecDataExtractor().getTandemMassSpectrometrySample(biologicalSample.getSampleName(), msmsSampleName)
			);
			list.put(msmsSampleName, newSearchResult);
			return newSearchResult;
		}
		return searchResult;
	}
}
