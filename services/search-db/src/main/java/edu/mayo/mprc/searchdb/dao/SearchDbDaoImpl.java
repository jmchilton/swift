package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.config.RuntimeInitializer;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.swift.db.SwiftDao;
import org.hibernate.criterion.Restrictions;

import java.util.Map;

/**
 * DAO for the search results stored in the database.
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
	public ProteinSequence addProteinSequence(String sequence) {
		ProteinSequence proteinSequence = new ProteinSequence(sequence);
		return save(proteinSequence, Restrictions.eq("sequence", sequence), false);
	}

	@Override
	public ProteinSequence getProteinSequence(int proteinId) {
		return (ProteinSequence) getSession().get(ProteinSequence.class, proteinId);
	}

	@Override
	public PeptideSequence addPeptideSequence(String sequence) {
		PeptideSequence peptideSequence = new PeptideSequence(sequence);
		return save(peptideSequence, Restrictions.eq("sequence", sequence), false);
	}

	@Override
	public PeptideSequence getPeptideSequence(int peptideId) {
		return (PeptideSequence) getSession().get(PeptideSequence.class, peptideId);
	}
}
