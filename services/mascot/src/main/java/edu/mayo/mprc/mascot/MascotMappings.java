package edu.mayo.mprc.mascot;

import com.google.common.collect.ImmutableMap;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.swift.params2.Instrument;
import edu.mayo.mprc.swift.params2.Protease;
import edu.mayo.mprc.swift.params2.Tolerance;
import edu.mayo.mprc.swift.params2.mapping.MappingContext;
import edu.mayo.mprc.swift.params2.mapping.Mappings;
import edu.mayo.mprc.swift.params2.mapping.ParamsInfo;
import edu.mayo.mprc.unimod.ModSet;
import edu.mayo.mprc.unimod.ModSpecificity;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MascotMappings implements Mappings {
	private Map<Protease, String> mascotNamesByEnzyme;
	private static final String PEP_TOL_VALUE = "TOL";
	private static final String PEP_TOL_UNIT = "TOLU";
	private static final String FRAG_TOL_VALUE = "ITOL";
	private static final String FRAG_TOL_UNIT = "ITOLU";
	private static final String DATABASE = "DB";
	private static final String VAR_MODS = "IT_MODS";
	private static final String FIXED_MODS = "MODS";
	private static final String ENZYME = "CLE";
	private static final String MISSED_CLEAVAGES = "PFA";
	private static final String INSTRUMENT = "INSTRUMENT";

	/**
	 * Mascot supports only limited amount of variable modifications.
	 */
	public static final int MAX_VARIABLE_MODS = 9;

	private Map<String, String> nativeParams = new HashMap<String, String>();
	private static final Pattern COMMA_SPLIT = Pattern.compile(",");

	public MascotMappings(ParamsInfo info) {
		// params name : mascot name
		Map<String, String> enzymeNames = new ImmutableMap.Builder<String, String>()
				.put("Trypsin (restrict P)", "Trypsin")
				.put("Arg-C", "Arg-C")
				.put("Asp-N", "Asp-N")
				.put("Asp-N_ambic", "Asp-N_ambic")
				.put("Chymotrypsin", "Chymotrypsin")
				.put("CNBr", "CNBr")
				.put("Formic_acid", "Formic_acid")
				.put("Lys-C (restrict P)", "Lys-C")
				.put("Lys-C (allow P)", "Lys-C/P")
				.put("PepsinA", "PepsinA")
				.put("Tryp-CNBr", "Tryp-CNBr")
				.put("TrypChymo", "TrypChymo")
				.put("Trypsin (allow P)", "Trypsin/P")
				.put("V8-DE", "V8-DE")
				.put("V8-E", "V8-E")
				.put("ChymoAndGluC", "ChymoAndGluC")
				.put("Non-Specific", "None")
				.build();

		Map<String, Protease> allowedH = new HashMap<String, Protease>();

		for (Protease protease : info.getEnzymeAllowedValues()) {
			allowedH.put(protease.getName(), protease);
		}
		mascotNamesByEnzyme = getNamesByEnzyme(allowedH, enzymeNames);
	}

	private static final Pattern PARAM = Pattern.compile("^[^#]+=.*");
	private static final Pattern COMMENT = Pattern.compile("^\\s*#.*");
	private static final Pattern KEY_VALUE_COMMENT = Pattern.compile("\\s*([^\\s=]+)=([^#]*)(\\s#.*)?");

	@Override
	public Reader baseSettings() {
		return ResourceUtilities.getReader("classpath:edu/mayo/mprc/swift/params/base.mascot.params", getClass());
	}

	public void read(Reader isr) {
	}

	public void write(Reader oldParams, Writer out) {
		Writer writer = null;
		try {
			writer = out;
			LineNumberReader br = new LineNumberReader(oldParams);
			while (true) {
				String it = br.readLine();
				if (it == null) {
					break;
				}
				if (it.length() == 0) {
					writer.write(it);
					writer.write('\n');
				} else if (PARAM.matcher(it).matches()) {
					writeParam(writer, it);
				} else if (COMMENT.matcher(it).matches()) {
					writer.write(it);
					writer.write('\n');
				} else {
					throw new MprcException("Can't understand '" + it + "'");
				}
			}
		} catch (IOException e) {
			throw new MprcException("Cannot parse mascot parameter file.", e);
		} finally {
			FileUtilities.closeQuietly(writer);
		}
	}

	private void writeParam(Writer writer, String it) throws IOException {
		Matcher matcher = KEY_VALUE_COMMENT.matcher(it);
		if (!matcher.matches()) {
			throw new MprcException("Can't understand '" + it + "'");
		}

		String id = matcher.group(1);
		String value = matcher.group(2);

		if (nativeParams.keySet().contains(id)) {
			String newValue = nativeParams.get(id);
			if (!newValue.equals(value)) {
				writer.write(id + "=" + newValue);
				if (matcher.group(3) != null) {
					writer.write(matcher.group(3));
				}
				writer.write('\n');
			} else {
				writer.write(it);
				writer.write('\n');
			}
		} else {
			writer.write(it);
			writer.write('\n');
		}
	}

	public void setPeptideTolerance(MappingContext context, Tolerance peptideTolerance) {
		mapToleranceToNative(context, peptideTolerance, PEP_TOL_VALUE, PEP_TOL_UNIT);
	}

	public void setFragmentTolerance(MappingContext context, Tolerance fragmentTolerance) {
		mapToleranceToNative(context, fragmentTolerance, FRAG_TOL_VALUE, FRAG_TOL_UNIT);
	}

	public void setVariableMods(MappingContext context, ModSet variableMods) {
		TreeSet<String> mods = new TreeSet<String>();
		int i = 0;
		StringBuilder droppedMods = new StringBuilder();
		for (ModSpecificity ms : variableMods.getModifications()) {
			if (i > MAX_VARIABLE_MODS) {
				droppedMods.append(ms.toString());
				droppedMods.append(", ");
			} else {
				warnMascotMultipleSites(context, ms, variableMods.getModifications());
				mods.add(ms.toMascotString());
				i++;
			}
		}

		if (droppedMods.length() > 0) {
			droppedMods.setLength(droppedMods.length() - 2);
			context.reportWarning("Mascot supports up to " + MAX_VARIABLE_MODS + " variable modifications; dropping " + droppedMods);
		}

		setNativeMods(context, VAR_MODS, mods);
	}

	public void setFixedMods(MappingContext context, ModSet fixedMods) {
		TreeSet<String> mods = new TreeSet<String>();

		// we first loop through the mods and stuff their string reps into a hashset;
		// this eliminates duplicates wrt spec_group
		try {
			for (ModSpecificity ms : fixedMods.getModifications()) {
				warnMascotMultipleSites(context, ms, fixedMods.getModifications());
				mods.add(ms.toMascotString());
			}
		} catch (Exception t) {
			context.reportError("Problem obtaining mascot fixed modifications", t);
		}

		setNativeMods(context, FIXED_MODS, mods);
	}

	public String getNativeParam(String name) {
		return nativeParams.get(name);
	}

	public void setNativeParam(String name, String value) {
		nativeParams.put(name, value);
	}

	/**
	 * The short db name matches directly the db name in Mascot.
	 */
	public void setSequenceDatabase(MappingContext context, String shortDatabaseName) {
		setNativeParam(DATABASE, shortDatabaseName);
	}

	public void setProtease(MappingContext context, Protease protease) {
		String cle;
		if (!mascotNamesByEnzyme.containsKey(protease)) {
			cle = "Trypsin/P";
			context.reportWarning("Mascot doesn't support " + (protease == null ? "null enzyme" : protease.getName()) + ", using Trypsin (allow P)");
		} else {
			cle = mascotNamesByEnzyme.get(protease);
		}

		setNativeParam(ENZYME, cle);
	}

	public void setMissedCleavages(MappingContext context, Integer missedCleavages) {
		if (missedCleavages != null) {
			setNativeParam(MISSED_CLEAVAGES, String.valueOf(missedCleavages));
		}
	}

	public void setInstrument(MappingContext context, Instrument instrument) {
		String instName = instrument.getMascotName();
		setNativeParam(INSTRUMENT, instName);
	}

	private void setNativeMods(MappingContext context, String nativeParamName, Set<String> mods) {
		StringBuilder sb = new StringBuilder();

		for (String mod : mods) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			sb.append(mod);
		}

		if (context.noErrors()) {
			setNativeParam(nativeParamName, sb.toString());
		}

		if (VAR_MODS.equals(nativeParamName)) {
			checkForSameFixAndVariableMods(context);
		}
	}

	private void checkForSameFixAndVariableMods(MappingContext context) {
		String fixedMods = getNativeParam(FIXED_MODS);
		String variableMods = getNativeParam(VAR_MODS);

		if (fixedMods != null && fixedMods.length() > 0 && variableMods != null && variableMods.length() > 0) {
			String[] fixedModsArr = COMMA_SPLIT.split(fixedMods);
			String[] variableModsArr = COMMA_SPLIT.split(variableMods);

			StringBuilder repeatMods = null;

			int numRepeatMods = 0;
			for (String fixedMod : fixedModsArr) {
				for (String variableMod : variableModsArr) {
					if (fixedMod.trim().equals(variableMod.trim())) {
						numRepeatMods++;
						if (repeatMods == null) {
							repeatMods = new StringBuilder(fixedMod);
						} else {
							repeatMods.append(", ").append(fixedMod);
						}
					}
				}
			}

			if (repeatMods != null) {
				context.reportError((numRepeatMods == 1 ? "Modification" : "Modifications") + " " + repeatMods + " cannot be both fixed and variable", null);
			}
		}
	}

	private void warnMascotMultipleSites(MappingContext context, ModSpecificity ms, Set<ModSpecificity> allSets) {
		if (ms.getSpecificityGroup() != null && ms.groupSpecificities().size() > 1) {
			StringBuilder specificities = new StringBuilder();
			for (ModSpecificity modSpecificity : ms.groupSpecificities()) {
				if (!allSets.contains(modSpecificity)) {
					specificities.append(modSpecificity.getSite());
				}
			}
			if (specificities.toString().length() > 0) {
				context.reportWarning("Mascot will search additional site (" + specificities + ") for modification " + ms.toString());
			}
		}
	}

	private void mapToleranceToNative(MappingContext context, Tolerance unit, String tolName, String tolUnitName) {
		if (!Arrays.asList("ppm", "Da", "mmu").contains(unit.getUnit().getCode())) {
			setNativeParam(tolName, "1");
			setNativeParam(tolUnitName, "Da");
			context.reportWarning("Mascot does not support '" + unit + "' tolerances; using 1 Da instead.");
		} else {
			setNativeParam(tolName, String.valueOf(unit.getValue()));
			setNativeParam(tolUnitName, unit.getUnit().getCode());
		}
	}

	private Map<Protease, String> getNamesByEnzyme(Map<String, Protease> allowedHash, Map<String, String> namesHash) {
		Map<Protease, String> hash = new HashMap<Protease, String>();
		for (Map.Entry<String, String> e : namesHash.entrySet()) {
			hash.put(allowedHash.get(e.getKey()), e.getValue());
		}
		return hash;
	}
}
