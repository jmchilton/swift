package edu.mayo.mprc.searchdb.dao;

import com.google.common.base.Joiner;
import edu.mayo.mprc.database.PersistableBase;
import edu.mayo.mprc.swift.dbmapping.ReportData;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

/**
 * Extracted information about how the analysis was performed that should get stored into the LIMS.
 * <p/>
 * Analysis is an equivalent of one Scaffold execution.
 *
 * @author Roman Zenka
 */
public final class Analysis extends PersistableBase {
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00%", DecimalFormatSymbols.getInstance(Locale.US));

    /**
     * Which search report (read = Scaffold file) does this Analysis link to?
     */
    private ReportData reportData;

    /**
     * Scaffold version as a string. Can be null if the version could not be determined.
     */
    private String scaffoldVersion;

    /**
     * Date and time of when the analysis was run, as recorded in the report. This information
     * is somewhat duplicated (we know when the user submitted the search).
     * It can be null if the date could not be determined.
     */
    private Date analysisDate;

    /**
     * A list of all biological samples defined within the Scaffold analysis report.
     */
    private BiologicalSampleList biologicalSamples;

    /**
     * Empty constructor for Hibernate.
     */
    public Analysis() {
    }

    public Analysis(ReportData reportData, String scaffoldVersion, Date analysisDate, BiologicalSampleList biologicalSamples) {
        this.reportData = reportData;
        this.scaffoldVersion = scaffoldVersion;
        this.analysisDate = analysisDate;
        this.biologicalSamples = biologicalSamples;
    }

    public ReportData getReportData() {
        return reportData;
    }

    public void setReportData(ReportData reportData) {
        this.reportData = reportData;
    }

    public String getScaffoldVersion() {
        return scaffoldVersion;
    }

    public void setScaffoldVersion(String scaffoldVersion) {
        this.scaffoldVersion = scaffoldVersion;
    }

    public Date getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(Date analysisDate) {
        this.analysisDate = analysisDate;
    }

    public BiologicalSampleList getBiologicalSamples() {
        return biologicalSamples;
    }

    public void setBiologicalSamples(BiologicalSampleList biologicalSamples) {
        this.biologicalSamples = biologicalSamples;
    }

    /**
     * Emulates the Scaffold's peptide report.
     *
     * @return String similar to Scaffold's peptide report. For testing mostly.
     */
    public String peptideReport() {
        StringBuilder builder = new StringBuilder();
        builder.append(
                "Experiment name\t" +
                        "Biological sample category\t" +
                        "Biological sample name\t" +
                        "MS/MS sample name\t" +
                        "Protein name\t" +
                        "Protein accession numbers\t" +
                        "Database sources\t" +
                        "Protein molecular weight (Da)\t" +
                        "Protein identification probability\t" +
                        "Number of Unique Peptides\t" +
                        "Number of Unique Spectra\t" +
                        "Number of Total Spectra\t" +
                        "Percentage of Total Spectra\t" +
                        "Percentage Sequence Coverage\t" +
                        "Peptide Sequence\t" +
                        "Peptide Modifications\t" +
                        "Previous Amino Acid\t" +
                        "Next Amino Acid\t" +
                        "Best Peptide Identification Probability\t" +
                        "Best SEQUEST XCorr score\t" +
                        "Best SEQUEST DCn score\t" +
                        "Best Mascot Ion score\t" +
                        "Best Mascot Identity score\t" +
                        "Best Mascot Delta Ion score\t" +
                        "Best X! Tandem -log(e) score\t" +
                        "Number of identified +1H spectra\t" +
                        "Number of identified +2H spectra\t" +
                        "Number of identified +3H spectra\t" +
                        "Number of identified +4H spectra\t" +
                        "Number of enzymatic termini\n");
        for (BiologicalSample sample : getBiologicalSamples()) {
            for (SearchResult result : sample.getSearchResults()) {
                for (ProteinGroup proteinGroup : result.getProteinGroups()) {
                    for (PeptideSpectrumMatch psm : proteinGroup.getPeptideSpectrumMatches()) {
                        builder
                                .append("").append('\t')
                                .append(sample.getCategory()).append('\t')
                                .append(sample.getSampleName()).append('\t')
                                .append("").append('\t')
                                .append("").append('\t')
                                .append("").append('\t')
                                .append("").append('\t')
                                .append("").append('\t')
                                .append(percent(proteinGroup.getProteinIdentificationProbability())).append('\t')
                                .append(proteinGroup.getNumberOfUniquePeptides()).append('\t')
                                .append(proteinGroup.getNumberOfUniqueSpectra()).append('\t')
                                .append(proteinGroup.getNumberOfTotalSpectra()).append('\t')
                                .append(percent(proteinGroup.getPercentageOfTotalSpectra())).append('\t')
                                .append(percent(proteinGroup.getPercentageSequenceCoverage())).append('\t')
                                .append(psm.getPeptide().getSequence().getSequence()).append('\t')
                                .append(psm.getPeptide().getModificationsAsString()).append('\t')
                                .append(psm.getPreviousAminoAcid()).append('\t')
                                .append(psm.getNextAminoAcid()).append('\t')
                                .append(psm.getBestPeptideIdentificationProbability()).append('\t')
                                .append(psm.getBestSearchEngineScores().getSequestXcorrScore()).append('\t')
                                .append(psm.getBestSearchEngineScores().getSequestDcnScore()).append('\t')
                                .append(psm.getBestSearchEngineScores().getMascotIonScore()).append('\t')
                                .append(psm.getBestSearchEngineScores().getMascotIdentityScore()).append('\t')
                                .append(psm.getBestSearchEngineScores().getMascotDeltaIonScore()).append('\t')
                                .append(psm.getBestSearchEngineScores().getTandemHyperScore()).append('\t')
                                .append(psm.getSpectrumIdentificationCounts().getNumberOfIdentified1HSpectra()).append('\t')
                                .append(psm.getSpectrumIdentificationCounts().getNumberOfIdentified2HSpectra()).append('\t')
                                .append(psm.getSpectrumIdentificationCounts().getNumberOfIdentified3HSpectra()).append('\t')
                                .append(psm.getSpectrumIdentificationCounts().getNumberOfIdentified4HSpectra()).append('\t')
                                .append(psm.getNumberOfEnzymaticTerminii()).append('\n');
                    }
                }
            }
        }
        return builder.toString();
    }

