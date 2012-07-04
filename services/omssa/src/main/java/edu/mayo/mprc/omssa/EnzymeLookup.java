package edu.mayo.mprc.omssa;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Utility class for OMSSA's enzyme lookups.
 */
final class EnzymeLookup {
	//if the ommsa protease id signifies a combination then they are seperated by '+' and listed ascending within the string
	private static Map<String, String> enzymeLookup = new ImmutableMap.Builder<String, String>()
			.put("Trypsin (allow P)", "0")
			.put("Arg-C", "1")
			.put("Asp-N", "12")
					//	enzymeLookup.put("Asp-N_ambic", "unsupported")
			.put("Chymotrypsin", "3")
			.put("CNBr", "2")
			.put("Formic_acid", "4")
			.put("Lys-C (restrict P)", "5")
			.put("Lys-C (allow P)", "6")
			.put("PepsinA", "7")
			.put("Tryp-CNBr", "8")
			.put("TrypChymo", "9")
			.put("TrypChymoKRWFYnoP", "10+18")
			.put("Trypsin (restrict P)", "10")
			.put("V8-DE", "20")
			.put("V8-E", "13")
			.put("ChymoAndGluC", "3+13")
			.put("Non-Specific", "17")
			.put("GluC", "20")
			.build();

	private EnzymeLookup() {
	}

	public static String mapEnzymeAbstractToOmssa(final String abstractName) {
		return enzymeLookup.get(abstractName);
	}

}
