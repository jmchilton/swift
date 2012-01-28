package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;

/**
 * Information about a single Scaffold biological sample.
 * <p/>
 * The spectrum IDs need to be separated on sample by
 * sample basis.
 * <p/>
 * The followign Scaffold spectrum report columns are parsed to form this object:
 * <ul>
 * <li>Biological sample category</li>
 * <li>Biological sample name</li>
 * </ul>
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
	private SearchResultList searchResults;

	/**
	 * Empty constructor for Hibernate.
	 */
	public BiologicalSample() {
	}

	public BiologicalSample(String sampleName, String category, SearchResultList searchResults) {
		this.sampleName = sampleName;
		this.category = category;
		this.searchResults = searchResults;
	}

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public SearchResultList getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(SearchResultList searchResults) {
		this.searchResults = searchResults;
	}
}
