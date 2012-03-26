package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import edu.mayo.mprc.chem.AminoAcidSet;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

@XStreamAlias("PeptideAnalysisIdentification")
public final class PeptideAnalysisIdentification {
	private static final Logger LOGGER = Logger.getLogger(PeptideAnalysisIdentification.class);

//<PeptideAnalysisIdentification id="10" matchProbability="0.95" primaryScoreValue="4.080922">
//                <sequence>(R)EAVCIVLSDDTCSDEK(I)</sequence>
//                <Modification id="11" name="Iodoacetamide derivative" location="3"/>
//                <Modification id="12" name="Iodoacetamide derivative" location="11"/>
//                <X_TandemScore>
//                  <score type="-Log Expect Value" value="4.080922"/>
//                  <score type="Hyper Score" value="82.2"/>
//                  <score type="Ladder Score" value="0.0"/>
//                </X_TandemScore>
//                <SpectrumAnalysisIdentification id="13" spectrum="test1 scan 92 92 (test1.92.92.2.dta)" analysisProgram="X! Tandem" charge="2"/>

	@XStreamAlias("matchProbability")
	@XStreamAsAttribute
	private double matchProbability;

	@XStreamAlias("primaryScoreValue")
	@XStreamAsAttribute
	private double primaryScoreValue;

	@XStreamAlias("sequence")
	private String sequence;

	@XStreamImplicit
	private List<Modification> modifications;

	@XStreamAlias("X_TandemScore")
	private List<Score> tandemScore;
	@XStreamAlias("MascotScore")
	private List<Score> mascotScore;
	@XStreamAlias("SequestScore")
	private List<Score> sequestScore;

	@XStreamAlias("SpectrumAnalysisIdentification")
	private SpectrumAnalysisIdentification spectrumAnalysisIdentification;

	public PeptideAnalysisIdentification() {
	}

	public double getMatchProbability() {
		return matchProbability;
	}

	public void setMatchProbability(final double matchProbability) {
		this.matchProbability = matchProbability;
	}

	public double getPrimaryScoreValue() {
		return primaryScoreValue;
	}

	public void setPrimaryScoreValue(final double primaryScoreValue) {
		this.primaryScoreValue = primaryScoreValue;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(final String sequence) {
		this.sequence = sequence;
	}

	public List<Modification> getModifications() {
		return modifications;
	}

	public void setModifications(final List<Modification> modifications) {
		this.modifications = modifications;
	}

	public List<Score> getTandemScore() {
		return tandemScore;
	}

	public void setTandemScore(final List<Score> tandemScore) {
		this.tandemScore = tandemScore;
	}

	public List<Score> getMascotScore() {
		return mascotScore;
	}

	public void setMascotScore(final List<Score> mascotScore) {
		this.mascotScore = mascotScore;
	}

	public List<Score> getSequestScore() {
		return sequestScore;
	}

	public void setSequestScore(final List<Score> sequestScore) {
		this.sequestScore = sequestScore;
	}

	public SpectrumAnalysisIdentification getSpectrumAnalysisIdentification() {
		return spectrumAnalysisIdentification;
	}

	/**
	 * Strips the neighboring amino acids from the sequence, e.g. <code>(K)ADFGH(R)</code> becomes
	 * <code>ADFGH</code>.
	 * Assumes for the sake of simplicity that the sequence is in form <code>[(X)]X+[(X)]</code>.
	 */
	public static String stripNeighborAminoAcids(final String sequence) {
		int start = 0;
		int end = sequence.length();
		if (sequence.charAt(end - 1) == ')') {
			end -= 3;
		}
		if (sequence.charAt(0) == '(') {
			start += 3;
		}
		return sequence.substring(start, end);
	}

	public String getSequenceWithoutNeighbors() {
		return stripNeighborAminoAcids(sequence);
	}

	public double getMonoisotopicMass(final AminoAcidSet aminoAcids) {
		return aminoAcids.getMonoisotopicMass(getSequenceWithoutNeighbors());
	}

	/**
	 * Checks whether the modification is defined, if not, warns and returns 0.
	 *
	 * @param modificationMap
	 * @param mod
	 * @return
	 */
	private double getCheckedModificationMass(final Map<String, Double> modificationMap, final Modification mod) {
		final Double modificationMass = modificationMap.get(mod.getName());
		if (modificationMass != null) {
			return modificationMass;
		} else {
			LOGGER.warn("The modification [" + mod.getName() + "] is not in our list");
			return 0;
		}
	}

	public double getModificationsMassShift(final Map<String, Double> modificationMap) {
		double mass = 0;
		if (getModifications() != null) {
			for (final Modification mod : getModifications()) {
				mass += getCheckedModificationMass(modificationMap, mod);
			}
		}
		return mass;
	}

	public double getModificationsMassShift(final Map<String, Double> modificationMap, final ModOverrideList modOverrides) {
		double mass = 0;
		if (getModifications() != null) {
			for (final Modification mod : getModifications()) {
				if (!modOverrides.isOverriden(mod.getName())) {
					mass += getCheckedModificationMass(modificationMap, mod);
				} else {
					mass += modOverrides.getMassShift(mod.getName());
				}
			}
		}
		return mass;
	}
}