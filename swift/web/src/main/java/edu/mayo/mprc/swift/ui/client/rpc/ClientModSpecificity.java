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

	public ClientModSpecificity(final String name, final String term, final Character site, final Boolean proteinOnly, final String classification, final double monoisotopic, final List<String> altNames, final String composition, final String comments, final Integer recordID, final boolean hidden) {
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

	public ClientModSpecificity(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(final String term) {
		this.term = term;
	}

	public Character getSite() {
		return site;
	}

	public void setSite(final Character site) {
		this.site = site;
	}

	public Boolean getProteinOnly() {
		return proteinOnly;
	}

	public void setProteinOnly(final Boolean proteinOnly) {
		this.proteinOnly = proteinOnly;
	}

	public boolean equals(final Object o) {
		if (!(o instanceof ClientModSpecificity)) {
			return false;
		}
		final ClientModSpecificity oo = (ClientModSpecificity) o;
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

	public void setClassification(final String classification) {
		this.classification = classification;
	}

	public double getMonoisotopic() {
		return monoisotopic;
	}

	public void setMonoisotopic(final double monoisotopic) {
		this.monoisotopic = monoisotopic;
	}

	public List<String> getAltNames() {
		return altNames;
	}

	public void setAltNames(final List<String> altNames) {
		this.altNames = altNames;
	}

	public String getComposition() {
		return composition;
	}

	public void setComposition(final String composition) {
		this.composition = composition;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(final String comments) {
		this.comments = comments;
	}

	public Integer getRecordID() {
		return recordID;
	}

	public void setRecordID(final Integer recordID) {
		this.recordID = recordID;
	}

	public String toString() {
		return name;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(final boolean hidden) {
		this.hidden = hidden;
	}

	public static ClientModSpecificity cast(final ClientValue value) {
		if (!(value instanceof ClientModSpecificity)) {
			ExceptionUtilities.throwCastException(value, ClientModSpecificity.class);
			return null;
		}
		return (ClientModSpecificity) value;
	}
}
