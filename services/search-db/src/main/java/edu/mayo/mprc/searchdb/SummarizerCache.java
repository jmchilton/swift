package edu.mayo.mprc.searchdb;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.searchdb.dao.*;
import edu.mayo.mprc.unimod.IndexedModSet;

import java.util.*;

/**
 * Caches previously seen objects, will provide identical object for same input.
 * <p/>
 * The Scaffold peptide spectrum report is basically a join on multiple tables. Some columns
 * serve as primary keys and other columns depend on their value. We check this assumption rigorously,
 * because a failure in this assessment means either that we do not understand the report format correctly,
 * or that the report is corrupted.
 *
 * @author Roman Zenka
 */
final class SummarizerCache {
	private static final Splitter PROTEIN_ACCESSION_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
	private static final int EXPECTED_PEPTIDES_PER_PROTEIN = 5;

	/**
	 * Indexed set of modifications to translate the Scaffold mods into actual Mod objects
	 */
	private IndexedModSet modSet;

	private LinkedHashMap</*Biological sample name*/String, BiologicalSample> biologicalSamples = new LinkedHashMap<String, BiologicalSample>(5);
	private Map<SearchResultKey, SearchResult> searchResults = new HashMap<SearchResultKey, SearchResult>(5);
	private Map<ProteinGroupKey, ProteinGroup> proteinGroups = new HashMap<ProteinGroupKey, ProteinGroup>(100);
	private Map<PeptideSpectrumMatchKey, PeptideSpectrumMatch> peptideSpectrumMatches = new HashMap<PeptideSpectrumMatchKey, PeptideSpectrumMatch>(1000);
	private Map</*Protein accession number*/String, ProteinSequence> proteinSequences = new HashMap<String, ProteinSequence>(100);
	private Map<IdentifiedPeptide, IdentifiedPeptide> identifiedPeptides = new HashMap<IdentifiedPeptide, IdentifiedPeptide>(1000);
	private Map</*Peptide sequence*/String, PeptideSequence> peptideSequences = new HashMap<String, PeptideSequence>(1000);
	private Map<LocalizedModification, LocalizedModification> localizedModifications = new HashMap<LocalizedModification, LocalizedModification>(100);

	SummarizerCache(IndexedModSet modSet) {
		this.modSet = modSet;
	}

	/**
	 * Get current biological sample object. If we encounter a new one, create a new one and add it to
	 * the {@link Analysis}.
	 *
	 * @param analysis   Sample is part of this.
	 * @param sampleName Primary key for {@link BiologicalSample}
	 * @param category   Category of the sample. Depends on {@code sampleName}
	 * @return The current sample.
	 */
	public BiologicalSample getBiologicalSample(Analysis analysis, String sampleName, String category) {
		final BiologicalSample sample = biologicalSamples.get(sampleName);
		if (sample == null) {
			final BiologicalSample newSample = new BiologicalSample(sampleName, category, new ArrayList<SearchResult>(1));
			biologicalSamples.put(sampleName, newSample);
			analysis.getBiologicalSamples().add(newSample);
			return newSample;
		}
		if (!Objects.equal(sample.getCategory(), category)) {
			throw new MprcException("Sample [" + sampleName + "] reported with two distinct categories [" + category + "] and [" + sample.getCategory() + "]");
		}
		return sample;
	}

	/**
	 * Get the current mass spec sample result test for given biological sample. If a new set is discovered,
	 * it is initialized and added to the biological sample.
	 *
	 * @param biologicalSample The containing biological sample.
	 * @param msmsSampleName   The name of the tandem mass spectrometry sample.
	 * @return Current tandem mass spec search result object.
	 */
	public SearchResult getTandemMassSpecResult(BiologicalSample biologicalSample, String msmsSampleName) {
		final SearchResultKey key = new SearchResultKey(biologicalSample, msmsSampleName);
		final SearchResult searchResult = searchResults.get(key);
		if (searchResult == null) {
			final SearchResult newSearchResult = new SearchResult(null, new ArrayList<ProteinGroup>(100));
			searchResults.put(key, newSearchResult);
			biologicalSample.getSearchResults().add(newSearchResult);
			return newSearchResult;
		}
		return searchResult;
	}

