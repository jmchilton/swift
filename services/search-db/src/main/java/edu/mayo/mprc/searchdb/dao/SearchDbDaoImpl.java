package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.config.RuntimeInitializer;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.swift.db.SwiftDao;
import org.hibernate.criterion.Restrictions;

import java.util.Map;

/**
 * DAO for the search results stored in the database.
 *
 * @author Roman Zenka
 */
public final class SearchDbDaoImpl extends DaoBase implements RuntimeInitializer, SearchDbDao {
	private SwiftDao swiftDao;

	public SearchDbDaoImpl(SwiftDao swiftDao, DatabasePlaceholder databasePlaceholder) {
		super(databasePlaceholder);
	}

	@Override
	public String check(Map<String, String> params) {
		return null;
	}

	@Override
	public void initialize(Map<String, String> params) {
	}

	@Override
	public ProteinSequence addProteinSequence(ProteinSequence proteinSequence) {
		return save(proteinSequence, Restrictions.eq("sequence", proteinSequence.getSequence()), false);
	}

	@Override
	public ProteinSequence getProteinSequence(int proteinId) {
		return (ProteinSequence) getSession().get(ProteinSequence.class, proteinId);
	}

	@Override
	public PeptideSequence addPeptideSequence(PeptideSequence peptideSequence) {
		return save(peptideSequence, Restrictions.eq("sequence", peptideSequence.getSequence()), false);
	}

	@Override
	public PeptideSequence getPeptideSequence(int peptideId) {
		return (PeptideSequence) getSession().get(PeptideSequence.class, peptideId);
	}

	@Override
	public LocalizedModification addLocalizedModification(LocalizedModification mod) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public IdentifiedPeptide addIdentifiedPeptide(IdentifiedPeptide peptide) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public PeptideSpectrumMatch addPeptideSpectrumMatch(PeptideSpectrumMatch match) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public ProteinGroup addProteinGroup(ProteinGroup group) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public TandemMassSpectrometrySample addTandemMassSpectrometrySample(TandemMassSpectrometrySample sample) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public SearchResult addSearchResult(SearchResult searchResult) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public BiologicalSample addBiologicalSample(BiologicalSample biologicalSample) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Analysis addAnalysis(Analysis analysis) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
