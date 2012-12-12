package edu.mayo.mprc.idpicker;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Zenka
 */
public final class IdpQonvertSettings implements Serializable {
	private static final long serialVersionUID = 2585921464603786125L;

	private String chargeStateHandling = "Partition";
	private String decoyPrefix = "Reversed_";
	private boolean embedSpectrumScanTimes = false;
	private boolean embedSpectrumSources = false;
	private double gamma = 5.0;
	private boolean ignoreUnmappedPeptides = false;
	private String kernel = "Linear";
	private String massErrorHandling = "Ignore";
	private double maxFDR = 0.05;
	private double maxImportFDR = 0.25;
	private int maxResultRank = 3;
	private int maxTrainingRank = 1;
	private int minPartitionSize = 10;
	private String missedCleavagesHandling = "Ignore";
	private double nu = 0.5;
	private String outputSuffix = "";
	private boolean overwriteExistingFiles = false;
	private int polynomialDegree = 3;
	private boolean predictProbability = true;
	private String proteinDatabase = "";
	private String qonverterMethod = "MonteCarlo";
	private boolean rerankMatches = false;
	private String svmType = "CSVC";
	private String scoreInfo = "1 off myrimatch:mvh; 1 off xcorr; 1 off sequest:xcorr; 1 off sequest:deltacn; 1 off mascot:score; -1 off x!tandem:expect; 1 off x!tandem:hyperscore; -1 off ms-gf:specevalue; -1 off evalue";
	private String sourceSearchPath = ".;..";
	private String terminalSpecificityHandling = "Partition";
	private double truePositiveThreshold = 0.01;
	private boolean writeQonversionDetails = false;

	public IdpQonvertSettings() {
	}

	private static String toggle(final boolean value) {
		return value ? "1" : "0";
	}

	private static DecimalFormat FORMAT = new DecimalFormat("0.########");

	private static String dbl(final double value) {
		return FORMAT.format(value);
	}

	/**
	 * @return The list of settings as a big string to be written into a config file.
	 *         The settings should have the same effect as if specified on the command line.
	 */
	public String toConfigFile() {
		StringBuilder builder = new StringBuilder(2000);
		boolean key = true;
		for (String item : toCommandLine()) {
			if (key) {
				builder.append(item.substring(1))
						.append("=\"");
			} else {
				builder.append(item)
						.append("\"\n");
			}
			key = !key;
		}
		return builder.toString();
	}

	public List<String> toCommandLine() {
		List<String> result = new ArrayList<String>(28 * 2);
		result.add("-ChargeStateHandling");
		result.add(getChargeStateHandling());
		result.add("-DecoyPrefix");
		result.add(getDecoyPrefix());
		result.add("-EmbedSpectrumScanTimes");
		result.add(toggle(isEmbedSpectrumScanTimes()));
		result.add("-EmbedSpectrumSources");
		result.add(toggle(isEmbedSpectrumSources()));
		result.add("-Gamma");
		result.add(dbl(getGamma()));
		result.add("-IgnoreUnmappedPeptides");
		result.add(toggle(isIgnoreUnmappedPeptides()));
		result.add("-Kernel");
		result.add(getKernel());
		result.add("-MassErrorHandling");
		result.add(getMassErrorHandling());
		result.add("-MaxFDR");
		result.add(dbl(getMaxFDR()));
		result.add("-MaxImportFDR");
		result.add(dbl(getMaxImportFDR()));
		result.add("-MaxResultRank");
		result.add(Integer.toString(getMaxResultRank()));
		result.add("-MaxTrainingRank");
		result.add(Integer.toString(getMaxTrainingRank()));
		result.add("-MinPartitionSize");
		result.add(Integer.toString(getMinPartitionSize()));
		result.add("-MissedCleavagesHandling");
		result.add(getMissedCleavagesHandling());
		result.add("-Nu");
		result.add(dbl(getNu()));
		result.add("-OutputSuffix");
		result.add(getOutputSuffix());
		result.add("-OverwriteExistingFiles");
		result.add(toggle(isOverwriteExistingFiles()));
		result.add("-PolynomialDegree");
		result.add(Integer.toString(getPolynomialDegree()));
		result.add("-PredictProbability");
		result.add(toggle(isPredictProbability()));
		result.add("-ProteinDatabase");
		result.add(getProteinDatabase());
		result.add("-QonverterMethod");
		result.add(getQonverterMethod());
		result.add("-RerankMatches");
		result.add(toggle(isRerankMatches()));
		result.add("-SVMType");
		result.add(getSvmType());
		result.add("-ScoreInfo");
		result.add(getScoreInfo());
		result.add("-SourceSearchPath");
		result.add(getSourceSearchPath());
		result.add("-TerminalSpecificityHandling");
		result.add(getTerminalSpecificityHandling());
		result.add("-TruePositiveThreshold");
		result.add(dbl(getTruePositiveThreshold()));
		result.add("-WriteQonversionDetails");
		result.add(toggle(isWriteQonversionDetails()));
		return result;
	}

	public String getChargeStateHandling() {
		return chargeStateHandling;
	}

	public void setChargeStateHandling(String chargeStateHandling) {
		this.chargeStateHandling = chargeStateHandling;
	}

