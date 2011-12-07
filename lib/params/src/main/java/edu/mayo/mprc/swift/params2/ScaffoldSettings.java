package edu.mayo.mprc.swift.params2;

import com.google.common.base.Objects;
import edu.mayo.mprc.database.PersistableBase;

/**
 * Scaffold settings - thresholds, starred proteins, exports.
 */
public class ScaffoldSettings extends PersistableBase {
	private double proteinProbability;
	private double peptideProbability;
	private int minimumPeptideCount;
	private int minimumNonTrypticTerminii;

	private StarredProteins starredProteins;

	private boolean saveOnlyIdentifiedSpectra;
	private boolean saveNoSpectra;
	private boolean connectToNCBI;
	private boolean annotateWithGOA;
	public static final double PROBABILITY_PRECISION = 1E-5;

	public ScaffoldSettings() {
	}

	private static final double DEFAULT_PEPTIDE_PROBABILITY = 0.95;
	private static final double DEFAULT_PROTEIN_PROBABILITY = 0.95;

	public static final ScaffoldSettings DEFAULT = new ScaffoldSettings(
			DEFAULT_PROTEIN_PROBABILITY,
			DEFAULT_PEPTIDE_PROBABILITY,
			2,
			1,
			null,
			false,
			false,
			false,
			false
	);

	public ScaffoldSettings(double proteinProbability, double peptideProbability, int minimumPeptideCount, int minimumNonTrypticTerminii, StarredProteins starredProteins, boolean saveOnlyIdentifiedSpectra, boolean saveNoSpectra, boolean connectToNCBI, boolean annotateWithGOA) {
		this.proteinProbability = proteinProbability;
		this.peptideProbability = peptideProbability;
		this.minimumPeptideCount = minimumPeptideCount;
		this.minimumNonTrypticTerminii = minimumNonTrypticTerminii;
		this.starredProteins = starredProteins;
		this.saveOnlyIdentifiedSpectra = saveOnlyIdentifiedSpectra;
		this.saveNoSpectra = saveNoSpectra;
		this.connectToNCBI = connectToNCBI;
		this.annotateWithGOA = annotateWithGOA;
	}

	public double getProteinProbability() {
		return proteinProbability;
	}

	public void setProteinProbability(double proteinProbability) {
		this.proteinProbability = proteinProbability;
	}

	public double getPeptideProbability() {
		return peptideProbability;
	}

	public void setPeptideProbability(double peptideProbability) {
		this.peptideProbability = peptideProbability;
	}

	public int getMinimumPeptideCount() {
		return minimumPeptideCount;
	}

	public void setMinimumPeptideCount(int minimumPeptideCount) {
		this.minimumPeptideCount = minimumPeptideCount;
	}

	public int getMinimumNonTrypticTerminii() {
		return minimumNonTrypticTerminii;
	}

	public void setMinimumNonTrypticTerminii(int minimumNonTrypticTerminii) {
		this.minimumNonTrypticTerminii = minimumNonTrypticTerminii;
	}

	public StarredProteins getStarredProteins() {
		return starredProteins;
	}

	public void setStarredProteins(StarredProteins starredProteins) {
		this.starredProteins = starredProteins;
	}

	public boolean isSaveOnlyIdentifiedSpectra() {
		return saveOnlyIdentifiedSpectra;
	}

	public void setSaveOnlyIdentifiedSpectra(boolean saveOnlyIdentifiedSpectra) {
		this.saveOnlyIdentifiedSpectra = saveOnlyIdentifiedSpectra;
	}

	public boolean isSaveNoSpectra() {
		return saveNoSpectra;
	}

	public void setSaveNoSpectra(boolean saveNoSpectra) {
		this.saveNoSpectra = saveNoSpectra;
	}

	public boolean isConnectToNCBI() {
		return connectToNCBI;
	}

	public void setConnectToNCBI(boolean connectToNCBI) {
		this.connectToNCBI = connectToNCBI;
	}

	public boolean isAnnotateWithGOA() {
		return annotateWithGOA;
	}

	public void setAnnotateWithGOA(boolean annotateWithGOA) {
		this.annotateWithGOA = annotateWithGOA;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof ScaffoldSettings)) {
			return false;
		}
		ScaffoldSettings that = (ScaffoldSettings) obj;

		return Objects.equal(this.isAnnotateWithGOA(), that.isAnnotateWithGOA()) &&
				Objects.equal(this.isConnectToNCBI(), that.isConnectToNCBI()) &&
				Objects.equal(this.getMinimumNonTrypticTerminii(), that.getMinimumNonTrypticTerminii()) &&
				Objects.equal(this.getMinimumPeptideCount(), that.getMinimumPeptideCount()) &&
				(Math.abs(this.getPeptideProbability() - that.getPeptideProbability()) <= PROBABILITY_PRECISION) &&
				(Math.abs(this.getProteinProbability() - that.getProteinProbability()) <= PROBABILITY_PRECISION) &&
				Objects.equal(this.isSaveNoSpectra(), that.isSaveNoSpectra()) &&
				Objects.equal(this.isSaveOnlyIdentifiedSpectra(), that.isSaveOnlyIdentifiedSpectra()) &&
				Objects.equal(this.getStarredProteins(), that.getStarredProteins());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getProteinProbability(),
				getPeptideProbability(),
				getMinimumPeptideCount(),
				getMinimumNonTrypticTerminii(),
				getStarredProteins(),
				isSaveOnlyIdentifiedSpectra(),
				isSaveNoSpectra(),
				isConnectToNCBI(),
				isAnnotateWithGOA());
	}

	public ScaffoldSettings copy() {
		final ScaffoldSettings scaffoldSettings = new ScaffoldSettings(this.getProteinProbability(),
				this.getPeptideProbability(),
				this.getMinimumPeptideCount(),
				this.getMinimumNonTrypticTerminii(),
				this.getStarredProteins(),
				this.isSaveOnlyIdentifiedSpectra(),
				this.isSaveNoSpectra(),
				this.isConnectToNCBI(),
				this.isAnnotateWithGOA());
		scaffoldSettings.setId(getId());
		return scaffoldSettings;
	}
}
