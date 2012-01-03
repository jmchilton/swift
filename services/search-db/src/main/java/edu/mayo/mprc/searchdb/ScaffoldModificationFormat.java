package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.searchdb.dao.LocalizedModification;
import edu.mayo.mprc.unimod.IndexedModSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for Scaffold modification format as seen in Spectrum report.
 *
 * @author Roman Zenka
 */
public final class ScaffoldModificationFormat {
	private IndexedModSet modSet;

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
	 * @param sequence              The sequence being modified. This is not being stored, it is used to check that the parse worked correctly.
	 * @param fixedModifications    Scaffold-like list of fixed modifications.
	 * @param variableModifications Scaffold-like list of variable modifications.
	 * @return Parsed list of localized modifications.
	 */
	public List<LocalizedModification> parseModifications(String sequence, String fixedModifications, String variableModifications) {
		final ArrayList<LocalizedModification> localizedModifications = new ArrayList<LocalizedModification>(1);
		return localizedModifications;
	}
}
