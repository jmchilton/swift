package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.searchdb.dao.LocalizedModification;
import edu.mayo.mprc.unimod.IndexedModSet;
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
	/**
	 * Example format:
	 * <p/>
	 * {@code n-term: Pyro-cmC (-17.03), m17: Oxidation (+15.99)}
	 */
	private static final Pattern MOD_PATTERN = Pattern.compile("([nc]-term|[a-zA-Z][1-9][0-9]*)\\s*:\\s*(.*?)\\s*\\(([+-][0-9.]+)\\)(,\\s*|\\s*$)");

	/**
	 * @param modSet A set of modifications that are recognized. Usually loaded from unimod.
	 */
	public ScaffoldModificationFormat(IndexedModSet modSet) {
		this.modSet = modSet;
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
				residue = '-';
				terminus = Terminus.Nterm;
				position = 0;
			} else if (positionGroup.equalsIgnoreCase("c-term")) {
				residue = '-';
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

			ModSpecificity modSpecificity = null;
			final Set<ModSpecificity> matchingModSpecificities = modSet.findMatchingModSpecificities(delta - MOD_DELTA_PRECISION, delta + MOD_DELTA_PRECISION, residue, terminus, null, null);
			for (ModSpecificity specificity : matchingModSpecificities) {
				if (titleGroup.equalsIgnoreCase(specificity.getModification().getTitle())) {
					modSpecificity = specificity;
					break;
				}
			}
			if (modSpecificity == null) {
				throw new MprcException("Could not find a modification: [" + positionGroup + ": " + titleGroup + "(" + deltaGroup + ")]");
			}

			LocalizedModification localizedModification = new LocalizedModification(modSpecificity, position, residue);
			mods.add(localizedModification);
		}
		if (lastEnd != modifications.length()) {
			throw new MprcException("Could not parse modification information [" + modifications.substring(lastEnd, modifications.length()) + "]");
		}
	}
}
