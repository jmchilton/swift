package edu.mayo.mprc.searchdb.dao;

import edu.mayo.mprc.database.Dao;

/**
 * This dao should be implemented in an efficient manner. Typically a large amount of queries (10000x per input file)
 * is going to be run when adding peptide/protein sequences.
 */
public interface SearchDbDao extends Dao {
    LocalizedModification addLocalizedModification(LocalizedModification mod);

    IdentifiedPeptide addIdentifiedPeptide(IdentifiedPeptide peptide);

    PeptideSpectrumMatch addPeptideSpectrumMatch(PeptideSpectrumMatch match);

    ProteinGroup addProteinGroup(ProteinGroup group);

    TandemMassSpectrometrySample addTandemMassSpectrometrySample(TandemMassSpectrometrySample sample);

    SearchResult addSearchResult(SearchResult searchResult);

    BiologicalSample addBiologicalSample(BiologicalSample biologicalSample);

    Analysis addAnalysis(Analysis analysis);
}
