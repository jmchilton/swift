package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;

/**
 * Information about a single Scaffold biological sample.
 * <p/>
 * The spectrum IDs need to be separated on sample by
 * sample basis.
 * <p/>
 * The following Scaffold spectrum report columns are parsed to form this object:
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

	public BiologicalSample(final String sampleName, final String category, final SearchResultList searchResults) {
		this.sampleName = sampleName;
		this.category = category;
		this.searchResults = searchResults;
	}

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(final String sampleName) {
		this.sampleName = sampleName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public SearchResultList getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(final SearchResultList searchResults) {
		this.searchResults = searchResults;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final BiologicalSample that = (BiologicalSample) o;

		if (!keyEquals(that)) {
			return false;
		}
		if (getSearchResults() != null ? !getSearchResults().equals(that.getSearchResults()) : that.getSearchResults() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = keyHashCode();
		result = 31 * result + (getSearchResults() != null ? getSearchResults().hashCode() : 0);
		return result;
	}

	public boolean keyEquals(final BiologicalSample that) {
		if (getCategory() != null ? !getCategory().equals(that.getCategory()) : that.getCategory() != null) {
			return false;
		}
		if (getSampleName() != null ? !getSampleName().equals(that.getSampleName()) : that.getSampleName() != null) {
			return false;
		}
		return true;
	}

	public int keyHashCode() {
		int result = getSampleName() != null ? getSampleName().hashCode() : 0;
		result = 31 * result + (getCategory() != null ? getCategory().hashCode() : 0);
		return result;
	}
}
