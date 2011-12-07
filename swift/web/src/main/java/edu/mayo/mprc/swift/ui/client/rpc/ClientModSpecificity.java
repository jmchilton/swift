package edu.mayo.mprc.swift.ui.client.rpc;

import edu.mayo.mprc.common.client.ExceptionUtilities;
import edu.mayo.mprc.unimod.ModSpecificity;

import java.util.List;

/**
 * Client proxy of {@link ModSpecificity}
 * Note: client side, mod specificities are handle in two parts:
 * the client side value of an ModSet is a ClientModSpecificitySet
 * however, the client side allowed values is ClientModSpecificity[].
 */
public final class ClientModSpecificity implements ClientValue {
	private static final long serialVersionUID = 20101221L;
	private String name;
	private String term;
	private Character site;
	private Boolean proteinOnly;

	private String classification;
	private double monoisotopic;
	private List<String> altNames;
	private String composition;
	private String comments;
	private Integer recordID;
	private boolean hidden;

	public ClientModSpecificity() {
	}

	public ClientModSpecificity(String name, String term, Character site, Boolean proteinOnly, String classification, double monoisotopic, List<String> altNames, String composition, String comments, Integer recordID, boolean hidden) {
		this.name = name;
		this.term = term;
		this.site = site;
		this.proteinOnly = proteinOnly;
		this.classification = classification;
		this.monoisotopic = monoisotopic;
		setAltNames(altNames);
		this.composition = composition;
		this.comments = comments;
		this.recordID = recordID;
		this.hidden = hidden;
	}

	public ClientModSpecificity(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public Character getSite() {
		return site;
	}

	public void setSite(Character site) {
		this.site = site;
	}

	public Boolean getProteinOnly() {
		return proteinOnly;
	}

	public void setProteinOnly(Boolean proteinOnly) {
		this.proteinOnly = proteinOnly;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ClientModSpecificity)) {
			return false;
		}
		ClientModSpecificity oo = (ClientModSpecificity) o;
		return getName().equals(oo.getName())
				&& getSite().equals(oo.getSite())
				&& getTerm().equals(oo.getTerm())
				&& ((getProteinOnly() == null && oo.getProteinOnly() == null) || getProteinOnly().equals(oo.getProteinOnly()));
	}

	public int hashCode() {
		return getName().hashCode();
	}


	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public double getMonoisotopic() {
		return monoisotopic;
	}

	public void setMonoisotopic(double monoisotopic) {
		this.monoisotopic = monoisotopic;
	}

	public List<String> getAltNames() {
		return altNames;
	}

	public void setAltNames(List<String> altNames) {
		this.altNames = altNames;
	}

	public String getComposition() {
		return composition;
	}

	public void setComposition(String composition) {
		this.composition = composition;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Integer getRecordID() {
		return recordID;
	}

	public void setRecordID(Integer recordID) {
		this.recordID = recordID;
	}

	public String toString() {
		return name;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public static ClientModSpecificity cast(ClientValue value) {
		if (!(value instanceof ClientModSpecificity)) {
			ExceptionUtilities.throwCastException(value, ClientModSpecificity.class);
			return null;
		}
		return (ClientModSpecificity) value;
	}
}
