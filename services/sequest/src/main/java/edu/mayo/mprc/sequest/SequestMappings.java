package edu.mayo.mprc.sequest;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.swift.params2.*;
import edu.mayo.mprc.swift.params2.mapping.MappingContext;
import edu.mayo.mprc.swift.params2.mapping.Mappings;
import edu.mayo.mprc.unimod.ModSet;
import edu.mayo.mprc.unimod.ModSpecificity;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SequestMappings implements Mappings, Cloneable {
	private static final String PEP_TOL_UNIT = "peptide_mass_units";
	private static final String PEP_TOL_VALUE = "peptide_mass_tolerance";
	private static final String FRAG_TOL_VALUE = "fragment_ion_tolerance";
	/**
	 * Variable modifications parameter.
	 */
	static final String VAR_MODS = "diff_search_options";

	/**
	 * C and N-terminus modifications parameter.
	 */
	static final String VAR_MODS_TERMINUS = "term_diff_search_options";
	private static final String DATABASE = "first_database_name";
	private static final String ENZYME = "enzyme_info";
	private static final String MISSED_CLEAVAGES = "max_num_internal_cleavage_sites";
	private static final String ION_SERIES = "ion_series";

	private static final Pattern FIXED = Pattern.compile("^add_([A-Z]|Nterm|Cterm)_(.*)");

	private static final Pattern SEQUEST_HEADER = Pattern.compile("\\[((?:SEQUEST)|(?:MAKEDB))\\]");

	private static final Pattern WHITESPACE = Pattern.compile("^\\s*$");
	private static final Pattern COMMENT = Pattern.compile("^\\s*(;.*)$");
	private static final Pattern EQUALS = Pattern.compile("^.*\\=.*$");
	private static final Pattern PARSE_LINE = Pattern.compile("^\\s*([^\\s=]+)\\s*=\\s*([^;]*)(\\s*;.*)?$");

	/**
	 * It is important to use linked hash map because the {@link SequestToMakeDBConverter} depends
	 * on ordering of the native params.
	 * <p/>
	 * Also, just because of the converter, we store all native parameters, not just those that map to abstract params.
	 */
	private LinkedHashMap<String, String> nativeParams = new LinkedHashMap<String, String>();

	public SequestMappings() {
	}

	@Override
	public Reader baseSettings() {
		return ResourceUtilities.getReader("classpath:edu/mayo/mprc/swift/params/base.sequest.params", this.getClass());
	}

	public void read(Reader isr) {
		BufferedReader reader = new BufferedReader(isr);
		try {
			String header = reader.readLine();
			Matcher m = SEQUEST_HEADER.matcher(header);
			if ((header == null) || (header.length() == 0) || (!m.lookingAt())) {
				throw new MprcException("Not a sequest params file");
			}
			while (true) {
				String it = reader.readLine();
				if (it == null) {
					break;
				}

				if (WHITESPACE.matcher(it).matches() || COMMENT.matcher(it).matches()) {
					// Comment, ignore
					continue;
				}

				if (EQUALS.matcher(it).matches()) {
					// basically, we want to match: a keyword followed by equals followed by an optional value
					// followed by optional whitespace and comment.
					Matcher matcher = PARSE_LINE.matcher(it);
					if (!matcher.matches()) {
						throw new MprcException("Can't understand '" + it + "'");
					}
					String id = matcher.group(1);
					String value = matcher.group(2);
					if (value == null) {
						value = "";
					}

					// We store absolutely all parameters, because makedb depends on it
					nativeParams.put(id, value);
				} else {
					throw new MprcException("Can't understand '" + it + "'");
				}
			}
		} catch (Exception t) {
			throw new MprcException("Failure reading sequest parameter collection", t);
		} finally {
			FileUtilities.closeQuietly(reader);
		}
	}

	public void write(Reader oldParams, Writer out) {
		BufferedReader reader = null;
		PrintWriter pw = null;
		try {
			reader = new BufferedReader(oldParams);
			pw = new PrintWriter(out);
			while (true) {
				String it = reader.readLine();
				if (it == null) {
					break;
				}

				if (WHITESPACE.matcher(it).matches()) {
					pw.println(it);
				} else if (COMMENT.matcher(it).matches()) {
					pw.println(it);
				} else if (EQUALS.matcher(it).matches()) {
					// basically, we want to match: a keyword followed by equals followed by an optional value
					// followed by optional whitespace and comment.
					Matcher matcher = PARSE_LINE.matcher(it);
					if (!matcher.matches()) {
						throw new MprcException("Can't understand '" + it + "'");
					}
					String id = matcher.group(1);
					if (nativeParams.keySet().contains(id)) {
						// Replace the value
						if (matcher.group(2) != null) {
							// We have the value defined
							pw.print(it.substring(0, matcher.start(2)));
							pw.print(nativeParams.get(id));
							pw.println(it.substring(matcher.end(2)));
						} else {
							// The value is missing
							pw.print(it.trim());
							pw.println(nativeParams.get(id));
						}
					} else {
						pw.println(it);
					}
				} else {
					pw.println(it);
				}
			}
		} catch (Exception t) {
			throw new MprcException("Failure reading sequest parameter collection", t);
		} finally {
			FileUtilities.closeQuietly(reader);
			FileUtilities.closeQuietly(pw);
		}
	}

	/**
	 * @return The complete map of native parameters.
	 */
	public Map<String, String> getNativeParams() {
		return Collections.unmodifiableMap(nativeParams);
	}

	/**
	 * Returns value of a given native parameter.
	 *
	 * @param name Name of the parameter.
	 * @return Value of the native parameter.
	 */
	public String getNativeParam(String name) {
		return nativeParams.get(name).trim();
	}

	/**
	 * Allows the sequest to makedb converter to overwrite the native parameter values directly.
	 *
	 * @param name  Name of the native parameter.
	 * @param value Value of the native parameter.
	 */
	public void setNativeParam(String name, String value) {
		nativeParams.put(name, value);
	}

	public void setPeptideTolerance(MappingContext context, Tolerance peptideTolerance) {
		setNativeParam(PEP_TOL_VALUE, String.valueOf(peptideTolerance.getValue()));
		if (MassUnit.Da.equals(peptideTolerance.getUnit())) {
			setNativeParam(PEP_TOL_UNIT, "0");
			// MMU = 1 but we never use MMU
		} else if (MassUnit.Ppm.equals(peptideTolerance.getUnit())) {
			setNativeParam(PEP_TOL_UNIT, "2");
		}
	}

	public void setFragmentTolerance(MappingContext context, Tolerance fragmentTolerance) {
		if (!MassUnit.Da.equals(fragmentTolerance.getUnit())) {
			setNativeParam(FRAG_TOL_VALUE, "1");
			context.reportWarning("Sequest does not support '" + fragmentTolerance.getUnit() + "' fragment tolerances; using 1 Da instead.");
		}
		setNativeParam(FRAG_TOL_VALUE, String.valueOf(fragmentTolerance.getValue()));
	}

	public void setVariableMods(MappingContext context, ModSet variableMods) {
		StringBuilder sb = new StringBuilder();

		List<ModSpecificity> set = new ArrayList<ModSpecificity>(variableMods.getModifications());
		Collections.sort(set);

		int i = 0;

		StringBuilder skippedMods = new StringBuilder(20);
		StringBuilder skippedNterm = new StringBuilder(20);
		StringBuilder skippedCterm = new StringBuilder(20);
		boolean proteinSpecificMod = false;
		Double cterm = null;
		Double nterm = null;

		for (ModSpecificity ms : set) {
			String title = ms.toString();
			double mass = ms.getModification().getMassMono();

			if (notSupportedMod(ms)) {
				context.reportWarning("Sequest does not support variable modification with specific site '" +
						ms.getSite() + "' limited to " + ms.getTerm() + ", skipping " + title);

			} else if (ms.isPositionNTerminus()) {
				if (nterm != null) {
					appendCommaSeparated(skippedNterm, title);
				} else {
					nterm = mass;
					if (ms.isPositionProteinSpecific()) {
						proteinSpecificMod = true;
					}
				}
			} else if (ms.isPositionCTerminus()) {
				if (cterm != null) {
					appendCommaSeparated(skippedCterm, title);
				} else {
					cterm = mass;
					if (ms.isPositionProteinSpecific()) {
						proteinSpecificMod = true;
					}
				}
			} else {
				i++;
				if (i > 6) {
					appendCommaSeparated(skippedMods, title);
				} else {
					if (sb.length() != 0) {
						sb.append(" ");
					}
					sb.append(mass).append(' ').append(ms.getSite());
				}
			}
		}
		while (i < 6) {
			sb.append(sb.length() == 0 ? "" : " ").append("0.0000 X");
			i++;
		}

		setNativeParam(VAR_MODS, sb.toString());
		setNativeParam(VAR_MODS_TERMINUS, (cterm == null ? "0.0" : cterm) + " " + (nterm == null ? "0.0" : nterm));
		if (skippedMods.length() != 0) {
			context.reportWarning("Sequest supports up to 6 variable modifications, skipping " + skippedMods);
		}

		if (skippedNterm.length() != 0) {
			context.reportWarning("Sequest does not support multiple variable modifications at N-terminal, skipping " + skippedNterm);
		}

		if (skippedCterm.length() != 0) {
			context.reportWarning("Sequest does not support multiple variable modifications at C-terminal, skipping " + skippedCterm);
		}

		if (proteinSpecificMod) {
			context.reportWarning("Sequest does not support variable modifications specific only to protein terminus. These mods will be used for peptide terminii as well.");
		}

	}

	/**
	 * @param builder Builder to append next string to.
	 * @param text    String to append. Strings are separated by {@code ", "}
	 */
	private void appendCommaSeparated(StringBuilder builder, String text) {
		if (builder.length() > 0) {
			builder.append(", ");
		}
		builder.append(text);
	}

	/**
	 * Sequest does not support modification with specific amino acid at the protein terminus (we can do only
	 * amino acid anywhere, or anything at the terminus).
	 *
	 * @param ms Mod specificity to check
	 * @return true if Sequest supports given mod.
	 */
	private boolean notSupportedMod(ModSpecificity ms) {
		// we can't support specific amino acids at N or C terminus of peptide or protein.
		return ms.isSiteSpecificAminoAcid() && (!ms.isPositionAnywhere() || ms.isProteinOnly());
	}

	public void setFixedMods(MappingContext context, ModSet fixedMods) {
		// The key is in form [AA|Cterm|Nterm]_[protein|peptide]
		// We sum all the fixed mod contributions
		Map<String, Double> masses = new HashMap<String, Double>();
		for (ModSpecificity ms : fixedMods.getModifications()) {
			String title = ms.toString();
			String key;
			if (ms.isPositionAnywhere() && ms.isSiteSpecificAminoAcid()) {
				key = String.valueOf(ms.getSite()); // The key is single letter corresponding to the amino acid
			} else if (ms.isPositionCTerminus()) {
				key = "Cterm_" + (ms.isProteinOnly() ? "protein" : "peptide");
			} else if (ms.isPositionNTerminus()) {
				key = "Nterm_" + (ms.isProteinOnly() ? "protein" : "peptide");
			} else {
				context.reportWarning("Sequest does not support modification with position '" +
						ms.getTerm() + "' and site '" + ms.getSite() + "', dropping " + title);
				return;
			}

			double mass = ms.getModification().getMassMono();
			Double d = 0.0;
			if (masses.containsKey(key)) {
				d = masses.get(key);
			}
			masses.put(key, d + mass);
		}

		for (String param : nativeParams.keySet()) {
			Matcher m = FIXED.matcher(param);
			if (m.matches()) {
				String ssite = m.group(1);
				String pos = m.group(2);
				String site;
				if (pos.startsWith("protein") || pos.startsWith("peptide")) {
					site = ssite + "_" + pos;
				} else {
					site = ssite;
				}
				if (masses.containsKey(site)) {
					setNativeParam(m.group(), String.valueOf(masses.get(site)));
				} else {
					setNativeParam(m.group(), "0.0");
				}
			}
		}
	}

	public void setSequenceDatabase(MappingContext context, String shortDatabaseName) {
		setNativeParam(DATABASE, "${DB:" + shortDatabaseName + "}");
	}

	public void setProtease(MappingContext context, Protease protease) {

		String name = protease.getName().replaceAll("\\s", "_");
		String firstDigit = "1"; // 0 - non-specific, 1-specific (?)
		String secondDigit = "1"; // Forward (cut C-terminus from the residue) or reverse (cut N-terminus)
		String rn = protease.getRn();
		String rnminus1 = protease.getRnminus1();

		// how do we recognize sense == 0 (ie inverted match)?
		if ("Non-Specific".equals(protease.getName())) {
			firstDigit = "0";
			secondDigit = "0";
		} else if (rnminus1.length() == 0) {
			// if there's no rnminus1, then we assume this is an inverted match
			secondDigit = "0";
			String swap = rn;
			rn = rnminus1;
			rnminus1 = swap;
		} else if (rn.startsWith("!")) {
			rn = rn.substring(1);
		} else if (rn.length() != 0) { // RN.length = 0 is fine, allowed
			// can't deal with this enzyme
			throw new MprcException("Enzyme " + protease.getName() + " cannot be used by Sequest");
		}

		setNativeParam(ENZYME,
				name + " " + firstDigit + " " + secondDigit + " " +
						(rnminus1.equals("") ? "-" : rnminus1) + " " +
						(rn.equals("") ? "-" : rn));
	}

	public void setMissedCleavages(MappingContext context, Integer missedCleavages) {
		String value = String.valueOf(missedCleavages);
		if (missedCleavages > 12) {
			value = "12";
			context.reportWarning("Sequest doesn't support > 12 missed cleavages");
		}
		setNativeParam(MISSED_CLEAVAGES, value);
	}

	// The series matches the pattern.
	private static final String[] INSTRUMENT_SERIES = "a      b      y      a           b           c           d           v           w           x           y           z".split("\\s+");
	private static final Pattern R = Pattern.compile("(\\d+) (\\d+) (\\d+) (\\d+.\\d+) (\\d+.\\d+) (\\d+.\\d+) (\\d+.\\d+) (\\d+.\\d+) (\\d+.\\d+) (\\d+.\\d+) (\\d+.\\d+) (\\d+.\\d+)");

	public void setInstrument(MappingContext context, Instrument it) {
		Map<String, IonSeries> hasseries = new HashMap<String, IonSeries>();
		StringBuilder sb = new StringBuilder();

		HashSet<String> seriesset = new HashSet<String>();
		seriesset.addAll(Arrays.asList(INSTRUMENT_SERIES));
		for (IonSeries is : it.getSeries()) {
			if (seriesset.contains(is.getName())) {
				hasseries.put(is.getName(), is);
			} else {
				context.reportWarning("Sequest doesn't support ion series " + is.getName());
			}
		}

		HashSet<String> first = new HashSet<String>();
		for (String ss : INSTRUMENT_SERIES) {
			if (sb.length() != 0) {
				sb.append(" ");
			}
			if (hasseries.containsKey(ss)) {
				if (("a".equals(ss) || "b".equals(ss) || "y".equals(ss)) && !first.contains(ss)) {
					sb.append("1");
					first.add(ss);
				} else {
					sb.append("1.0");
				}
			} else {
				if (("a".equals(ss) || "b".equals(ss) || "y".equals(ss)) && !first.contains(ss)) {
					sb.append("0");
					first.add(ss);
				} else {
					sb.append("0.0");
				}
			}
		}

		setNativeParam(ION_SERIES, sb.toString());
	}


	@Override
	public Object clone() throws CloneNotSupportedException {
		SequestMappings mappings = (SequestMappings) super.clone();
		mappings.nativeParams = new LinkedHashMap<String, String>(this.nativeParams.size());
		mappings.nativeParams.putAll(this.nativeParams);
		return mappings;
	}
}