	public String getDecoyPrefix() {
		return decoyPrefix;
	}

	public void setDecoyPrefix(String decoyPrefix) {
		this.decoyPrefix = decoyPrefix;
	}

	public boolean isEmbedSpectrumScanTimes() {
		return embedSpectrumScanTimes;
	}

	public void setEmbedSpectrumScanTimes(boolean embedSpectrumScanTimes) {
		this.embedSpectrumScanTimes = embedSpectrumScanTimes;
	}

	public boolean isEmbedSpectrumSources() {
		return embedSpectrumSources;
	}

	public void setEmbedSpectrumSources(boolean embedSpectrumSources) {
		this.embedSpectrumSources = embedSpectrumSources;
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public boolean isIgnoreUnmappedPeptides() {
		return ignoreUnmappedPeptides;
	}

	public void setIgnoreUnmappedPeptides(boolean ignoreUnmappedPeptides) {
		this.ignoreUnmappedPeptides = ignoreUnmappedPeptides;
	}

	public String getKernel() {
		return kernel;
	}

	public void setKernel(String kernel) {
		this.kernel = kernel;
	}

	public String getMassErrorHandling() {
		return massErrorHandling;
	}

	public void setMassErrorHandling(String massErrorHandling) {
		this.massErrorHandling = massErrorHandling;
	}

	public double getMaxFDR() {
		return maxFDR;
	}

	public void setMaxFDR(double maxFDR) {
		this.maxFDR = maxFDR;
	}

	public double getMaxImportFDR() {
		return maxImportFDR;
	}

	public void setMaxImportFDR(double maxImportFDR) {
		this.maxImportFDR = maxImportFDR;
	}

	public int getMaxResultRank() {
		return maxResultRank;
	}

	public void setMaxResultRank(int maxResultRank) {
		this.maxResultRank = maxResultRank;
	}

	public int getMaxTrainingRank() {
		return maxTrainingRank;
	}

	public void setMaxTrainingRank(int maxTrainingRank) {
		this.maxTrainingRank = maxTrainingRank;
	}

	public int getMinPartitionSize() {
		return minPartitionSize;
	}

	public void setMinPartitionSize(int minPartitionSize) {
		this.minPartitionSize = minPartitionSize;
	}

	public String getMissedCleavagesHandling() {
		return missedCleavagesHandling;
	}

	public void setMissedCleavagesHandling(String missedCleavagesHandling) {
		this.missedCleavagesHandling = missedCleavagesHandling;
	}

	public double getNu() {
		return nu;
	}

	public void setNu(double nu) {
		this.nu = nu;
	}

	public String getOutputSuffix() {
		return outputSuffix;
	}

	public void setOutputSuffix(String outputSuffix) {
		this.outputSuffix = outputSuffix;
	}

	public boolean isOverwriteExistingFiles() {
		return overwriteExistingFiles;
	}

	public void setOverwriteExistingFiles(boolean overwriteExistingFiles) {
		this.overwriteExistingFiles = overwriteExistingFiles;
	}

	public int getPolynomialDegree() {
		return polynomialDegree;
	}

	public void setPolynomialDegree(int polynomialDegree) {
		this.polynomialDegree = polynomialDegree;
	}

	public boolean isPredictProbability() {
		return predictProbability;
	}

	public void setPredictProbability(boolean predictProbability) {
		this.predictProbability = predictProbability;
	}

	public String getProteinDatabase() {
		return proteinDatabase;
	}

	public void setProteinDatabase(String proteinDatabase) {
		this.proteinDatabase = proteinDatabase;
	}

	public String getQonverterMethod() {
		return qonverterMethod;
	}

	public void setQonverterMethod(String qonverterMethod) {
		this.qonverterMethod = qonverterMethod;
	}

	public boolean isRerankMatches() {
		return rerankMatches;
	}

	public void setRerankMatches(boolean rerankMatches) {
		this.rerankMatches = rerankMatches;
	}

	public String getSvmType() {
		return svmType;
	}

	public void setSvmType(String svmType) {
		this.svmType = svmType;
	}

	public String getScoreInfo() {
		return scoreInfo;
	}

	public void setScoreInfo(String scoreInfo) {
		this.scoreInfo = scoreInfo;
	}

	public String getSourceSearchPath() {
		return sourceSearchPath;
	}

	public void setSourceSearchPath(String sourceSearchPath) {
		this.sourceSearchPath = sourceSearchPath;
	}

	public String getTerminalSpecificityHandling() {
		return terminalSpecificityHandling;
	}

	public void setTerminalSpecificityHandling(String terminalSpecificityHandling) {
		this.terminalSpecificityHandling = terminalSpecificityHandling;
	}

	public double getTruePositiveThreshold() {
		return truePositiveThreshold;
	}

	public void setTruePositiveThreshold(double truePositiveThreshold) {
		this.truePositiveThreshold = truePositiveThreshold;
	}

	public boolean isWriteQonversionDetails() {
		return writeQonversionDetails;
	}

	public void setWriteQonversionDetails(boolean writeQonversionDetails) {
		this.writeQonversionDetails = writeQonversionDetails;
	}
}
