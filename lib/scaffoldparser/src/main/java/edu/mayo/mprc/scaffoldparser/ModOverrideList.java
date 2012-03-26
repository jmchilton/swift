package edu.mayo.mprc.scaffoldparser;

import edu.mayo.mprc.MprcException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A list of modifications to be overriden. The list is expressed as <code>modName1=massShift1,modName2=massShift2,...</code>.
 * Use {@link #parse(String)} method to get an instance easily.
 */
public final class ModOverrideList {
	private final Map<String, Double> overrideMap;

	public ModOverrideList(final List<ModOverride> overrideList) {
		overrideMap = new HashMap<String, Double>();
		for (final ModOverride o : overrideList) {
			overrideMap.put(o.getModName(), o.getModMassShift());
		}
	}

	public boolean isOverriden(final String modName) {
		return overrideMap.containsKey(modName);
	}

	public double getMassShift(final String modName) {
		return overrideMap.get(modName);
	}

	public static ModOverrideList parse(final String list) {
		final String[] parts = list.split(";");
		final List<ModOverride> overrides = new ArrayList<ModOverride>();
		for (final String part : parts) {
			final String[] mod = part.split("=", 2);
			if (mod.length != 2) {
				throw new MprcException("Incorrect modification override: " + part);
			}
			final double massShift;
			try {
				massShift = Double.parseDouble(mod[1]);
			} catch (Exception t) {
				throw new MprcException("Mass shift was not a number when parsing modification overrides: " + mod[1], t);
			}
			overrides.add(new ModOverride(mod[0], massShift));
		}
		return new ModOverrideList(overrides);
	}

	@Override
	public String toString() {
		final StringBuilder out = new StringBuilder();
		for (final Map.Entry<String, Double> entry : overrideMap.entrySet()) {
			out.append(entry.getKey())
					.append("=")
					.append(entry.getValue())
					.append(",");
		}
		return out.substring(0, out.length() - 1);
	}

	public static class ModOverride {
		private final String modName;
		private final double modMassShift;

		public ModOverride(final String modName, final double modMassShift) {
			this.modName = modName;
			this.modMassShift = modMassShift;
		}

		public String getModName() {
			return modName;
		}

		public double getModMassShift() {
			return modMassShift;
		}
	}
}
