package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("DisplayThresholds")
public final class DisplayThresholds {
	@XStreamAlias("name")
	@XStreamAsAttribute
	private String name;

	@XStreamAlias("proteinProbability")
	@XStreamAsAttribute
	private double proteinProbability;

	@XStreamAlias("peptideProbability")
	@XStreamAsAttribute
	private double peptideProbability;

	@XStreamAlias("minimumPeptideCount")
	@XStreamAsAttribute
	private int minimumPeptideCount;

	@XStreamAlias("minimumNTT")
	@XStreamAsAttribute
	private int minimumNTT;

	@XStreamAlias("useCharge")
	@XStreamAsAttribute
	private String useCharge;

	@XStreamAlias("useMergedPeptideProbability")
	@XStreamAsAttribute
	private boolean useMergedPeptideProbability;

	@XStreamImplicit
	private List<AbstractProgramSpecificThreshold> thresholds;

	public DisplayThresholds() {
	}

	public DisplayThresholds(final String name, final double proteinProbability, final double peptideProbability, final int minimumPeptideCount, final int minimumNTT, final String useCharge, final boolean useMergedPeptideProbability, final List<AbstractProgramSpecificThreshold> thresholds) {
		this.name = name;
		this.proteinProbability = proteinProbability;
		this.peptideProbability = peptideProbability;
		this.minimumPeptideCount = minimumPeptideCount;
		this.minimumNTT = minimumNTT;
		this.useCharge = useCharge;
		this.useMergedPeptideProbability = useMergedPeptideProbability;
		this.thresholds = thresholds;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public double getProteinProbability() {
		return proteinProbability;
	}

	public void setProteinProbability(final double proteinProbability) {
		this.proteinProbability = proteinProbability;
	}

	public double getPeptideProbability() {
		return peptideProbability;
	}

	public void setPeptideProbability(final double peptideProbability) {
		this.peptideProbability = peptideProbability;
	}

	public int getMinimumPeptideCount() {
		return minimumPeptideCount;
	}

	public void setMinimumPeptideCount(final int minimumPeptideCount) {
		this.minimumPeptideCount = minimumPeptideCount;
	}

	public int getMinimumNTT() {
		return minimumNTT;
	}

	public void setMinimumNTT(final int minimumNTT) {
		this.minimumNTT = minimumNTT;
	}

	public String getUseCharge() {
		return useCharge;
	}

	public void setUseCharge(final String useCharge) {
		this.useCharge = useCharge;
	}

	public boolean isUseMergedPeptideProbability() {
		return useMergedPeptideProbability;
	}

	public void setUseMergedPeptideProbability(final boolean useMergedPeptideProbability) {
		this.useMergedPeptideProbability = useMergedPeptideProbability;
	}

	public List<AbstractProgramSpecificThreshold> getThresholds() {
		return thresholds;
	}

	public void setThresholds(final List<AbstractProgramSpecificThreshold> thresholds) {
		this.thresholds = thresholds;
	}
}
