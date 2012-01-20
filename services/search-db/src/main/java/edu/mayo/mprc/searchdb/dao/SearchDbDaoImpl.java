package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.config.RuntimeInitializer;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.swift.db.SwiftDao;
import org.hibernate.criterion.Restrictions;

import java.util.Map;

/**
 * DAO for the search results stored in the database.
 * We use stateless session to speed up the access, as this dao is used for bulk data loading.
 *
 * @author Roman Zenka
 */
public final class SearchDbDaoImpl extends DaoBase implements RuntimeInitializer, SearchDbDao {
	private SwiftDao swiftDao;

	public SearchDbDaoImpl(SwiftDao swiftDao, DatabasePlaceholder databasePlaceholder) {
		super(databasePlaceholder);
		this.swiftDao = swiftDao;
	}

	@Override
	public String check(Map<String, String> params) {
		return null;
	}

	@Override
	public void initialize(Map<String, String> params) {
	}

	@Override
	public void addProteinSequence(ProteinSequence proteinSequence) {
		save(proteinSequence, Restrictions.eq("sequence", proteinSequence.getSequence()), false);
	}

	@Override
	public ProteinSequence getProteinSequence(int proteinId) {
		return (ProteinSequence) getSession().get(ProteinSequence.class, proteinId);
	}

	@Override
	public void addPeptideSequence(PeptideSequence peptideSequence) {
		save(peptideSequence, Restrictions.eq("sequence", peptideSequence.getSequence()), false);
	}

	@Override
	public PeptideSequence getPeptideSequence(int peptideId) {
		return (PeptideSequence) getSession().get(PeptideSequence.class, peptideId);
	}

	@Override
	public void addLocalizedModification(LocalizedModification mod) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addIdentifiedPeptide(IdentifiedPeptide peptide) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addPeptideSpectrumMatch(PeptideSpectrumMatch match) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addProteinGroup(ProteinGroup group) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addTandemMassSpectrometrySample(TandemMassSpectrometrySample sample) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addSearchResult(SearchResult searchResult) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addBiologicalSample(BiologicalSample biologicalSample) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void addAnalysis(Analysis analysis) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