	/**
	 * Get current protein group for a tandem mass spec sample within a biological sample.
	 * If no such group is defined yet, create a new one and add it to the {@link SearchResult}.
	 * <p/>
	 * All the additional parameters should depend on the accession numbers as the primary key for the protein group.
	 * Check this for consistency and throw exceptions when the file is suspected to be corrupted.
	 *
	 * @param biologicalSample           Biological sample.
	 * @param tandemMassSpecResult       Result collection for one mass spec sample.
	 * @param proteinAccessionNumbers    List of protein accession numbers. The first one is the reference, preferred one.
	 * @param numberOfTotalSpectra       How many spectra in the group total.
	 * @param numberOfUniquePeptides     How many unique peptides in the group. Unique - different mods.
	 * @param numberOfUniqueSpectra      How many unique spectra - belonging to different peptides/mods or different charge.
	 * @param percentageOfTotalSpectra   How many percent of the total spectra assigned to this group (spectral counting)
	 * @param percentageSequenceCoverage How many percent of the sequence are covered.
	 * @param proteinIdentificationProbability
	 *                                   What is the calculated probability that this protein is identified correctly.
	 * @return Current protein group.
	 */
	public ProteinGroup getProteinGroup(BiologicalSample biologicalSample,
	                                    SearchResult tandemMassSpecResult,
	                                    String proteinAccessionNumbers, int numberOfTotalSpectra,
	                                    int numberOfUniquePeptides, int numberOfUniqueSpectra,
	                                    double percentageOfTotalSpectra, double percentageSequenceCoverage,
	                                    double proteinIdentificationProbability) {
		// Canonicalize the protein accession numbers- just in case
		final String[] accNums = Iterables.toArray(PROTEIN_ACCESSION_SPLITTER.split(proteinAccessionNumbers), String.class);
		final String referenceAccNum = accNums[0];

		Arrays.sort(accNums, String.CASE_INSENSITIVE_ORDER);
		final String canonicalizedAccNums = Joiner.on(',').join(accNums);
		final ProteinGroupKey key = new ProteinGroupKey(biologicalSample, canonicalizedAccNums);

		final ProteinGroup proteinGroup = proteinGroups.get(key);
		if (proteinGroup == null) {
			final ProteinGroup newProteinGroup = new ProteinGroup();
			addProteinSequences(accNums, newProteinGroup);
			newProteinGroup.setPeptideSpectrumMatches(new ArrayList<PeptideSpectrumMatch>(EXPECTED_PEPTIDES_PER_PROTEIN));

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

	private void addProteinSequences(String[] accNums, ProteinGroup newProteinGroup) {
		ArrayList<ProteinSequence> proteinSequences = new ArrayList<ProteinSequence>(accNums.length);
		for (String accessionNumber : accNums) {
			proteinSequences.add(getProteinSequence(accessionNumber));
		}
		newProteinGroup.setProteinSequences(proteinSequences);
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

	/**
	 * Get current {@link PeptideSpectrumMatch} entry. If none exist, new one is created and added to the protein group.
	 *
	 * @param biologicalSample          {@link BiologicalSample} within which we operate.
	 * @param searchResult              {@link SearchResult} within the biological sample.
	 * @param proteinGroup              Protein group to assign the PSM to.
	 * @param peptideSequence           Peptide sequence. This + fixed+variable mods form a primary key.
	 * @param fixedModifications        Fixed modifications, parsed by {@link ScaffoldModificationFormat}. Primary key.
	 * @param variableModifications     Variable modifications, parsed by {@link ScaffoldModificationFormat}. Primary key.
	 * @param previousAminoAcid         Previous amino acid in the context of this protein group.
	 * @param nextAminoAcid             Next amino acid in the context of this protein group.
	 * @param numberOfEnzymaticTerminii Number of enzymatic terminii for this peptide (0,1=semi,2=fully)
	 * @return Current peptide spectrum match information.
	 */
	public PeptideSpectrumMatch getPeptideSpectrumMatch(
			BiologicalSample biologicalSample,
			SearchResult searchResult,
			ProteinGroup proteinGroup,
			String peptideSequence,
			String fixedModifications,
			String variableModifications,
			char previousAminoAcid,
			char nextAminoAcid,
			int numberOfEnzymaticTerminii) {

		PeptideSequence sequence = getPeptideSequence(peptideSequence);
		IdentifiedPeptide identifiedPeptide = getIdentifiedPeptide(sequence, fixedModifications, variableModifications);
		final PeptideSpectrumMatchKey key = new PeptideSpectrumMatchKey(biologicalSample, searchResult, proteinGroup, identifiedPeptide);
		final PeptideSpectrumMatch match = peptideSpectrumMatches.get(key);
		if (match == null) {
			final PeptideSpectrumMatch newMatch = new PeptideSpectrumMatch();
			newMatch.setPeptide(identifiedPeptide);
			newMatch.setPreviousAminoAcid(previousAminoAcid);
			newMatch.setNextAminoAcid(nextAminoAcid);
			newMatch.setNumberOfEnzymaticTerminii(numberOfEnzymaticTerminii);
			peptideSpectrumMatches.put(key, newMatch);
			proteinGroup.getPeptideSpectrumMatches().add(newMatch);
			return newMatch;
		}
		return match;
	}

	/**
	 * Get identified peptide.
	 *
	 * @param peptideSequence       The sequence of the peptide.
	 * @param fixedModifications    Fixed modifications parseable by {@link ScaffoldModificationFormat}.
	 * @param variableModifications Variable modifications parseable by {@link ScaffoldModificationFormat}.
	 * @return Unique identified peptide entry.
	 */
	private IdentifiedPeptide getIdentifiedPeptide(
			PeptideSequence peptideSequence,
			String fixedModifications,
			String variableModifications) {
		final IdentifiedPeptide key = new IdentifiedPeptide(peptideSequence, fixedModifications, variableModifications, modSet);
		final IdentifiedPeptide peptide = identifiedPeptides.get(key);
		if (peptide == null) {
			identifiedPeptides.put(key, key);
			return key;
		}
		return peptide;
	}

	/**
	 * @param peptideSequence Peptide sequence to cache and translate.
	 * @return The corresponding PeptideSequence object. The sequence is canonicalized to uppercase.
	 */
	private PeptideSequence getPeptideSequence(String peptideSequence) {
		final String upperCaseSequence = peptideSequence.toUpperCase(Locale.US);
		final PeptideSequence sequence = peptideSequences.get(upperCaseSequence);
		if (sequence == null) {
			final PeptideSequence newSequence = new PeptideSequence(upperCaseSequence);
			peptideSequences.put(upperCaseSequence, newSequence);
			return newSequence;
		}
		return sequence;
	}

	/**
	 * @param psm            PSM to update.
	 * @param spectrumName   Name of the spectrum. In Swift-friendly format (filename.fromScan.toScan.charge.dta)
	 * @param spectrumCharge Charge as extracted by Scaffold.
	 * @param peptideIdentificationProbability
	 *                       Probability that this ID is correct as assigned by Scaffold.
	 */
	public void recordSpectrum(
			PeptideSpectrumMatch psm,
			String spectrumName,
			int spectrumCharge,
			double peptideIdentificationProbability,
			SearchEngineScores searchEngineScores
	) {
		psm.updateScores(peptideIdentificationProbability, searchEngineScores);
		psm.addSpectrum(spectrumCharge);
	}
}
