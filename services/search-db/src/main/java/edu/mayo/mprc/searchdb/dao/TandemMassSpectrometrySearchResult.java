package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.PersistableBase;

import java.util.List;

/**
 * A list of search results for a particular tandem mass spectrometry sample.
 *
 * @author Roman Zenka
 */
public class TandemMassSpectrometrySearchResult extends PersistableBase {
	/**
	 * The mass spec sample.
	 */
	private TandemMassSpectrometrySample massSpecSample;

	/**
	 * List of all peptides identified in this sample.
	 */
	private List<PeptideIdentification> peptideIdentifications;

	/**
	 * Empty constructor for Hibernate.
	 */
	public TandemMassSpectrometrySearchResult() {
	}

	public TandemMassSpectrometrySearchResult(TandemMassSpectrometrySample massSpecSample, List<PeptideIdentification> peptideIdentifications) {
		this.massSpecSample = massSpecSample;
		this.peptideIdentifications = peptideIdentifications;
	}

	public TandemMassSpectrometrySample getMassSpecSample() {
		return massSpecSample;
	}

	public List<PeptideIdentification> getPeptideIdentifications() {
		return peptideIdentifications;
	}
}