    private String percent(double percent) {
        return PERCENT_FORMAT.format(percent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Analysis analysis = (Analysis) o;

        if (getAnalysisDate() != null ? !getAnalysisDate().equals(analysis.getAnalysisDate()) : analysis.getAnalysisDate() != null)
            return false;
        if (getBiologicalSamples() != null ? !getBiologicalSamples().equals(analysis.getBiologicalSamples()) : analysis.getBiologicalSamples() != null)
            return false;
        if (getReportData() != null ? !getReportData().equals(analysis.getReportData()) : analysis.getReportData() != null)
            return false;
        if (getScaffoldVersion() != null ? !getScaffoldVersion().equals(analysis.getScaffoldVersion()) : analysis.getScaffoldVersion() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getReportData() != null ? getReportData().hashCode() : 0;
        result = 31 * result + (getScaffoldVersion() != null ? getScaffoldVersion().hashCode() : 0);
        result = 31 * result + (getAnalysisDate() != null ? getAnalysisDate().hashCode() : 0);
        result = 31 * result + (getBiologicalSamples() != null ? getBiologicalSamples().hashCode() : 0);
        return result;
    }

    /**
     * Report information about entire analysis into a given writer in HTML format.
     */
    public void htmlReport(Report r, SearchDbDao searchDbDao) {
        r
                .startTable("Scaffold run")
                .addKeyValueTable("Date", getAnalysisDate())
                .addKeyValueTable("Scaffold Version", getScaffoldVersion())
                .endTable();

        r.startTable("Results") // -- Results
                .cell("", 1);

        TreeMap<Integer, ProteinGroup> allProteinGroups = new TreeMap<Integer, ProteinGroup>();

        // List biological samples
        for (BiologicalSample sample : getBiologicalSamples()) {
            r.cell(sample.getSampleName(), sample.getSearchResults().size());

            for (SearchResult result : sample.getSearchResults()) {
                for (ProteinGroup group : result.getProteinGroups()) {
                    allProteinGroups.put(group.getId(), group);
                }
            }
        }
        r.nextRow(); // ---------------

        r.cell("");

        // List all mass-spec samples within the biological samples
        for (BiologicalSample sample : getBiologicalSamples()) {
            for (SearchResult result : sample.getSearchResults()) {
                r.cell(result.getMassSpecSample().getFile().getName());
            }
        }
        r.nextRow(); // ---------------

        for (ProteinGroup proteinGroup : allProteinGroups.values()) {
            List<String> proteinAccessionNumbers = searchDbDao.getProteinAccessionNumbers(proteinGroup.getProteinSequences());
            r.hCell(Joiner.on(", ").join(proteinAccessionNumbers));

            for (BiologicalSample sample : getBiologicalSamples()) {
                for (SearchResult result : sample.getSearchResults()) {
                    for (ProteinGroup g : result.getProteinGroups()) {
                        if (proteinGroup.getId().equals(g.getId())) {
                            r.cell(String.valueOf(g.getNumberOfTotalSpectra()));
                        }
                    }
                }
            }

            r.nextRow();
        }


        r.endTable();
    }
}
