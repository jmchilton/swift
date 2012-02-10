package edu.mayo.mprc.searchdb.builder;

import edu.mayo.mprc.searchdb.dao.ProteinGroup;
import edu.mayo.mprc.searchdb.dao.ProteinSequenceList;

/**
 * @author Roman Zenka
 */
public class ProteinGroupBuilder implements Builder<ProteinGroup> {
    private ProteinSequenceList proteinSequences;
    private PsmListBuilder peptideSpectrumMatches;
    private double proteinIdentificationProbability;
    private int numberOfUniquePeptides;
    private int numberOfUniqueSpectra;
    private int numberOfTotalSpectra;
    private double percentageOfTotalSpectra;
    private double percentageSequenceCoverage;

    private SearchResultBuilder searchResult;

    public ProteinGroupBuilder(SearchResultBuilder searchResult) {
        this.searchResult = searchResult;
        this.peptideSpectrumMatches = new PsmListBuilder(this);
    }

    @Override
    public ProteinGroup build() {
        return new ProteinGroup(proteinSequences, peptideSpectrumMatches.build(),
                proteinIdentificationProbability, numberOfUniquePeptides,
                numberOfUniqueSpectra, numberOfTotalSpectra, percentageOfTotalSpectra,
                percentageSequenceCoverage);
    }

    public SearchResultBuilder getSearchResult() {
        return searchResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProteinGroupBuilder that = (ProteinGroupBuilder) o;

        if (numberOfTotalSpectra != that.numberOfTotalSpectra) return false;
        if (numberOfUniquePeptides != that.numberOfUniquePeptides) return false;
        if (numberOfUniqueSpectra != that.numberOfUniqueSpectra) return false;
        if (Double.compare(that.percentageOfTotalSpectra, percentageOfTotalSpectra) != 0) return false;
        if (Double.compare(that.percentageSequenceCoverage, percentageSequenceCoverage) != 0) return false;
        if (Double.compare(that.proteinIdentificationProbability, proteinIdentificationProbability) != 0) return false;
        if (peptideSpectrumMatches != null ? !peptideSpectrumMatches.equals(that.peptideSpectrumMatches) : that.peptideSpectrumMatches != null)
            return false;
        if (proteinSequences != null ? !proteinSequences.equals(that.proteinSequences) : that.proteinSequences != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = proteinSequences != null ? proteinSequences.hashCode() : 0;
        result = 31 * result + (peptideSpectrumMatches != null ? peptideSpectrumMatches.hashCode() : 0);
        temp = proteinIdentificationProbability != +0.0d ? Double.doubleToLongBits(proteinIdentificationProbability) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + numberOfUniquePeptides;
        result = 31 * result + numberOfUniqueSpectra;
        result = 31 * result + numberOfTotalSpectra;
        temp = percentageOfTotalSpectra != +0.0d ? Double.doubleToLongBits(percentageOfTotalSpectra) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = percentageSequenceCoverage != +0.0d ? Double.doubleToLongBits(percentageSequenceCoverage) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public ProteinSequenceList getProteinSequences() {
        return proteinSequences;
    }

    public void setProteinSequences(ProteinSequenceList proteinSequences) {
        this.proteinSequences = proteinSequences;
    }

    public PsmListBuilder getPeptideSpectrumMatches() {
        return peptideSpectrumMatches;
    }

    public double getProteinIdentificationProbability() {
        return proteinIdentificationProbability;
    }

    public void setProteinIdentificationProbability(double proteinIdentificationProbability) {
        this.proteinIdentificationProbability = proteinIdentificationProbability;
    }

    public int getNumberOfUniquePeptides() {
        return numberOfUniquePeptides;
    }

    public void setNumberOfUniquePeptides(int numberOfUniquePeptides) {
        this.numberOfUniquePeptides = numberOfUniquePeptides;
    }

    public int getNumberOfUniqueSpectra() {
        return numberOfUniqueSpectra;
    }

    public void setNumberOfUniqueSpectra(int numberOfUniqueSpectra) {
        this.numberOfUniqueSpectra = numberOfUniqueSpectra;
    }

    public int getNumberOfTotalSpectra() {
        return numberOfTotalSpectra;
    }

    public void setNumberOfTotalSpectra(int numberOfTotalSpectra) {
        this.numberOfTotalSpectra = numberOfTotalSpectra;
    }

    public double getPercentageOfTotalSpectra() {
        return percentageOfTotalSpectra;
    }

    public void setPercentageOfTotalSpectra(double percentageOfTotalSpectra) {
        this.percentageOfTotalSpectra = percentageOfTotalSpectra;
    }

    public double getPercentageSequenceCoverage() {
        return percentageSequenceCoverage;
    }

    public void setPercentageSequenceCoverage(double percentageSequenceCoverage) {
        this.percentageSequenceCoverage = percentageSequenceCoverage;
    }
}
