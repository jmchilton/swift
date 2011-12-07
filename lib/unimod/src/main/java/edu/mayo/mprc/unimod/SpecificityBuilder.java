package edu.mayo.mprc.unimod;

import edu.mayo.mprc.chem.AminoAcid;

/**
 * Builds an unmodifiable specificity. Use together with ModBuilder (a specificity cannot exist without its {@link Mod}).
 */
public final class SpecificityBuilder {
	private AminoAcid site;
	private Terminus term;
	private Boolean proteinOnly;
	private Boolean hidden;
	private String classification;
	private Integer specificityGroup;
	private StringBuilder comments = new StringBuilder();

	public SpecificityBuilder(AminoAcid site, Terminus term, Boolean proteinOnly, Boolean hidden, String classification, Integer specificityGroup) {
		this.site = site;
		this.term = term;
		this.proteinOnly = proteinOnly;
		this.hidden = hidden;
		this.classification = classification;
		this.specificityGroup = specificityGroup;
	}

	public Boolean isHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public AminoAcid getSite() {
		return site;
	}

	public void setSite(AminoAcid site) {
		this.site = site;
	}

	public Terminus getTerm() {
		return term;
	}

	public void setTerm(Terminus term) {
		this.term = term;
	}

	public Boolean isProteinOnly() {
		return proteinOnly;
	}

	public void setProteinOnly(Boolean proteinOnly) {
		this.proteinOnly = proteinOnly;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public Integer getSpecificityGroup() {
		return specificityGroup;
	}

	public void setSpecificityGroup(Integer specificityGroup) {
		this.specificityGroup = specificityGroup;
	}

	public String getComments() {
		return comments.toString();
	}

	public void setComments(String comments) {
		this.comments.setLength(0);
		this.comments.append(comments);
	}

	public void addComment(String comment) {
		if (this.comments.length() > 0) {
			this.comments.append("; ").append(comment);
		} else {
			this.comments.append(comment);
		}
	}

	public ModSpecificity build(Mod parent) {
		return new ModSpecificity(parent, site == null ? null : site.getCode(), term, proteinOnly, hidden, classification, specificityGroup, getComments());
	}
}
