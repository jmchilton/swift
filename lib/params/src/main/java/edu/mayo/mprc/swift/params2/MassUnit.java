package edu.mayo.mprc.swift.params2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit of mass.
 */
public enum MassUnit {
	Da("Da", Arrays.asList("Dalton", "Daltons"), "Daltons"),
	Ppm("ppm", new ArrayList<String>(0), "parts per million");

	private final String code;
	private final List<String> alternativeNames;
	private final String description;

	MassUnit(String code, List<String> alternativeNames, String description) {
		this.code = code;
		this.alternativeNames = alternativeNames;
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return code;
	}

	/**
	 * Get unit for given name.
	 */
	public static MassUnit getUnitForName(String name) {
		String trimmedName = name.trim();
		for (MassUnit unit : MassUnit.values()) {
			if (unit.getCode().equalsIgnoreCase(trimmedName)) {
				return unit;
			}
			for (String alternative : unit.alternativeNames) {
				if (alternative.equalsIgnoreCase(trimmedName)) {
					return unit;
				}
			}
		}
		return null;
	}

	/**
	 * @return A list of options the user can choose from, e.g. "Da, ppm".
	 */
	public static String getOptions() {
		StringBuilder units = new StringBuilder();
		for (MassUnit unit : MassUnit.values()) {
			units.append(", ").append(unit);
			for (String additional : unit.alternativeNames) {
				units.append(", ").append(additional);
			}
		}
		return units.substring(2);
	}
}
