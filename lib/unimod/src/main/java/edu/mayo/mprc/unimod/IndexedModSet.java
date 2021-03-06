package edu.mayo.mprc.unimod;

import com.google.common.base.Objects;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.ComparisonChain;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A set of Modifications.
 */
public class IndexedModSet implements Set<Mod> {
	private String name;
	/**
	 * all modification in this set
	 */
	protected Set<Mod> modifications = new TreeSet<Mod>();
	/**
	 * an index to allow fast look between a name (title or alternative names) and the associated Modification object.
	 */
	private Map<String, Mod> titleIndex = new HashMap<String, Mod>();
	/**
	 * A sorted map of the Delta's sorted by Monoisotopic mass.  This will allow a fast searching of mods in a given mass range.
	 */
	private TreeMap<Double, List<Mod>> deltaMonoMassIndex = new TreeMap<Double, List<Mod>>();

	public IndexedModSet() {

	}

	/**
	 * takes a modification and adds it to the indexes
	 *
	 * @param m the Modification you want to have added to indices.
	 */
	private void index(Mod m) {
		titleIndex.put(m.getTitle(), m);
		if (m.getAltNames() != null) {
			for (String altName : m.getAltNames()) {
				titleIndex.put(altName, m);
			}
		}

		List<Mod> massList = this.deltaMonoMassIndex.get(m.getMassMono());
		if (massList == null) {
			massList = new LinkedList<Mod>();
		}
		massList.add(m);
		deltaMonoMassIndex.put(m.getMassMono(), massList);
	}

	/**
	 * Return mod for given title.
	 *
	 * @param title the title that you want ta Modification of
	 * @return the Modification that matches the given title
	 */
	public Mod getByTitle(String title) {
		return titleIndex.get(title);
	}

	public static final class MascotNameParts {
		private String title;
		private String protein;
		private String term;
		private String acids;
		/**
		 * Parses Mascot specificity, that is in form [1=Title] ([2=Protein]? [3=N/C-term]? [4=list of amino acids]?)
		 */
		public static final Pattern MASCOT_SPECIFICITY = Pattern.compile("\\s*(.+?)\\s*\\(\\s*((?:Protein)?)\\s*((?:[NC]-term)?)\\s*([^)]*)?\\)");

		public MascotNameParts(String title, String protein, String term, String acids) {
			this.title = title;
			this.protein = protein;
			this.term = term;
			this.acids = acids;
		}

		public String getTitle() {
			return title;
		}

		public String getProtein() {
			return protein;
		}

		public String getTerm() {
			return term;
		}

		public String getAcids() {
			return acids;
		}

		public static MascotNameParts parseMascotName(String mascotName) {
			final Matcher matcher = MASCOT_SPECIFICITY.matcher(mascotName);
			if (matcher.matches()) {
				return new MascotNameParts(
						matcher.group(1),
						matcher.group(2),
						matcher.group(3),
						matcher.group(4));
			}
			return null;
		}
	}

	/**
	 * Get a modification specificities.
	 * The input is mascot name for a modification, examples:
	 * <ul>
	 * <li>Title (ST)</li>
	 * <li>Title (Protein N-term A)</li>
	 * <li>Title (Protein C-term)</li>
	 * <li>Title (C-term T)</li>
	 * <li>Title (C-term)</li>
	 * </ul>
	 */
	public List<ModSpecificity> getSpecificitiesByMascotName(String mascotName) {
		final MascotNameParts nameParts = MascotNameParts.parseMascotName(mascotName);
		if (nameParts != null) {
			final Mod modification = getByTitle(nameParts.getTitle());
			if (modification == null) {
				throw new MprcException("Cannot find modification named " + nameParts.getTitle());
			}
			boolean proteinEnd = "Protein".equalsIgnoreCase(nameParts.getProtein());
			Terminus terminus;
			if ("N-term".equalsIgnoreCase(nameParts.getTerm())) {
				terminus = Terminus.Nterm;
			} else if ("C-term".equalsIgnoreCase(nameParts.getTerm())) {
				terminus = Terminus.Cterm;
			} else {
				terminus = Terminus.Anywhere;
			}

			List<ModSpecificity> modSpecificities = new ArrayList<ModSpecificity>();
			for (ModSpecificity modSpecificity : modification.getModSpecificities()) {
				if (modSpecificity.matches(nameParts.getAcids(), terminus, proteinEnd)) {
					modSpecificities.add(modSpecificity);
				}
			}
			return modSpecificities;
		} else {
			throw new MprcException("Cannot understand modification: " + mascotName);
		}
	}

