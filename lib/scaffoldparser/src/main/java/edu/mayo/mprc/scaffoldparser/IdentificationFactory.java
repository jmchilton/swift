package edu.mayo.mprc.scaffoldparser;

public interface IdentificationFactory<T> {

	T createIdentification(
			SpectrumAnalysisIdentification spectrumAnalysisIdentification,
			PeptideAnalysisIdentification peptideAnalysisId,
			ProteinAnalysisIdentification proteinAnalysisId,
			ProteinGroup proteinGroup);
}
