package edu.mayo.mprc.searchdb;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.searchdb.dao.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Caches previously seen objects, will provide identical object for same input.
 *
 * @author Roman Zenka
 */
public class SummarizerCache {
	private static final Splitter PROTEIN_ACCESSION_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
	private LinkedHashMap<String, BiologicalSample> biologicalSamples = new LinkedHashMap<String, BiologicalSample>(5);
	private HashMap</*Biological_Sample Name \t MS/MS Sample name*/String, TandemMassSpectrometrySearchResult> searchResults = new HashMap<String, TandemMassSpectrometrySearchResult>(5);
	private HashMap</*Biological_Sample Name \t Accession numbers canonicalized, joined by comma*/String, ProteinGroup> proteinGroups = new HashMap<String, ProteinGroup>(100);
	private HashMap<String, IdentifiedPeptide> identifiedPeptides = new HashMap<String, IdentifiedPeptide>(1000);
	private HashMap<String, PeptideSpectrumMatch> peptideSpectrumMatches = new HashMap<String, PeptideSpectrumMatch>(1000);
	private HashMap</*Protein accession number*/String, ProteinSequence> proteinSequences = new HashMap<String, ProteinSequence>(100);
	private HashMap</*Peptide sequence*/String, PeptideSequence> peptideSequences = new HashMap<String, PeptideSequence>(1000);
	private HashMap<String, ModificationPosition> modificationPositions = new HashMap<String, ModificationPosition>(100);

	public BiologicalSample getBiologicalSample(Analysis analysis, String sampleName, String category) {
		final BiologicalSample sample = biologicalSamples.get(sampleName);
		if (sample == null) {
			final BiologicalSample newSample = new BiologicalSample(sampleName, category, new ArrayList<TandemMassSpectrometrySearchResult>(1));
			biologicalSamples.put(sampleName, newSample);
			analysis.getBiologicalSamples().add(newSample);
			return newSample;
		}
		return sample;
	}

	public TandemMassSpectrometrySearchResult getTandemMassSpecResult(BiologicalSample biologicalSample, String msmsSampleName) {
		String key = biologicalSample.getSampleName() + '\t' + msmsSampleName;
		final TandemMassSpectrometrySearchResult searchResult = searchResults.get(key);
		if (searchResult == null) {
			final TandemMassSpectrometrySearchResult newSearchResult = new TandemMassSpectrometrySearchResult(null, new ArrayList<ProteinGroup>(100));
			searchResults.put(key, newSearchResult);
			biologicalSample.getSearchResults().add(newSearchResult);
			return newSearchResult;
		}
		return searchResult;
	}

	public ProteinGroup getProteinGroup(BiologicalSample biologicalSample,
	                                    TandemMassSpectrometrySearchResult tandemMassSpecResult,
	                                    String proteinAccessionNumbers, int numberOfTotalSpectra,
	                                    int numberOfUniquePeptides, int numberOfUniqueSpectra,
	                                    double percentageOfTotalSpectra, double percentageSequenceCoverage,
	                                    double proteinIdentificationProbability) {
		final String[] accNums = Iterables.toArray(PROTEIN_ACCESSION_SPLITTER.split(proteinAccessionNumbers), String.class);
		Arrays.sort(accNums, String.CASE_INSENSITIVE_ORDER);
		final int numProteins = accNums.length;
		final String canonicalizedAccNums = Joiner.on(',').join(accNums);
		final String key = biologicalSample.getSampleName() + '\t' + canonicalizedAccNums;

		final ProteinGroup proteinGroup = proteinGroups.get(key);
		if (proteinGroup == null) {
			final ProteinGroup newProteinGroup = new ProteinGroup();
			ArrayList<ProteinSequence> proteinSequences = new ArrayList<ProteinSequence>(numProteins);
			for (String accessionNumber : accNums) {
				proteinSequences.add(getProteinSequence(accessionNumber));
			}
			newProteinGroup.setProteinSequences(proteinSequences);
			newProteinGroup.setPeptideSpectrumMatches(new ArrayList<PeptideSpectrumMatch>(5));

			newProteinGroup.setNumberOfTotalSpectra(numberOfTotalSpectra);
			newProteinGroup.setNumberOfUniquePeptides(numberOfUniquePeptides);
			newProteinGroup.setNumberOfUniqueSpectra(numberOfUniqueSpectra);
			newProteinGroup.setPercentageOfTotalSpectra(percentageOfTotalSpectra);
			newProteinGroup.setPercentageSequenceCoverage(percentageSequenceCoverage);
			newProteinGroup.setProteinIdentificationProbability(proteinIdentificationProbability);

			tandemMassSpecResult.getProteinGroups().add(newProteinGroup);
			proteinGroups.put(key, newProteinGroup);
			return newProteinGroup;
		}

		// Make sure that two consecutive lines for the same protein group have all values matching to what we already extracted
		checkConsistencyWithinSample(biologicalSample, "number of total spectra", proteinGroup.getNumberOfTotalSpectra(), numberOfTotalSpectra);
		checkConsistencyWithinSample(biologicalSample, "number of unique peptides", proteinGroup.getNumberOfUniquePeptides(), numberOfUniquePeptides);
		checkConsistencyWithinSample(biologicalSample, "number of unique spectra", proteinGroup.getNumberOfUniqueSpectra(), numberOfUniqueSpectra);
		checkConsistencyWithinSample(biologicalSample, "percentage of total spectra", proteinGroup.getPercentageOfTotalSpectra(), percentageOfTotalSpectra);
		checkConsistencyWithinSample(biologicalSample, "percentage of sequence coverage", proteinGroup.getPercentageSequenceCoverage(), percentageSequenceCoverage);
		checkConsistencyWithinSample(biologicalSample, "protein identification probability", proteinGroup.getProteinIdentificationProbability(), proteinIdentificationProbability);
		return proteinGroup;
	}

	private void checkConsistencyWithinSample(BiologicalSample biologicalSample, String column, int previousValue, int currentValue) {
		checkConsistencyWithinSample(biologicalSample, column, String.valueOf(previousValue), String.valueOf(currentValue));
	}

	private void checkConsistencyWithinSample(BiologicalSample biologicalSample, String column, double previousValue, double currentValue) {
		checkConsistencyWithinSample(biologicalSample, column, String.valueOf(previousValue), String.valueOf(currentValue));
	}

	private void checkConsistencyWithinSample(BiologicalSample biologicalSample, String column, String previousValue, String currentValue) {
		if (!Objects.equal(previousValue, currentValue)) {
			throw new MprcException("The protein group for biological sample [" + biologicalSample.getSampleName() + "] has conflicting " + column + " value, was previously [" + previousValue + "] now is [" + currentValue + "]");
		}
	}

	private ProteinSequence getProteinSequence(String accessionNumber) {
		final ProteinSequence proteinSequence = proteinSequences.get(accessionNumber);
		if (proteinSequence == null) {
			final ProteinSequence newProteinSequence = new ProteinSequence(null);
			proteinSequences.put(accessionNumber, newProteinSequence);
			return newProteinSequence;
		}
		return proteinSequence;
	}
}
