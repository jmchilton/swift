package edu.mayo.mprc.peaks;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.chem.AminoAcidSet;
import edu.mayo.mprc.peaks.core.PeaksSearchParameters;
import edu.mayo.mprc.swift.params2.Instrument;
import edu.mayo.mprc.swift.params2.MassUnit;
import edu.mayo.mprc.swift.params2.Protease;
import edu.mayo.mprc.swift.params2.Tolerance;
import edu.mayo.mprc.swift.params2.mapping.MappingContext;
import edu.mayo.mprc.swift.params2.mapping.Mappings;
import edu.mayo.mprc.unimod.ModSet;
import edu.mayo.mprc.unimod.ModSpecificity;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

/**
 * Mapping class for Peaks. Since we will never use it for mapping from the native parameters,
 * all the <code>mapXXXFromNative</code> methods can return <code>null</code>.
 */
public final class PeaksMappings implements Mappings {
	private static final String commentsBeginningCharacter = "#";
	private static final String nameValueDelimiter = "=";

	private Map<String, String> parameters;
	private Map<String, String> ensymeMapping;
	private Map<String, String> instrumentMapping;

	public PeaksMappings(final Map<String, String> ensymeMapping, final Map<String, String> instrumentMapping) {
		parameters = new HashMap<String, String>();
		this.ensymeMapping = ensymeMapping;
		this.instrumentMapping = instrumentMapping;
	}

	@Override
	public Reader baseSettings() {
		return ResourceUtilities.getReader("classpath:edu/mayo/mprc/swift/params/base.peaks.params", this.getClass());
	}

	public void read(final Reader isr) {
		BufferedReader bufferedReader = null;

		try {
			bufferedReader = new BufferedReader(isr);
			String line = null;
			String paramaterName = null;
			String paramaterValue = null;
			int delimieterIndex = 0;

			while ((line = bufferedReader.readLine()) != null) {
				if (!line.startsWith(commentsBeginningCharacter)) {
					delimieterIndex = line.indexOf(nameValueDelimiter);
					paramaterName = line.substring(0, delimieterIndex);
					paramaterValue = line.substring(delimieterIndex + 1);

					parameters.put(paramaterName, paramaterValue);
				}
			}

		} catch (Exception e) {
			throw new MprcException("Error reading Peaks paramater file.", e);
		} finally {
			FileUtilities.closeQuietly(bufferedReader);
		}
	}

	public void write(final Reader oldParams, final Writer out) {
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;

		try {
			bufferedReader = new BufferedReader(oldParams);
			bufferedWriter = new BufferedWriter(out);

			String line = null;
			String paramaterName = null;
			StringBuilder builder = null;
			int delimieterIndex = 0;

			while ((line = bufferedReader.readLine()) != null) {
				if (!line.startsWith(commentsBeginningCharacter)) {
					delimieterIndex = line.indexOf(nameValueDelimiter);
					paramaterName = line.substring(0, delimieterIndex);

					builder = new StringBuilder(paramaterName);
					builder.append(nameValueDelimiter);

					builder.append(parameters.get(paramaterName));

					bufferedWriter.write(builder.toString());
				} else {
					bufferedWriter.write(line);
				}

				bufferedWriter.newLine();
			}

		} catch (Exception e) {
			throw new MprcException("Error reading Peaks paramater file.", e);
		} finally {
			FileUtilities.closeQuietly(bufferedReader);
			FileUtilities.closeQuietly(bufferedWriter);
		}
	}

	public Tolerance mapPeptideToleranceFromNative(final MappingContext context) {
		return null;
	}

