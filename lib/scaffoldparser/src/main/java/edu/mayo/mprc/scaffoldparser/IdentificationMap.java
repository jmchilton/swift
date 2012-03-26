package edu.mayo.mprc.scaffoldparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps all the identifications by Scaffold and makes them accessible by spectrum and m/z
 */
public final class IdentificationMap<T> {
	private Map<Integer, List<T>> spectrumMzIndex;
	private int numIds = 0;

	public IdentificationMap() {
	}

	/**
	 * Loads the map from given scaffold file.
	 *
	 * @param scaffold      Scaffold file to process.
	 * @param inputFileName Only load data for spectra coming from this file. The name is usually the name of the original
	 *                      .RAW or .mgf, with the extension dropped.<br>
	 *                      If null, all ids are loaded.
	 */
	public void loadFromScaffold(final Scaffold scaffold, final String inputFileName, final IdentificationFactory<T> idFactory) {
		spectrumMzIndex = new HashMap<Integer, List<T>>();
		for (final Experiment experiment : scaffold.getExperiments()) {
			for (final BiologicalSample sample : experiment.getBiologicalSamples()) {
				for (final TandemMassSpectrometrySample tandemMsSample : sample.getTandemMassSpectrometrySamples()) {
					for (final ProteinGroup proteinGroup : tandemMsSample.getProteinGroups()) {
						for (final ProteinAnalysisIdentification proteinAnalysisId : proteinGroup.getProteinAnalysisIdentifications()) {
							for (final PeptideGroupIdentification peptideGroupId : proteinAnalysisId.getPeptideGroupIdentifications()) {
								for (final PeptideAnalysisIdentification peptideAnalysisId : peptideGroupId.getPeptideAnalysisIdentifications()) {
									addNewIdToMap(inputFileName, peptideAnalysisId.getSpectrumAnalysisIdentification(),
											peptideAnalysisId, proteinAnalysisId, proteinGroup, idFactory);
								}
							}
						}
					}
				}
			}
		}
	}

	private void addNewIdToMap(final String inputFileName,
	                           final SpectrumAnalysisIdentification spectrumAnalysisIdentification,
	                           final PeptideAnalysisIdentification peptideAnalysisId,
	                           final ProteinAnalysisIdentification proteinAnalysisId,
	                           final ProteinGroup proteinGroup,
	                           final IdentificationFactory<T> idFactory) {
		if (inputFileName != null && !spectrumAnalysisIdentification.getSpectrumName().equals(inputFileName)) {
			return;
		}

		final T id = idFactory.createIdentification(
				spectrumAnalysisIdentification,
				peptideAnalysisId,
				proteinAnalysisId,
				proteinGroup);

		final int spectrumNumber = spectrumAnalysisIdentification.getSpectrumNumber();
		List<T> idSet = spectrumMzIndex.get(spectrumNumber);
		if (idSet == null) {
			idSet = new ArrayList<T>();
			spectrumMzIndex.put(spectrumNumber, idSet);
		}
		idSet.add(id);
		numIds++;
	}

	public List<T> getIdsForSpectrum(final int spectrumNumber) {
		return spectrumMzIndex.get(spectrumNumber);
	}

	public int getNumIds() {
		return numIds;
	}

	public Map<Integer, List<T>> getSpectrumMzIndex() {
		return spectrumMzIndex;
	}
}