	/**
	 * Takes a name of a Modification or a modification's alias and finds the other aliases.
	 *
	 * @param title the name you want to find aliases for
	 * @return all of the aliases for the given name or null if name is not found, if there is not alternative names then empty list is returned.
	 */
	public Set<String> getAlternativeNames(String title) {
		Set<String> retSet = new HashSet<String>();
		Mod mod = getByTitle(title);
		if (mod == null) {
			return null;
		}
		retSet.add(mod.getTitle());
		retSet.addAll(mod.getAltNames());
		retSet.remove(title);
		return retSet;
	}

	/**
	 * Finds the ModificationSpecifities that meet the requirements specified. Requirement set to null is not considered.
	 * For a set of all modifications, set all parameters to null.
	 *
	 * @param minMass     the minimum mass
	 * @param maxMass     the maximum mass
	 * @param site        the specificity site
	 * @param terminus    the specificity terminus
	 * @param proteinOnly if terminus is set, specify if it has to be protein terminus
	 * @param hidden      whether the modification is hidden or not
	 * @return a set of modifications that match the given parameters
	 */
	public Set<ModSpecificity> findMatchingModSpecificities(Double minMass, Double maxMass, Character site, Terminus terminus, Boolean proteinOnly, Boolean hidden) {
		Collection<List<Mod>> inRange = null;
		if (minMass != null && maxMass != null) {
			inRange = deltaMonoMassIndex.subMap(minMass, maxMass).values();
		} else {
			inRange = deltaMonoMassIndex.values();
		}

		Set<ModSpecificity> modsInMassRange = collectModSpecificities(inRange);

		Set<ModSpecificity> matchingModSpecificities = new HashSet<ModSpecificity>();
		for (ModSpecificity sp : modsInMassRange) {
			if (site != null && !sp.getSite().equals(site)) {
				continue;
			}
			if (terminus != null && !sp.getTerm().equals(terminus)) {
				continue;
			}
			if (proteinOnly != null && !proteinOnly.equals(sp.isProteinOnly())) {
				continue;
			}
			if (hidden != null && !hidden.equals(sp.getHidden())) {
				continue;
			}
			matchingModSpecificities.add(sp);
		}
		return matchingModSpecificities;
	}

	private static Set<ModSpecificity> collectModSpecificities(Collection<List<Mod>> inRange) {
		Set<ModSpecificity> modsInMassRange = new HashSet<ModSpecificity>();
		for (List<Mod> l : inRange) {
			for (Mod m : l) {
				for (ModSpecificity ms : m.getModSpecificities()) {
					modsInMassRange.add(ms);
				}
			}
		}
		return modsInMassRange;
	}