	public void setPeptideTolerance(final MappingContext context, final Tolerance peptideTolerance) {
		setNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_PARENTTOLERANCE, String.valueOf(peptideTolerance.getValue()));
		setNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_PARENTTOLERANCEUNIT, peptideTolerance.getUnit().getCode());
	}

	public Tolerance mapFragmentToleranceFromNative(final MappingContext context) {
		return null;
	}

	public void setFragmentTolerance(final MappingContext context, final Tolerance fragmentTolerance) {
		setNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_FRAGMENTTOLERANCE,
				fragmentTolerance.getUnit().equals(MassUnit.Ppm) ? String.valueOf(convertToDalton(context, fragmentTolerance)) : String.valueOf(fragmentTolerance.getValue()));
		setNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_FRAGMENTTOLERANCEUNIT, "Da");
	}

	private double convertToDalton(final MappingContext context, final Tolerance tolerance) {
		double value = tolerance.getValue();
		if (tolerance.getUnit().equals(MassUnit.Ppm)) {
			//convert ppm to Da assuming a average peptide mass of 1000 Da
			final double normMass = 1000d;
			value = value * normMass / 1000000d;
			context.reportWarning("Converted to " + value + " Da for Peaks.");
		}

		return value;
	}

	public ModSet mapVariableModsFromNative(final MappingContext context) {
		return null;
	}

	public void setVariableMods(final MappingContext context, final ModSet variableMods) {
		//If a fix modification is specific to a protein, the modification is changed to be variable and specific to a peptide. Therefore this
		//value may be already set to those modifications.
		final String currentValue = parameters.get(PeaksSearchParameters.SUBMIT_SEARCH_INPUTVARIABLEMODIES);

		setNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_INPUTVARIABLEMODIES, getInputModifications(context, variableMods, currentValue));
	}

	public ModSet mapFixedModsFromNative(final MappingContext context) {
		return null;
	}

	public void setFixedMods(final MappingContext context, final ModSet fixedMods) {

		final ModSet proteinMods = new ModSet();
		final ModSet nonProteinMods = new ModSet();

		for (final ModSpecificity modModSpecificity : fixedMods.getModifications()) {
			if (modModSpecificity.isProteinOnly()) {
				proteinMods.add(modModSpecificity);
				context.reportWarning("Modification: " + modModSpecificity.toString() + ", Peaks does not support Protein N/C-term, converting to peptite N/C-term.");
			} else {
				nonProteinMods.add(modModSpecificity);
			}
		}

		if (proteinMods.size() > 0) {
			setVariableMods(context, proteinMods);
		}

		final String parsedMods = getInputModifications(context, nonProteinMods, null);
		setNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_INPUTFIXEDMODIES, parsedMods);
	}

	public String mapSequenceDatabaseFromNative(final MappingContext context) {
		return null;
	}

	public void setSequenceDatabase(final MappingContext context, final String shortDatabaseName) {
		setNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_DATABASE, shortDatabaseName);
	}

	public Protease mapEnzymeFromNative(final MappingContext context) {
		return null;
	}

	public void setProtease(final MappingContext context, final Protease protease) {
		final String enzymeName = ensymeMapping.get(protease.getName());

		if (enzymeName == null) {
			context.reportWarning("Enzyme " + protease.toString() + " is not defiend by default in Peaks. Create enzyme with same name '" + protease.getName() + "' in Peaks if it does not already exist.");
			setNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_ENZYME, protease.getName());
		} else {
			setNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_ENZYME, enzymeName);
		}
	}

	public Integer mapMissedCleavagesFromNative(final MappingContext context) {
		return null;
	}

	public void setMissedCleavages(final MappingContext context, final Integer missedCleavages) {
		setNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_MISSCLEAVAGE, missedCleavages.toString());
	}

	public Instrument mapInstrumentFromNative(final MappingContext context) {
		return null;
	}

	public void setInstrument(final MappingContext context, final Instrument instrument) {
		final String instrumentName = instrumentMapping.get(instrument.getName());
		setNativeParam(PeaksSearchParameters.SUBMIT_SEARCH_INSTRUMENT, instrumentName);
	}

	/**
	 * Gets parameter value
	 *
	 * @param name Native param name. Set of parameter names is defined in class PeaksOnlineSearchParameters.
	 * @return
	 */
	public String getNativeParam(final String name) {
		return parameters.get(name);
	}

	public void setNativeParam(final String name, final String value) {
		parameters.put(name, value);
	}

	private String getPositionModNotation(final MappingContext context, final ModSpecificity modModSpecificity) {

		//Special cases where the site is N or C terminus. Peaks does not support specifying N or C terminus sites, so
		//This is translated to any site at the N or C terminus position.
		if (modModSpecificity.isSiteCTerminus()) {
			return "C";
		} else if (modModSpecificity.isSiteNTerminus()) {
			return "N";
		}

		if (modModSpecificity.isPositionAnywhere()) {
			return "A";
		} else if (modModSpecificity.isPositionCTerminus()) {
			return "C";
		} else if (modModSpecificity.isPositionNTerminus()) {
			return "N";
		}

		context.reportWarning("Peaks does not support modification position " + modModSpecificity.getTerm().getCode() + " using Anywhere");

		return "A";
	}

	private Set<String> getSiteModNotation(final MappingContext context, final ModSpecificity modModSpecificity) {
		final TreeSet<String> sites = new TreeSet<String>();

		//Special cases where the site is N or C terminus. Peaks does not support specifying N or C terminus sites, so
		//This is translated to any site at the N or C terminus position.
		if (modModSpecificity.isSiteCTerminus() || modModSpecificity.isSiteNTerminus()) {
			context.reportWarning("Modification: " + modModSpecificity.toString() + ", Peaks does not support " + modModSpecificity.getSite() + ", using non-specific terminus.");

			return (new AminoAcidSet()).getCodes();
		}

		sites.add(modModSpecificity.getSite().toString());

		return sites;
	}

	private TreeMap<Double, TreeMap<String, TreeSet<String>>> parseExistingModifications(final String modifications) {
		final TreeMap<Double, TreeMap<String, TreeSet<String>>> masses = new TreeMap<Double, TreeMap<String, TreeSet<String>>>();

		if (modifications != null && modifications.length() > 0) {
			TreeMap<String, TreeSet<String>> positions = null;
			TreeSet<String> sites = null;
			Double mass = null;
			String position = null;

			final String[] mods = modifications.split(",");
			String[] massArray = null;
			String[] positionArray = null;

			for (int i = 0; i < mods.length; i++) {
				massArray = mods[i].split("@");
				mass = new Double(massArray[0]);
				positionArray = massArray[1].split(":");
				position = positionArray[1];

				positions = masses.get(mass);

				if (positions == null) {
					positions = new TreeMap<String, TreeSet<String>>();
					positions.put(position, parseSites(positionArray[0]));
					masses.put(mass, positions);
				} else {
					sites = positions.get(position);

					if (sites == null) {
						positions.put(position, parseSites(positionArray[0]));
					} else {
						sites.addAll(parseSites(positionArray[0]));
					}
				}
			}
		}

		return masses;
	}

	private TreeSet<String> parseSites(final String sitesString) {
		final TreeSet<String> sites = new TreeSet<String>();

		for (int j = 0; j < sitesString.length(); j++) {
			sites.add(Character.toString(sitesString.charAt(j)));
		}

		return sites;
	}

	private String getInputModifications(final MappingContext context, final ModSet mods, final String existingValue) {
		final TreeMap<Double, TreeMap<String, TreeSet<String>>> masses = parseExistingModifications(existingValue);
		TreeMap<String, TreeSet<String>> positions = null;
		TreeSet<String> sites = null;
		Set<String> modSites = null;
		String position = null;

		for (final ModSpecificity modModSpecificity : mods.getModifications()) {

			position = getPositionModNotation(context, modModSpecificity);
			modSites = getSiteModNotation(context, modModSpecificity);

			positions = masses.get(modModSpecificity.getModification().getMassMono());

			if (positions != null) {
				sites = positions.get(position);

				if (sites != null) {
					sites.addAll(modSites);
				} else {
					sites = new TreeSet<String>();
					sites.addAll(modSites);
					positions.put(position, sites);
				}

			} else {
				positions = new TreeMap<String, TreeSet<String>>();
				sites = new TreeSet<String>();
				sites.addAll(modSites);
				positions.put(position, sites);
				masses.put(modModSpecificity.getModification().getMassMono(), positions);
			}
		}

		final StringBuilder sb = new StringBuilder();

		// Masses
		for (final Map.Entry<Double, TreeMap<String, TreeSet<String>>> me1 : masses.entrySet()) {
			//Positions
			for (final Map.Entry<String, TreeSet<String>> me2 : me1.getValue().entrySet()) {
				sb.append(me1.getKey().toString()).append("@");

				//Sites
				for (final String site : me2.getValue()) {
					sb.append(site);
				}

				sb.append(":").append(me2.getKey());
				sb.append(",");
			}
		}

		return sb.substring(0, sb.length() - 1);
	}
}
