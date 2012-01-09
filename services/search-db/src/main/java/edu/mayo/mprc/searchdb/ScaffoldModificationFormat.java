package edu.mayo.mprc.searchdb;

import com.google.common.base.Objects;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.searchdb.dao.LocalizedModification;
import edu.mayo.mprc.unimod.IndexedModSet;
import edu.mayo.mprc.unimod.Mod;
import edu.mayo.mprc.unimod.ModSpecificity;
import edu.mayo.mprc.unimod.Terminus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Scaffold modification format as seen in Spectrum report.
 *
 * @author Roman Zenka
 */
public final class ScaffoldModificationFormat {
	private static final double MOD_DELTA_PRECISION = 0.005;
	private IndexedModSet modSet;
	private IndexedModSet scaffoldModSet;
	/**
	 * Example format:
	 * <p/>
	 * {@code n-term: Pyro-cmC (-17.03), m17: Oxidation (+15.99)}
	 */
	private static final Pattern MOD_PATTERN = Pattern.compile("([nc]-term|[a-zA-Z][1-9][0-9]*)\\s*:\\s*(.*?)\\s*\\(([+-][0-9.]+)\\)(,\\s*|\\s*$)");

	/**
	 * @param modSet         A set of modifications that are recognized. Usually loaded from unimod.
	 * @param scaffoldModSet Scaffold's unimod configuration. We need this to be able to parse Scaffold's unimod entry names to the official ones.
	 */
	public ScaffoldModificationFormat(IndexedModSet modSet, IndexedModSet scaffoldModSet) {
		this.modSet = modSet;
		this.scaffoldModSet = scaffoldModSet;
	}

	/**
	 * Parse a given list of modification in Scaffold Spectrum report format into a canonical form.
	 * <p/>
	 * The canonical form orders the mods by their location ascending and then by the modification name
	 * in case multiple mods apply to the same spot.
	 * <p/>
	 * N-term modifications are stored with location=0;
	 * C-term modifications are stored with location= sequence length-1;
	 * <p/>
	 * Example format:
	 * <p/>
	 * {@code m17: Oxidation (+15.99)} - oxidation of Methionine at position 17 (numbered from 1)
	 *
	 * @param sequence     The sequence being modified. This is not being stored, it is used to check that the parse worked correctly.
	 * @param fixedMods    Scaffold-like list of fixed modifications.
	 * @param variableMods Scaffold-like list of variable modifications.
	 * @return Parsed list of localized modifications.
	 */
	public List<LocalizedModification> parseModifications(String sequence, String fixedMods, String variableMods) {
		final ArrayList<LocalizedModification> localizedModifications = new ArrayList<LocalizedModification>(1);

		addModifications(fixedMods.trim(), localizedModifications, sequence);
		addModifications(variableMods.trim(), localizedModifications, sequence);
		Collections.sort(localizedModifications);

		return localizedModifications;
	}

	private void addModifications(String modifications, ArrayList<LocalizedModification> mods, String sequence) {
		if (",".equals(modifications)) {
			// Nothing to do
			return;
		}
		final Matcher matcher = MOD_PATTERN.matcher(modifications);
		int lastEnd = 0;
		while (matcher.find()) {
			lastEnd = matcher.end();
			final String positionGroup = matcher.group(1);
			final String titleGroup = matcher.group(2);
			final String deltaGroup = matcher.group(3);
			double delta = Double.parseDouble(deltaGroup);

			final char residue;
			final int position;
			final Terminus terminus;
			if (positionGroup.equalsIgnoreCase("n-term")) {
				residue = sequence.charAt(0);
				terminus = Terminus.Nterm;
				position = 0;
			} else if (positionGroup.equalsIgnoreCase("c-term")) {
				residue = sequence.charAt(sequence.length() - 1);
				terminus = Terminus.Cterm;
				position = sequence.length() - 1;
			} else {
				residue = Character.toUpperCase(positionGroup.charAt(0));
				terminus = Terminus.Anywhere;
				position = Integer.parseInt(positionGroup.substring(1)) - 1;
				final char sequenceResidue = Character.toUpperCase(sequence.charAt(position));
				if (sequenceResidue != residue) {
					throw new MprcException("The modification was reported at [" + residue + "] but the peptide sequence lists [" + sequenceResidue + "]");
				}
			}

			ModSpecificity scaffoldSpecificity = matchScaffoldMod(positionGroup, titleGroup, deltaGroup, delta, residue, terminus);
			ModSpecificity modSpecificity = matchEquivalentMod(scaffoldSpecificity);

			// Now we have a mod from scaffold.

			LocalizedModification localizedModification = new LocalizedModification(modSpecificity, position, residue);
			mods.add(localizedModification);
		}
		if (lastEnd != modifications.length()) {
			throw new MprcException("Could not parse modification information [" + modifications.substring(lastEnd, modifications.length()) + "]");
		}
	}

