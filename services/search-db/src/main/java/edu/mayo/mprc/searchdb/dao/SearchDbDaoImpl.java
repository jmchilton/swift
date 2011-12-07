package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.config.RuntimeInitializer;
import edu.mayo.mprc.database.DaoBase;
import edu.mayo.mprc.database.DatabasePlaceholder;
import edu.mayo.mprc.swift.db.SwiftDao;

import java.util.List;
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
		return null; //TODO: implement me
	}

	@Override
	public ProteinSequence getProteinSequence(int proteinId) {
		return null; //TODO: implement me
	}

	@Override
	public PeptideSequence addPeptideSequence(String sequence) {
		return null; //TODO: implement me
	}

	@Override
	public PeptideSequence getPeptideSequence(int peptideId) {
		return null; //TODO: implement me
	}

	@Override
	public List<Integer> getPeptidesForProtein(int proteinId) {
		return null; //TODO: implement me
	}

	@Override
	public List<Integer> getProteinsForPeptide(int peptideId) {
		return null; //TODO: implement me
	}
}
