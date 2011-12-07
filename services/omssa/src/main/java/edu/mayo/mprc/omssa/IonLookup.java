package edu.mayo.mprc.omssa;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

/**
 * Utility class for OMSSA's ion lookups.
 */
final class IonLookup {
	//these are the ions supported by OMSSA based on the ASN document.
	private static BiMap<String, String> ionSeriesLookup = new ImmutableBiMap.Builder<String, String>()
			.put("a", "0")
			.put("b", "1")
			.put("c", "2")
			.put("x", "3")
			.put("y", "4")
			.put("z", "5")
			.put("parent", "6")
			.put("internal", "7")
			.put("immonium", "8")
			.put("unknown", "9")
			.build();

	private IonLookup() {
	}

	/**
	 * From ion name (b) to ion enum number (1)
	 */
	public static String lookupEnum(String ion) {
		return ionSeriesLookup.get(ion);
	}

	/**
	 * From ion enum number (4) to ion name (y)
	 */
	public static String lookupIon(String ionEnum) {
		return ionSeriesLookup.inverse().get(ionEnum);
	}

}