	/**
	 * Take a mod from Scaffold, match it to the source Unimod.
	 *
	 * @param scaffoldSpecificity Scaffold mod specificity.
	 * @return Scaffold's mod matched to the actual mod specificity from our database.
	 */
	private ModSpecificity matchEquivalentMod(ModSpecificity scaffoldSpecificity) {
		final Mod scaffoldMod = scaffoldSpecificity.getModification();
		final Integer recordId = scaffoldMod.getRecordID();
		Mod matchingMod = modSet.getByRecordId(recordId);
		if (matchingMod == null) {
			matchingMod = modSet.getByTitle(scaffoldMod.getTitle());
		}
		if (!Objects.equal(matchingMod.getComposition(), scaffoldMod.getComposition())) {
			throw new MprcException("Modification reported by Scaffold does not match your unimod modification. " +
					"RecordId=[" + recordId + "], " +
					"Scaffold mod: [" + scaffoldMod.getTitle() + "], composition: [" + scaffoldMod.getComposition() + "], " +
					"Unimod mod: [" + matchingMod.getTitle() + "], composition: [" + matchingMod.getComposition() + "]");
		}
		for (ModSpecificity specificity : matchingMod.getModSpecificities()) {
			if (specificity.getSite().equals(scaffoldSpecificity.getSite())
					&& specificity.getTerm().equals(scaffoldSpecificity.getTerm())
					&& specificity.isProteinOnly().equals(scaffoldSpecificity.isProteinOnly())) {
				return specificity;
			}
		}
		throw new MprcException("Could not find a matching mod specificity for Scaffold's " + scaffoldSpecificity.toMascotString());
	}

	/**
	 * For given parsed data, find closest Scaffold mod that matches.
	 * <p/>
	 * We simply list all mods within a certain delta from specified one, that have a matching residue and terminus.
	 * <p/>
	 * Out of all those mods we pick the first one with a matching mod title. If no such mod is present, we throw an exception.
	 * <p/>
	 * HACK: See {@link #fixPyroCmc}.
	 */
	private ModSpecificity matchScaffoldMod(String position, String title, String deltaString, double delta, char residue, Terminus terminus) {
		final Set<ModSpecificity> matchingModSpecificities = scaffoldModSet.findMatchingModSpecificities(delta - MOD_DELTA_PRECISION, delta + MOD_DELTA_PRECISION, residue, terminus, null, null);

		String effectiveTitle = fixPyroCmc(title, residue, terminus);

		for (ModSpecificity specificity : matchingModSpecificities) {
			if (effectiveTitle.equalsIgnoreCase(specificity.getModification().getTitle())) {
				return specificity;
			}
		}
		throw new MprcException("Could not find a modification: [" + position + ": " + title + "(" + deltaString + ")]");
	}

	/**
	 * * HACK: There is a bug in Scaffold-bundled unimod that lists pyro-cmC to have same delta as pyro-Glu. If we run into pyro-cmC and the
	 * composition is equivalent to the broken one, we replace the mod with pyro-Glu. This happens for X!Tandem a lot.
	 */
	private String fixPyroCmc(String title, char residue, Terminus terminus) {
		String effectiveTitle = title;
		if ("Pyro-cmC".equalsIgnoreCase(title) && (residue == 'Q' || terminus == Terminus.Nterm)) {
			final Mod brokenMod = scaffoldModSet.getByTitle("Pyro-cmC");
			if (brokenMod != null && "H(-3) N(-1)".equals(brokenMod.getComposition())) {
				effectiveTitle = "Pyro-glu";
			}
		}
		return effectiveTitle;
	}
}
