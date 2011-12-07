package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.Dao;

import java.util.List;

public interface SearchDbDao extends Dao {
	/**
	 * Look up given protein sequence in the database. Return an object containing the sequence ID to be referenced.
	 * If the sequence does not exist in the database, it is added.
	 *
	 * @param sequence Sequence to look up.
	 * @return ProteinSequence object with the ID set up.
	 */
	ProteinSequence addProteinSequence(String sequence);

	/**
	 * Return a protein sequence for given ID.
	 *
	 * @param proteinId Id of the sequence to return.
	 * @return Sequence from the database.
	 */
	ProteinSequence getProteinSequence(int proteinId);

	/**
	 * Look up given protein sequence in the database. Return an object containing the sequence ID to be referenced.
	 * If the sequence does not exist in the database, it is added.
	 *
	 * @param sequence Sequence to look up.
	 * @return ProteinSequence object with the ID set up.
	 */
	PeptideSequence addPeptideSequence(String sequence);

	/**
	 * Return a protein sequence for given ID.
	 *
	 * @param peptideId Id of the sequence to return.
	 * @return Sequence from the database.
	 */
	PeptideSequence getPeptideSequence(int peptideId);

	/**
	 * Return all the peptide sequences we have ever seen for a particular protein.
	 *
	 * @param proteinId Id of the protein.
	 * @return List of peptide sequence ids belonging to the protein.
	 */
	List<Integer> getPeptidesForProtein(int proteinId);

	/**
	 * Return all the protein sequences that contain a particular peptide.
	 *
	 * @param peptideId Id of the peptide.
	 * @return List of protein sequence ids containing the peptide.
	 */
	List<Integer> getProteinsForPeptide(int peptideId);
}
