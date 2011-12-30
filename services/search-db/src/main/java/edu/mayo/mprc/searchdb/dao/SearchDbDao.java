package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.Dao;

/**
 * This dao should be implemented in an efficient manner. Typically a large amount of queries (10000x per input file)
 * is going to be run when adding peptide/protein sequences. Many of those queries will be hitting identical objects.
 * Since all the entries are immutable, it makes sense to cache them in an indexed form to prevent extra database hits.
 */
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
}
