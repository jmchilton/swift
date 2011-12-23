package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;

import java.util.List;

/**
 * Information about a single Scaffold biological sample.
 * <p/>
 * The spectrum IDs need to be separated on sample by
 * sample basis.
 *
 * @author Roman Zenka
 */
public class BiologicalSample extends PersistableBase {
	/**
	 * Name of the sample.
	 */
	private String sampleName;

	/**
	 * Category of the sample. This is usually set to "none", but sometimes it can contain useful information.
	 */
	private String category;

	/**
	 * Results of protein searches for this particular biological sample. Would usually contain only one mass
	 * spec sample.
	 */
	private List<TandemMassSpectrometrySearchResult> searchResults;

	/**
	 * Empty constructor for Hibernate.
	 */
	public BiologicalSample() {
	}

	public BiologicalSample(String sampleName, String category, List<TandemMassSpectrometrySearchResult> searchResults) {
		this.sampleName = sampleName;
		this.category = category;
		this.searchResults = searchResults;
	}

	public String getSampleName() {
		return sampleName;
	}

	public String getCategory() {
		return category;
	}

	public List<TandemMassSpectrometrySearchResult> getSearchResults() {
		return searchResults;
	}
}
