package edu.mayo.mprc.myrimatch;

import com.google.common.collect.ImmutableMap;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.swift.params2.Instrument;
import edu.mayo.mprc.swift.params2.MassUnit;
import edu.mayo.mprc.swift.params2.Protease;
import edu.mayo.mprc.swift.params2.Tolerance;
import edu.mayo.mprc.swift.params2.mapping.MappingContext;
import edu.mayo.mprc.swift.params2.mapping.Mappings;
import edu.mayo.mprc.unimod.ModSet;
import edu.mayo.mprc.unimod.ModSpecificity;
import edu.mayo.mprc.unimod.Terminus;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public final class MyrimatchMappings implements Mappings {

	public static final String STATIC_MODS = "StaticMods";
	public static final String DYNAMIC_MODS = "DynamicMods";
	public static final String CLEAVAGE_RULES = "CleavageRules";
	public static final String NUM_MIN_TERMINI_CLEAVAGES = "NumMinTerminiCleavages";
	public static final String NUM_MAX_MISSED_CLEAVAGES = "NumMaxMissedCleavages";
	public static final String USE_AVG_MASS_OF_SEQUENCES = "UseAvgMassOfSequences";
	public static final String PRECURSOR_MZ_TOLERANCE = "PrecursorMzTolerance";
	public static final String PRECURSOR_MZ_TOLERANCE_UNITS = "PrecursorMzToleranceUnits";
	public static final String FRAGMENT_MZ_TOLERANCE = "FragmentMzTolerance";
	public static final String FRAGMENT_MZ_TOLERANCE_UNITS = "FragmentMzToleranceUnits";

	private Map<String, String> nativeParams;
	private static final String VARIABLE_MODS_MARKERS = "*^@%~&?!:;|+-/";

	public MyrimatchMappings() {
		nativeParams = initNativeParams();
	}

	@Override
	public Reader baseSettings() {
		return ResourceUtilities.getReader("classpath:edu/mayo/mprc/myrimatch/base.myrimatch.cfg", this.getClass());
	}

	@Override
	public void read(Reader isr) {
	}

	/**
	 * Access for testing purposes only.
	 *
	 * @return Map of all native parameters.
	 */
	Map<String, String> getNativeParams() {
		return ImmutableMap.copyOf(nativeParams);
	}

	/**
	 * A comment starts with the character #
	 *
	 * @param s String to remove comment from
	 * @return String without the comment part
	 */
	static String stripComment(String s) {
		int pos = s.indexOf('#');
		return pos >= 0 ? s.substring(0, pos) : s;
	}

	private Map<String, String> initNativeParams() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(STATIC_MODS, null);
		map.put(DYNAMIC_MODS, null);
		map.put(CLEAVAGE_RULES, null);
		map.put(NUM_MIN_TERMINI_CLEAVAGES, null);
		map.put(NUM_MAX_MISSED_CLEAVAGES, null);
		map.put(USE_AVG_MASS_OF_SEQUENCES, null);
		map.put(PRECURSOR_MZ_TOLERANCE, null);
		map.put(PRECURSOR_MZ_TOLERANCE_UNITS, null);
		map.put(FRAGMENT_MZ_TOLERANCE, null);
		map.put(FRAGMENT_MZ_TOLERANCE_UNITS, null);
		return map;
	}

	@Override
	public void write(Reader oldParams, Writer out) {
		BufferedReader bufferedReader = new BufferedReader(oldParams);
		StringBuilder result = new StringBuilder(100);
		try {
			while (true) {
				boolean processed = false;

				String line = bufferedReader.readLine();
				if (null == line) {
					break;
				}

				int commentPos = line.indexOf('#');
				int lastPart;
				if (commentPos < 0) {
					lastPart = line.length();
				} else {
					lastPart = commentPos;
				}

				int equalsSign = line.indexOf('=');
				if (equalsSign < lastPart && equalsSign >= 0) {
					String key = line.substring(0, equalsSign).trim();
					String oldValue = line.substring(equalsSign + 1, lastPart).trim();
					final String newValue = nativeParams.get(key);
					if (null != newValue && !oldValue.equals(newValue)) {
						processed = true;
						result.setLength(0);
						result
								.append(key)
								.append(" = ")
								.append(newValue)
								.append(line.substring(lastPart))
								.append('\n');
						out.write(result.toString());
					}
				}

				if (!processed) {
					out.write(line);
					out.write('\n');
				}
			}
		} catch (Exception e) {
			throw new MprcException("Cannot load base Myrimatch configuration", e);
		} finally {
			FileUtilities.closeQuietly(bufferedReader);
		}
	}

	@Override
	public void setPeptideTolerance(MappingContext context, Tolerance peptideTolerance) {
		nativeParams.put(PRECURSOR_MZ_TOLERANCE, String.valueOf(peptideTolerance.getValue()));
		nativeParams.put(PRECURSOR_MZ_TOLERANCE_UNITS, massUnitToMyrimatch(peptideTolerance));
	}

	private String massUnitToMyrimatch(Tolerance peptideTolerance) {
		if (MassUnit.Da == peptideTolerance.getUnit()) {
			return "daltons";
		}
		if (MassUnit.Ppm == peptideTolerance.getUnit()) {
			return "ppm";
		}
		throw new MprcException("MyriMatch does not support mass unit " + peptideTolerance.getUnit().name() + "(" + peptideTolerance.getUnit().getDescription() + ")");
	}

	@Override
	public void setFragmentTolerance(MappingContext context, Tolerance fragmentTolerance) {
		final String tolerance = massUnitToMyrimatch(fragmentTolerance);
		nativeParams.put(FRAGMENT_MZ_TOLERANCE, fragmentTolerance.getValue() + " " + tolerance);
		nativeParams.put(FRAGMENT_MZ_TOLERANCE_UNITS, tolerance);
	}

	@Override
	public void setVariableMods(MappingContext context, ModSet variableMods) {
		nativeParams.put(DYNAMIC_MODS, variableModsToString(context, variableMods));
	}

	private String variableModsToString(MappingContext context, ModSet variableMods) {
		if (variableMods.getModifications().size() == 0) {
			return "";
		}

		StringBuilder result = new StringBuilder(100);
		int index = 0;
		boolean warnedUnsupportedProteinOnly = false;

		for (ModSpecificity specificity : variableMods.getModifications()) {
			if (!warnedUnsupportedProteinOnly && specificity.isPositionProteinSpecific()) {
				context.reportWarning("Protein terminus-only mods are not supported. Will allow modification at peptide terminus.");
				warnedUnsupportedProteinOnly = true;
			}

			if (specificity.getTerm() == Terminus.Nterm) {
				result.append("(");
			}

			result.append(specificity.getSite());

			if (specificity.getTerm() == Terminus.Cterm) {
				result.append(")");
			}

			result.append(' ')
					.append(VARIABLE_MODS_MARKERS.charAt(index))
					.append(' ')
					.append(specificity.getModification().getMassMono())
					.append(' ');

			index++;
			if (variableMods.size() >= VARIABLE_MODS_MARKERS.length()) {
				context.reportWarning("Myrimatch supports only " + VARIABLE_MODS_MARKERS.length() + " modifications.");
				break;
			}
		}
		return result.substring(0, result.length() - 1);
	}

	@Override
	public void setFixedMods(MappingContext context, ModSet fixedMods) {
		nativeParams.put(STATIC_MODS, fixedModsToString(fixedMods));
	}

	private String fixedModsToString(ModSet fixedMods) {
		if (fixedMods.getModifications().size() == 0) {
			return "";
		}
		StringBuilder result = new StringBuilder(50);
		for (ModSpecificity specificity : fixedMods.getModifications()) {
			result
					.append(specificity.getSite())
					.append(' ')
					.append(specificity.getModification().getMassMono())
					.append(' ');
		}
		return result.substring(0, result.length() - 1);
	}

	@Override
	public void setSequenceDatabase(MappingContext context, String shortDatabaseName) {
		// The database is not set in the config
	}

	@Override
	/**
	 * Map enzyme to PCRE regular expression with zero-width lookbehind, lookahead assertions.
	 *
	 * <table>
	 *     <tr><th></th><th>Behind</th><th>Ahead</th></tr>
	 *     <tr><td>Positive</td><td>(&lt;<= )</td><td>(?= )</td></tr>
	 *     <tr><td>Negative</td><td>(&lt;<! )</td><td>(?! )</td></tr>
	 * </table>
	 *
	 * Example: Trypsin <pre>(?<=[KR])(?!P)</pre>
	 */
	public void setProtease(MappingContext context, Protease protease) {
		nativeParams.put(CLEAVAGE_RULES, enzymeToString(protease));
	}

	static String enzymeToString(Protease enzyme) {
		if ("".equals(enzyme.getRnminus1()) && "".equals(enzyme.getRn())) {
			return "NoEnzyme";
		}

		StringBuilder result = new StringBuilder(20);

		if (!"".equals(enzyme.getRnminus1())) {
			if (enzyme.getRnminus1().startsWith("!")) {
				result
						.append("(?<!")
						.append(wrapAminoAcidGroup(enzyme.getRnminus1().substring(1)))
						.append(")");
			} else {
				result
						.append("(?<=")
						.append(wrapAminoAcidGroup(enzyme.getRnminus1()))
						.append(")");
			}
		}
		if (!"".equals(enzyme.getRn())) {
			if (enzyme.getRn().startsWith("!")) {
				result
						.append("(?!")
						.append(wrapAminoAcidGroup(enzyme.getRn().substring(1)))
						.append(")");
			} else {
				result
						.append("(?=")
						.append(wrapAminoAcidGroup(enzyme.getRn()))
						.append(")");
			}
		}
		return result.toString();
	}

	private static String wrapAminoAcidGroup(String group) {
		if (group.length() <= 1) {
			return group;
		}
		return "[" + group + "]";
	}

	@Override
	public void setMissedCleavages(MappingContext context, Integer missedCleavages) {
		nativeParams.put(NUM_MAX_MISSED_CLEAVAGES, String.valueOf(missedCleavages));
	}

	@Override
	public void setInstrument(MappingContext context, Instrument instrument) {
		// Only Orbitrap is precise enough to use the monoisotopic mass
		// Is that true?
		nativeParams.put(USE_AVG_MASS_OF_SEQUENCES, Instrument.ORBITRAP.equals(instrument) ? "false" : "true");
	}

	@Override
	public String getNativeParam(String name) {
		return nativeParams.get(name);
	}

	@Override
	public void setNativeParam(String name, String value) {
		nativeParams.put(name, value);
	}
}
