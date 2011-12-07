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

	public DisplayThresholds(String name, double proteinProbability, double peptideProbability, int minimumPeptideCount, int minimumNTT, String useCharge, boolean useMergedPeptideProbability, List<AbstractProgramSpecificThreshold> thresholds) {
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

	public void setName(String name) {
		this.name = name;
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

	public int getMinimumNTT() {
		return minimumNTT;
	}

	public void setMinimumNTT(int minimumNTT) {
		this.minimumNTT = minimumNTT;
	}

	public String getUseCharge() {
		return useCharge;
	}

	public void setUseCharge(String useCharge) {
		this.useCharge = useCharge;
	}

	public boolean isUseMergedPeptideProbability() {
		return useMergedPeptideProbability;
	}

	public void setUseMergedPeptideProbability(boolean useMergedPeptideProbability) {
		this.useMergedPeptideProbability = useMergedPeptideProbability;
	}

	public List<AbstractProgramSpecificThreshold> getThresholds() {
		return thresholds;
	}

	public void setThresholds(List<AbstractProgramSpecificThreshold> thresholds) {
		this.thresholds = thresholds;
	}
}
