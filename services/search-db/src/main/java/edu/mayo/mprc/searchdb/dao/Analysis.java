package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;

import java.util.Date;
import java.util.List;

/**
 * Extracted information about how the analysis was performed that should get stored into the LIMS.
 *
 * @author Roman Zenka
 */
public final class Analysis extends PersistableBase {
	/**
	 * Scaffold version as a string. Can be null if the version could not be determined.
	 */
	private String scaffoldVersion;

	/**
	 * Date and time of when the analysis was run, as recorded in the report. This information
	 * is somewhat duplicated (we know when the user submitted the search).
	 * It can be null if the date could not be determined.
	 */
	private Date analysisDate;

	/**
	 * A list of all biological samples defined within the Scaffold analysis report.
	 */
	private List<BiologicalSample> biologicalSamples;

	/**
	 * Empty constructor for Hibernate.
	 */
	public Analysis() {
	}

	public Analysis(String scaffoldVersion, Date analysisDate, List<BiologicalSample> biologicalSamples) {
		this.scaffoldVersion = scaffoldVersion;
		this.analysisDate = analysisDate;
		this.biologicalSamples = biologicalSamples;
	}

	public String getScaffoldVersion() {
		return scaffoldVersion;
	}

	public void setScaffoldVersion(String scaffoldVersion) {
		this.scaffoldVersion = scaffoldVersion;
	}

	public Date getAnalysisDate() {
		return analysisDate;
	}

	public void setAnalysisDate(Date analysisDate) {
		this.analysisDate = analysisDate;
	}

	public List<BiologicalSample> getBiologicalSamples() {
		return biologicalSamples;
	}

	public void setBiologicalSamples(List<BiologicalSample> biologicalSamples) {
		this.biologicalSamples = biologicalSamples;
	}
}
