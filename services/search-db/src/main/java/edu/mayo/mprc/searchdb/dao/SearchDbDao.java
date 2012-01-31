package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.Dao;
import edu.mayo.mprc.dbcurator.model.Curation;

/**
 * This dao should be implemented in an efficient manner. Typically a large amount of queries (10000x per input file)
 * is going to be run when adding peptide/protein sequences.
 */
public interface SearchDbDao extends Dao {
    /**
     * Look up given protein sequence in the database.
     * If the sequence does not exist in the database, it is added.
     * ID of the protein sequence is updated to match the database id.
     *
     * @param proteinSequence Sequence to add.
     */
    ProteinSequence addProteinSequence(ProteinSequence proteinSequence);

    /**
     * Return a protein sequence for given ID.
     *
     * @param proteinId Id of the sequence to return.
     * @return Sequence from the database.
     */
    ProteinSequence getProteinSequence(int proteinId);

    /**
     * Look up given peptide sequence in the database.
     * If the sequence does not exist in the database, it is added.
     * ID of the peptide sequence is updated to match the database id.
     *
     * @param peptideSequence Sequence to add.
     */
    PeptideSequence addPeptideSequence(PeptideSequence peptideSequence);

    /**
     * Return a protein sequence for given ID.
     *
     * @param peptideId Id of the sequence to return.
     * @return Sequence from the database.
     */
    PeptideSequence getPeptideSequence(int peptideId);

    /**
     * Add data from a given FASTA file into the database.
     *
     * @param database Database to load data for.
     */
    void addFastaDatabase(Curation database);

    LocalizedModification addLocalizedModification(LocalizedModification mod);

    IdentifiedPeptide addIdentifiedPeptide(IdentifiedPeptide peptide);

    PeptideSpectrumMatch addPeptideSpectrumMatch(PeptideSpectrumMatch match);

    ProteinGroup addProteinGroup(ProteinGroup group);

    TandemMassSpectrometrySample addTandemMassSpectrometrySample(TandemMassSpectrometrySample sample);

    SearchResult addSearchResult(SearchResult searchResult);

    BiologicalSample addBiologicalSample(BiologicalSample biologicalSample);

    Analysis addAnalysis(Analysis analysis);
}
