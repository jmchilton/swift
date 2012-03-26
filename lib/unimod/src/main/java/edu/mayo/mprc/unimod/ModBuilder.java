package edu.mayo.mprc.unimod;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.chem.AminoAcid;
import edu.mayo.mprc.chem.AminoAcidSet;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Builds an instance of a {@link Mod}, step by step, since the mods themselves are immutable.
 */
public final class ModBuilder {
	private String title;
	private String fullName;
	private Integer recordID;
	private Double massMono;
	private Double massAverage;
	private String composition;
	private Set<String> altNames = new TreeSet<String>();
	private Set<SpecificityBuilder> specificities = new HashSet<SpecificityBuilder>();

	public Mod build() {
		return new Mod(title, fullName, recordID, massMono, massAverage, composition, altNames, specificities, true);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(final String fullName) {
		this.fullName = fullName;
	}

	public Integer getRecordID() {
		return recordID;
	}

	public void setRecordID(final Integer recordID) {
		this.recordID = recordID;
	}

	public Double getMassMono() {
		return massMono;
	}

	public void setMassMono(final Double massMono) {
		this.massMono = massMono;
	}

	public Double getMassAverage() {
		return massAverage;
	}

	public void setMassAverage(final Double massAverage) {
		this.massAverage = massAverage;
	}

	public String getComposition() {
		return composition;
	}

	public void setComposition(final String composition) {
		this.composition = composition;
	}

	public Set<String> getAltNames() {
		return altNames;
	}

	public void setAltNames(final Set<String> altNames) {
		this.altNames = altNames;
	}

	/**
	 * Create mod specificity given information from unimod.
	 * <p/>
	 * This corresponds to the umod:specificity element in unimod.  This is specified by a pair of
	 * strings "site" and "position."  In the version of unimod that I have (checked in as of this revision),
	 * here are <b>all</b> the unique values for these two strings:
	 * <UL>
	 * <LI>site="A" position="Anywhere"
	 * <LI>site="A" position="Protein N-term"
	 * <LI>site="C-term" position="Any C-term"
	 * <LI>site="C-term" position="Anywhere"
	 * <LI>site="C-term" position="Protein C-term"
	 * <LI>site="C" position="Any N-term"
	 * <LI>site="C" position="Anywhere"
	 * <LI>site="C" position="Protein N-term"
	 * <LI>site="D" position="Anywhere"
	 * <LI>site="E" position="Any N-term"
	 * <LI>site="E" position="Anywhere"
	 * <LI>site="F" position="Anywhere"
	 * <LI>site="F" position="Protein N-term"
	 * <LI>site="G" position="Any N-term"
	 * <LI>site="G" position="Anywhere"
	 * <LI>site="G" position="Protein C-term"
	 * <LI>site="G" position="Protein N-term"
	 * <LI>site="H" position="Anywhere"
	 * <LI>site="I" position="Anywhere"
	 * <LI>site="K" position="Anywhere"
	 * <LI>site="K" position="Protein C-term"
	 * <LI>site="L" position="Anywhere"
	 * <LI>site="M" position="Any C-term"
	 * <LI>site="M" position="Anywhere"
	 * <LI>site="M" position="Protein N-term"
	 * <LI>site="N-term" position="Any N-term"
	 * <LI>site="N-term" position="Anywhere"
	 * <LI>site="N-term" position="Protein N-term"
	 * <LI>site="N" position="Anywhere"
	 * <LI>site="N" position="Protein C-term"
	 * <LI>site="P" position="Anywhere"
	 * <LI>site="P" position="Protein N-term"
	 * <LI>site="Q" position="Any N-term"
	 * <LI>site="Q" position="Anywhere"
	 * <LI>site="Q" position="Protein C-term"
	 * <LI>site="R" position="Anywhere"
	 * <LI>site="S" position="Anywhere"
	 * <LI>site="S" position="Protein N-term"
	 * <LI>site="T" position="Anywhere"
	 * <LI>site="T" position="Protein N-term"
	 * <LI>site="V" position="Anywhere"
	 * <LI>site="V" position="Protein N-term"
	 * <LI>site="W" position="Anywhere"
	 * <LI>site="Y" position="Anywhere"
	 * </UL>
	 * <p/>
	 * As you can see, site specifies an amino acid (or n- or c-term) and position optionally further
	 * qualifies this location to the N- or C-terminus of the peptide (eg "Any N-term") or protein
	 * ("Protein N-term").
	 *
	 * @param site     Site - amino acid or terminus
	 * @param position - Terminus/anywhere.
	 * @return Mod specificity corresponding to the unimod entry.
	 */
	public SpecificityBuilder addSpecificityFromUnimod(final String site, final String position, final boolean hidden, final String classification, final Integer specificityGroup) {
		Terminus terminus;
		boolean proteinOnly = false;
		AminoAcid acidSite = null;

		if (ModSpecificity.SITE_N_TERM.equalsIgnoreCase(site)) {
			terminus = Terminus.Nterm;
		} else if (ModSpecificity.SITE_C_TERM.equalsIgnoreCase(site)) {
			terminus = Terminus.Cterm;
		} else {
			terminus = Terminus.Anywhere;
			if (site.length() == 1) {
				acidSite = AminoAcidSet.DEFAULT.getForSingleLetterCode(site);
			}
		}

		if (ModSpecificity.POSITION_PROTEIN_C_TERM.equalsIgnoreCase(position)) {
			terminus = Terminus.Cterm;
			proteinOnly = true;
		} else if (ModSpecificity.POSITION_PROTEIN_N_TERM.equalsIgnoreCase(position)) {
			terminus = Terminus.Nterm;
			proteinOnly = true;
		} else if (ModSpecificity.POSITION_ANY_C_TERM.equalsIgnoreCase(position)) {
			terminus = Terminus.Cterm;
			proteinOnly = false;
		} else if (ModSpecificity.POSITION_ANY_N_TERM.equalsIgnoreCase(position)) {
			terminus = Terminus.Nterm;
			proteinOnly = false;
		} else if (ModSpecificity.POSITION_ANYWHERE.equalsIgnoreCase(position)) {
			// We already have everything set up. It could be ?-term Anywhere or AA Anywhere.
			doNothing();
		} else {
			throw new MprcException("Unsupported modification position: " + position);
		}

		final SpecificityBuilder specBuilder = new SpecificityBuilder(acidSite, terminus, proteinOnly, hidden, classification, specificityGroup);
		specificities.add(specBuilder);
		return specBuilder;
	}

	/**
	 * No work needs to be done - marker method.
	 */
	private void doNothing() {

	}

	public SpecificityBuilder addSpecificity(final String title, final String site, final Terminus terminus, final boolean proteinOnly, final Integer specificityGroup) {
		final SpecificityBuilder specificityBuilder = new SpecificityBuilder(
				AminoAcidSet.DEFAULT.getForSingleLetterCode(site),
				terminus,
				proteinOnly,
				false,
				null,
				specificityGroup);
		specificities.add(specificityBuilder);
		return specificityBuilder;
	}
}