	/**
	 * This method works just as {@link #findMatchingModSpecificities} except if multiple results are found
	 * then a little more intelligence is used to give just one.  Only use this if it really doesn't matter as long as the given parameteres match.
	 * <p/>
	 * Currently this only takes into account the 'hidden' property so that ones that are not hidden are chosen over those that are
	 */
	public ModSpecificity findSingleMatchingModificationSet(Double minMass, Double maxMass, Character site, Terminus terminus, Boolean proteinOnly, Boolean hidden) {
		List<ModSpecificity> allMatches = new ArrayList<ModSpecificity>(findMatchingModSpecificities(minMass, maxMass, site, terminus, proteinOnly, hidden));

		if (allMatches.isEmpty()) {
			return null;
		} else if (allMatches.size() == 1) {
			return allMatches.get(0);
		} else {
			Collections.sort(allMatches, new Comparator<ModSpecificity>() {
				//try to determine which specificity is more favorable

				//favor non-hidden over hidden specificities.

				public int compare(ModSpecificity o1, ModSpecificity o2) {
					//want hidden ones to be higher in the list
					return Boolean.valueOf(o1.getHidden()).compareTo(o2.getHidden());
				}
			});

			return allMatches.get(0);
		}
	}

	/**
	 * gets all of the official titles of all of modifications
	 *
	 * @return a list of all titles
	 */
	public Set<String> getAllTitles() {
		Set<String> titleSet = new HashSet<String>();
		for (Mod m : modifications) {
			titleSet.add(m.getTitle());
		}
		return titleSet;
	}

	public Set<Mod> asSet() {
		return Collections.unmodifiableSet(modifications);
	}

	public Set<ModSpecificity> getAllSpecificities(boolean includeHidden) {
		Set<ModSpecificity> modspecset = new TreeSet<ModSpecificity>();
		for (Mod modification : modifications) {
			for (ModSpecificity modspec : modification.getModSpecificities()) {
				if (includeHidden || !modspec.getHidden()) {
					modspecset.add(modspec);
				}
			}
		}
		return modspecset;
	}

	/**
	 * @return A map from full_name to the monoisotopic mass.
	 */
	public Map<String, Double> getFullNameToMonoisotopicMassMap() {
		Map<String, Double> map = new HashMap<String, Double>(modifications.size());
		for (Mod mod : modifications) {
			map.put(mod.getFullName(), mod.getMassMono());
			map.put(mod.getTitle(), mod.getMassMono());
		}
		return map;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int size() {
		return modifications.size();
	}

	@Override
	public boolean isEmpty() {
		return modifications.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return modifications.contains((Mod) o);
	}

	@Override
	public Iterator<Mod> iterator() {
		return modifications.iterator();
	}

	@Override
	public Object[] toArray() {
		return modifications.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return modifications.toArray(a);
	}

	@Override
	public boolean add(Mod toAdd) {
		final boolean added = this.modifications.add(toAdd);
		if (added) {
			this.index(toAdd);
		}
		return added;
	}

	@Override
	public boolean remove(Object o) {
		throw new MprcException("A modification set does not support removing its elements");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return modifications.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Mod> c) {
		final boolean added = modifications.addAll(c);
		if (added) {
			for (Mod mod : c) {
				this.index(mod);
			}
		}
		return added;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new MprcException("A modification set does not support retainAll");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new MprcException("A modification set does not support removeAll");
	}

	@Override
	public void clear() {
		throw new MprcException("A modification set does not support clearing");
	}

	public int compareTo(Object t) {
		if (!(t instanceof IndexedModSet)) {
			return 1;
		}
		IndexedModSet tt = (IndexedModSet) t;
		ComparisonChain chain = ComparisonChain.start().nullsFirst();
		chain = chain
				.compare(this.name, tt.name)
				.compare(this.modifications.size(), tt.modifications.size());
		if (chain.result() == 0) {
			for (Iterator<Mod> i = this.modifications.iterator(), j = tt.modifications.iterator(); i.hasNext();) {
				final Mod left = i.next();
				final Mod right = j.next();
				chain = chain.compare(left, right);
				if (chain.result() != 0) {
					break;
				}
			}
		}
		return chain.result();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof IndexedModSet)) {
			return false;
		}
		IndexedModSet tt = (IndexedModSet) obj;
		return Objects.equal(this.name, tt.name) &&
				Objects.equal(this.modifications, tt.modifications);
	}

	public int hashCode() {
		return (name != null ? name.hashCode() : 0) + modifications.hashCode();
	}

}
