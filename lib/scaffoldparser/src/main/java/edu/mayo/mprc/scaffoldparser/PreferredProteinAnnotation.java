package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("preferredProteinAnnotation")
public final class PreferredProteinAnnotation {
	@XStreamAlias("accessionNumber")
	private String accessionNumber;

	@XStreamAlias("proteinName")
	private String proteinName;

	@XStreamAlias("nameAdjusted")
	private String nameAdjusted;

	@XStreamAlias("numberAdjusted")
	private String numberAdjusted;

	public PreferredProteinAnnotation() {
	}

	public String getAccessionNumber() {
		return accessionNumber;
	}

	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public String getProteinName() {
		return proteinName;
	}

	public void setProteinName(String proteinName) {
		this.proteinName = proteinName;
	}

	public String getNameAdjusted() {
		return nameAdjusted;
	}

	public void setNameAdjusted(String nameAdjusted) {
		this.nameAdjusted = nameAdjusted;
	}

	public String getNumberAdjusted() {
		return numberAdjusted;
	}

	public void setNumberAdjusted(String numberAdjusted) {
		this.numberAdjusted = numberAdjusted;
	}
}
