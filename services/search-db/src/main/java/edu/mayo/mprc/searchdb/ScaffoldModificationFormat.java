package edu.mayo.mprc.searchdb;

import com.google.common.base.Objects;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.searchdb.dao.LocalizedModification;
import edu.mayo.mprc.searchdb.dao.LocalizedModificationList;
import edu.mayo.mprc.unimod.IndexedModSet;
import edu.mayo.mprc.unimod.Mod;
import edu.mayo.mprc.unimod.ModSpecificity;
import edu.mayo.mprc.unimod.Terminus;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Scaffold modification format as seen in Spectrum report.
 *
 * @author Roman Zenka
 */
public final class ScaffoldModificationFormat {
	private static final double MOD_DELTA_PRECISION = 0.005;
	private static final String PYRO_CMC = "Pyro-cmC";
	private IndexedModSet modSet;
	private IndexedModSet scaffoldModSet;
	private Boolean pyroCmcBroken = null;
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
		setScaffoldModSet(scaffoldModSet);
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
	public LocalizedModificationList parseModifications(String sequence, String fixedMods, String variableMods) {
		final LocalizedModificationList list = new LocalizedModificationList();

		addModifications(fixedMods.trim(), list, sequence);
		addModifications(variableMods.trim(), list, sequence);
        list.fix();

		return list;
	}

	private void addModifications(String modifications, LocalizedModificationList mods, String sequence) {
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
				position = Integer.parseInt(positionGroup.substring(1)) - 1;
				// We are a bit stringent with the terminus... we specifically ask for e.g. Nterm if
				// we happen to be at the beginning, knowing that mods with terminus set to "anywhere" will match as well.
				// This will however not happen vice-versa, e.g. being at the first position and asking for Anywhere mod
				// would not actually match the n-terminus mod, because that is a more stringent requirement.
				if (position == 0) {
					terminus = Terminus.Nterm;
				} else if (position == sequence.length() - 1) {
					terminus = Terminus.Cterm;
				} else {
					terminus = Terminus.Anywhere;
				}

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
		matchingMod = fixTandemPyro(matchingMod, scaffoldSpecificity);
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
	 * HACK: See {@link #fixTandemPyro}.
	 */
	private ModSpecificity matchScaffoldMod(String position, String title, String deltaString, double delta, char residue, Terminus terminus) {
		final Collection<ModSpecificity> matchingModSpecificities = scaffoldModSet.findMatchingModSpecificities(delta, MOD_DELTA_PRECISION, residue, terminus, null, null);

		String effectiveTitle = fixTandemPyro(title, residue, terminus);

		for (ModSpecificity specificity : matchingModSpecificities) {
			if (effectiveTitle.equalsIgnoreCase(specificity.getModification().getTitle())) {
				return specificity;
			}
		}
		throw new MprcException("Could not find a modification: [" + position + ": " + title + "(" + deltaString + ")]");
	}

	/**
	 * HACK: There is a bug in Scaffold-bundled unimod that lists pyro-cmC to have same delta as pyro-Glu. If we run into pyro-cmC and the
	 * composition is equivalent to the broken one (NH3), we replace the mod with pyro-Glu. This happens for X!Tandem a lot.
	 */
	private String fixTandemPyro(String title, char residue, Terminus terminus) {
		String effectiveTitle = title;
		if (PYRO_CMC.equalsIgnoreCase(title)) {
			if (residue == 'Q') {
				if (checkPyroCmcBroken()) {
					effectiveTitle = "Pyro-glu";
				}
			}
		}
		return effectiveTitle;
	}

	/**
	 * HACK: Anothe part of the workaround. pyro-cmC on carbamidomethylated C as reported by Scaffold
	 * should actually be reported as Ammonia-loss.
	 *
	 * @param matchingMod         What we found in our database.
	 * @param scaffoldSpecificity What Scaffold requested.
	 * @return Cleaned up matching mod in case we ran into the X!Tandem pyro-cmC problem
	 */
	private Mod fixTandemPyro(Mod matchingMod, ModSpecificity scaffoldSpecificity) {
		// It is the broken PYRO_CMC mod
		if (PYRO_CMC.equalsIgnoreCase(scaffoldSpecificity.getModification().getTitle()) && checkPyroCmcBroken()) {
			// Replace the modification with ammonia loss (-NH3) on the top of carbamidomethylation
			return modSet.getByTitle("Ammonia-loss");
		}
		return matchingMod;
	}

	/**
	 * @return True if the Pyro-cmC mod is broken (lists NH3 as its composition).
	 */
	private boolean checkPyroCmcBroken() {
		if (pyroCmcBroken == null) {
			final Mod brokenMod = scaffoldModSet.getByTitle("Pyro-cmC");
			pyroCmcBroken = brokenMod != null && "H(-3) N(-1)".equals(brokenMod.getComposition());
		}
		return pyroCmcBroken;
	}

	/**
	 * Whether pyroCmc is broken or not depends on the set of mods.
	 *
	 * @param scaffoldModSet New mod set to use.
	 */
	private void setScaffoldModSet(IndexedModSet scaffoldModSet) {
		this.scaffoldModSet = scaffoldModSet;
		pyroCmcBroken = null;
	}
}
