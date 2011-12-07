package edu.mayo.mprc.sequest;

import com.google.common.base.Joiner;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.swift.params2.*;
import edu.mayo.mprc.swift.params2.mapping.MappingContext;
import edu.mayo.mprc.swift.params2.mapping.Mappings;
import edu.mayo.mprc.unimod.ModSet;
import edu.mayo.mprc.unimod.ModSpecificity;
import edu.mayo.mprc.unimod.Terminus;
import edu.mayo.mprc.unimod.Unimod;
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
	private static final String VAR_MODS = "diff_search_options";
	private static final String VAR_MODS_OPTIONS = "term_diff_search_options";
	private static final String DATABASE = "first_database_name";
	private static final String ENZYME = "enzyme_info";
	private static final String MISSED_CLEAVAGES = "max_num_internal_cleavage_sites";
	private static final String ION_SERIES = "ion_series";

	private static final Pattern FIXED = Pattern.compile("^add_([A-Z]|Nterm|Cterm)_(.*)");

	private static final Pattern UNIT = Pattern.compile("(\\d+).*");

	private static final Pattern SEQUEST_HEADER = Pattern.compile("\\[((?:SEQUEST)|(?:MAKEDB))\\]");

	private static final Pattern WHITESPACE = Pattern.compile("^\\s*$");
	private static final Pattern COMMENT = Pattern.compile("^\\s*(;.*)$");
	private static final Pattern EQUALS = Pattern.compile("^.*\\=.*$");
	private static final Pattern PARSE_LINE = Pattern.compile("^\\s*([^\\s=]+)\\s*=\\s*([^;]*)(\\s*;.*)?$");

	/**
	 * It is important to use linked hash map because the {@link edu.mayo.mprc.sequest.SequestToMakeDBConverter} depends
	 * on ordering of the native params.
	 * <p/>
	 * Also, just because of the converter, we store all native parameters, not just those that map to abstract params.
	 */
	private LinkedHashMap<String, String> nativeParams = new LinkedHashMap<String, String>();
	private static final double MMU_TO_DA = 0.001;

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

	public Tolerance mapPeptideToleranceFromNative(MappingContext context) {
		MassUnit u;
		double d;
		double factor = 1.0;

		String units = getNativeParam(PEP_TOL_UNIT);

		try {
			// ; 0=amu, 1=mmu, 2=ppm
			Matcher m = UNIT.matcher(units);
			if (!m.matches()) {
				throw new MprcException("The unit number appears to be missing");
			}
			int unit = Integer.parseInt(m.group(1));
			switch (unit) {
				case 0:
					u = MassUnit.Da;
					break;
				case 1:
					u = MassUnit.Da;
					factor = MMU_TO_DA; // 1 mmu = 0.001 Da
					break;
				case 2:
					u = MassUnit.Ppm;
					break;
				default:
					throw new MprcException("No such unit " + units);
			}
		} catch (Exception t) {
			throw new MprcException("Can't understand unit " + units, t);
		}

		String value = getNativeParam(PEP_TOL_VALUE);
		try {
			d = Double.parseDouble(value) * factor;
		} catch (Exception t) {
			throw new MprcException("Can't understand number " + value, t);
		}

		return new Tolerance(d, u);
	}

	public void mapPeptideToleranceToNative(MappingContext context, Tolerance peptideTolerance) {
		setNativeParam(PEP_TOL_VALUE, String.valueOf(peptideTolerance.getValue()));
		if (MassUnit.Da.equals(peptideTolerance.getUnit())) {
			setNativeParam(PEP_TOL_UNIT, "0");
			// MMU = 1 but we never use MMU
		} else if (MassUnit.Ppm.equals(peptideTolerance.getUnit())) {
			setNativeParam(PEP_TOL_UNIT, "2");
		}
	}

	public Tolerance mapFragmentToleranceFromNative(MappingContext context) {
		double d = 0.0;

		String it = getNativeParam(FRAG_TOL_VALUE);
		try {
			d = Double.parseDouble(it);
		} catch (Exception t) {
			context.reportError("Can't understand number " + it, t);
		}
		return new Tolerance(d, MassUnit.Da);
	}

	public void mapFragmentToleranceToNative(MappingContext context, Tolerance fragmentTolerance) {
		if (!MassUnit.Da.equals(fragmentTolerance.getUnit())) {
			setNativeParam(FRAG_TOL_VALUE, "1");
			context.reportWarning("Sequest does not support '" + fragmentTolerance.getUnit() + "' fragment tolerances; using 1 Da instead.");
		}
		setNativeParam(FRAG_TOL_VALUE, String.valueOf(fragmentTolerance.getValue()));
	}

	private static final Pattern SINGLE_MOD = Pattern.compile("(\\d+\\.\\d+) ([A-Z])");
	private static final Pattern OPTION = Pattern.compile("(\\d+\\.\\d+)\\s+(\\d+\\.\\d+)");

	public ModSet mapVariableModsFromNative(MappingContext context) {
		Unimod unimod = context.getAbstractParamsInfo().getUnimod();
		ModSet modspecs = new ModSet();

		String mods = getNativeParam(VAR_MODS);
		//15.99492 M 57.02146 C 0.000000 X 0.000000 T 0.000000 Y 0.000000 X
		int i = 0;
		Matcher matcher = SINGLE_MOD.matcher(mods);
		while (matcher.find()) {
			Double d = Double.parseDouble(matcher.group(1));
			if (d != 0.0D) {
				Character site = matcher.group(2).charAt(0);
				addModSpec(unimod, modspecs, d, site, Terminus.Anywhere, false, context);
			}
			i++;
		}
		if (i == 0) {
			throw new MprcException("Can't understand Sequest diff_search_options " + mods);
		}

		String options = getNativeParam(VAR_MODS_OPTIONS);
		// cterm nterm
		Matcher m2 = OPTION.matcher(options);
		if (!m2.matches()) {
			throw new MprcException("Can't understand term_diff_search_options " + options);
		}
		double cterm = Double.parseDouble(m2.group(1));
		double nterm = Double.parseDouble(m2.group(2));
		if (cterm != 0.0D) {
			// TODO: HACK. We need to abandon the reverse mapping altogether
			addModSpecTerm(unimod, modspecs, cterm, Terminus.Cterm, context);
		}
		if (nterm != 0.0D) {
			// TODO: HACK. We need to abandon the reverse mapping altogether
			addModSpecTerm(unimod, modspecs, nterm, Terminus.Nterm, context);
		}

		return modspecs;
	}

	public void mapVariableModsToNative(MappingContext context, ModSet variableMods) {
		StringBuilder sb = new StringBuilder();

		Double cterm = null;
		Double nterm = null;
		List<ModSpecificity> set = new ArrayList<ModSpecificity>(variableMods.getModifications());
		Collections.sort(set);

		int i = 0;

		for (ModSpecificity ms : set) {
			String title = ms.toString();
			double mass = ms.getModification().getMassMono();

			if (notSupportedMod(ms)) {
				context.reportWarning("Sequest does not support variable modification with position '" +
						ms.getTerm() + "' and site '" + ms.getSite() + "', dropping " + title);

			} else if (ms.isPositionNTerminus()) {
				if (nterm != null) {
					context.reportWarning("Sequest does not support multiple N-terminal variable modifications, dropping " + title);
				} else {
					nterm = mass;
				}
			} else if (ms.isPositionCTerminus()) {
				if (cterm != null) {
					context.reportWarning("Sequest does not support multiple C-terminal variable modifications, dropping " + title);
				} else {
					cterm = mass;
				}
			} else {
				i++;
				if (i > 6) {
					context.reportWarning("Sequest supports up to 6 variable modifications, skipping " + title);
					return;
				}
				if (sb.length() != 0) {
					sb.append(" ");
				}
				sb.append(mass).append(' ').append(ms.getSite());
			}
		}
		while (i < 6) {
			sb.append(sb.length() == 0 ? "" : " ").append("0.0000 X");
			i++;
		}

		setNativeParam(VAR_MODS, sb.toString());
		setNativeParam(VAR_MODS_OPTIONS, (cterm == null ? "0.0" : cterm) + " " + (nterm == null ? "0.0" : nterm));
	}

	/**
	 * Sequest does not support modification with specific amino acid at the protein terminus (we can do only
	 * amino acid anywhere, or anything at the terminus).
	 *
	 * @return true if Sequest supports given mod.
	 */
	private boolean notSupportedMod(ModSpecificity ms) {
		// we can't support specific amino acids at N or C terminus of peptide or protein.
		return (ms.isSiteAminoAcid() && (!ms.isPositionAnywhere() || ms.isProteinOnly()));
	}

	public ModSet mapFixedModsFromNative(MappingContext context) {
		Unimod unimod = context.getAbstractParamsInfo().getUnimod();
		ModSet modspecs = new ModSet();

		for (String p : nativeParams.keySet()) {
			Matcher matcher = FIXED.matcher(p);
			if (matcher.matches()) {
				String site = matcher.group(1);
				String text = matcher.group(2);
				Terminus terminus;
				Character aminoAcid;
				boolean proteinOnly = false;
				if (text.startsWith("protein") || text.startsWith("peptide")) {
					terminus = "Cterm".equalsIgnoreCase(site) ? Terminus.Cterm : Terminus.Nterm;
					aminoAcid = '*';
					if (text.startsWith("protein")) {
						proteinOnly = true;
					}
				} else {
					terminus = Terminus.Anywhere;
					aminoAcid = site.charAt(0);
				}
				try {
					double mass = Double.parseDouble(getNativeParam(p));
					if (mass != 0.0) {
						addModSpec(unimod, modspecs, mass, aminoAcid, terminus, proteinOnly, context);
					}
				} catch (Exception t) {
					context.reportWarning(t.getMessage());
				}
			}

		}
		return modspecs;
	}

	public void mapFixedModsToNative(MappingContext context, ModSet fixedMods) {
		// The key is in form [AA|Cterm|Nterm]_[protein|peptide]
		// We sum all the fixed mod contributions
		Map<String, Double> masses = new HashMap<String, Double>();
		for (ModSpecificity ms : fixedMods.getModifications()) {
			String title = ms.toString();
			String key;
			if (ms.isPositionAnywhere() && ms.isSiteAminoAcid()) {
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

	public String mapSequenceDatabaseFromNative(MappingContext context) {
		final String database = getNativeParam(DATABASE);
		if (database.startsWith("${DB:")) {
			return database.substring("${DB:".length(), database.length() - 1);
		}
		return database;
	}

	public void mapSequenceDatabaseToNative(MappingContext context, String shortDatabaseName) {
		setNativeParam(DATABASE, "${DB:" + shortDatabaseName + "}");
	}

	private static final Pattern PROTEASE = Pattern.compile("^(.*) (\\d) (\\d) ([A-Z\\-]+) ([A-Z\\-]+)$");

	/**
	 * How sequest models enzymes (I think):
	 * <p/>
	 * AspN 1 0 D -
	 * Enzyme_Name (Specificity) (Sense) (Permit) (Restrict)
	 * <p/>
	 * where:
	 * <p/>
	 * Specificity = 0 means non specific (no enzyme)
	 * <ul>
	 * <li>1 means fully specific (Number of "Tryptic" Terminii = 2)
	 * <li>2 means semi-specific at either terminus (NTT = 1)
	 * <li>3 means semi-specific at the N-terminus
	 * <li>4 means semi-specific at the C-terminus
	 * </ul>
	 * <p/>
	 * Sense
	 * <ul>
	 * <li>= 0 means protease cuts N-terminal to the amino(s) specified in Permit.
	 * <li>= 1 means protease cuts C-terminal to the amino(s) specified in Permit.
	 * </ul>
	 * <p/>
	 * Permit is a list of single letter amino acids that are allowed at cleavage site.
	 * Restrict is a list of single letter amino that are not permitted adjacent to the cleavage site.
	 * <p/>
	 * <p/>
	 * The full bioworks enzyme list:
	 * <code><pre>
	 * --- All full enzyme ---
	 * ./trypsinKR-Full.txt:enzyme_info = Trypsin(KR) 1 1 KR -
	 * ./aspn.txt:enzyme_info = AspN 1 0 D -
	 * ./chyrmotrypsin.txt:enzyme_info = Chymotrypsin 1 1 FWYL -
	 * ./chyrmotrypsinFWY.txt:enzyme_info = Chymotrypsin(FWY) 1 1 FWY P
	 * ./clostripain.txt:enzyme_info = Clostripain 1 1 R -
	 * ./cyanogen_bromide.txt:enzyme_info = Cyanogen_Bromide 1 1 M -
	 * ./elastase-tryp-chymo.txt:enzyme_info = Elastase/Tryp/Chymo 1 1 ALIVKRWFY P
	 * ./elastase.txt:enzyme_info = Elastase 1 1 ALIV P
	 * ./gluc.txt:enzyme_info = GluC 1 1 ED -
	 * ./iodosobenzoate.txt:enzyme_info = IodosoBenzoate 1 1 W -
	 * ./lysc.txt:enzyme_info = LysC 1 1 K -
	 * ./proline_endopept.txt:enzyme_info = Proline_Endopept 1 1 P -
	 * ./staph_protease.txt:enzyme_info = Staph_Protease 1 1 E -
	 * ./trypsinKR-P.txt:enzyme_info = Trypsin(KR/P) 1 1 KR P
	 * ./trypsinKRLNH-P.txt:enzyme_info = Trypsin(KRLNH/P) 1 1 KRLNH P
	 * ./trypsinKRLNH.txt:enzyme_info = Trypsin(KRLNH) 1 1 KRLNH -
	 * ./trypsin_k.txt:enzyme_info = Trypsin_K 1 1 K P
	 * ./trypsin_r.txt:enzyme_info = Trypsin_R 1 1 R P
	 * ./noenzyme.txt:enzyme_info = No_Enzyme 0 0 - -
	 * <p/>
	 * --- different cleavage options ---
	 * <p/>
	 * ./trypsinKR-ct.txt:enzyme_info = Trypsin(KR) 4 1 KR -
	 * ./trypsinKR-eitherend.txt:enzyme_info = Trypsin(KR) 2 1 KR -
	 * ./trypsinKR-nt.txt:enzyme_info = Trypsin(KR) 3 1 KR -
	 * ./trypsinKR-Full.txt:enzyme_info = Trypsin(KR) 1 1 KR -
	 * </pre></code>
	 */
	public Protease mapEnzymeFromNative(MappingContext context) {
		Iterable<Protease> proteases = context.getAbstractParamsInfo().getEnzymeAllowedValues();

		String rn = null;
		String rnminus1 = null;
		Protease p = null;

		String it = getNativeParam(ENZYME);

		Matcher m = PROTEASE.matcher(it);
		if (!m.matches()) {
			throw new MprcException("Can't understand enzyme_info " + it);
		}

		if ("0".equals(m.group(2))) {
			rn = "";
			rnminus1 = "";
		} else {
			rnminus1 = "0".equals(m.group(3)) ? m.group(5) : m.group(4);
			if ("-".equals(rnminus1)) {
				rnminus1 = "";
			}
			rn = "0".equals(m.group(3)) ? m.group(4) : m.group(5);
			if ("-".equals(rn)) {
				rn = "";
			} else {
				rn = '!' + rn;
			}
		}

		for (Protease protease : proteases) {
			if (protease.getRn().equals(rn) && protease.getRnminus1().equals(rnminus1)) {
				if (p != null) {
					throw new MprcException("Multiple enzymes match Sequest enzyme_info rnminus1=" + rnminus1 + ", rn=" + rn);
				}
				p = protease;
			}
		}
		if (p == null) {
			throw new MprcException("Unknown Sequest enzyme rnminus1=" + rnminus1 + ", rn=" + rn);
		}
		return p;
	}

	public void mapEnzymeToNative(MappingContext context, Protease enzyme) {

		String name = enzyme.getName().replaceAll("\\s", "_");
		String firstDigit = "1";
		String secondDigit = "1";
		String rn = enzyme.getRn();
		String rnminus1 = enzyme.getRnminus1();

		// how do we recognize sense == 0 (ie inverted match)?
		if ("Non-Specific".equals(enzyme.getName())) {
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
			throw new MprcException("Enzyme " + enzyme.getName() + " cannot be used by Sequest");
		}

		setNativeParam(ENZYME,
				name + " " + firstDigit + " " + secondDigit + " " +
						(rnminus1.equals("") ? "-" : rnminus1) + " " +
						(rn.equals("") ? "-" : rn));
	}

	public Integer mapMissedCleavagesFromNative(MappingContext context) {
		Integer value;
		String it = getNativeParam(MISSED_CLEAVAGES);
		try {
			value = Integer.parseInt(it);
		} catch (Exception t) {
			throw new MprcException("Can't understand Sequest missed cleavages " + it, t);
		}

		return value;
	}

	public void mapMissedCleavagesToNative(MappingContext context, Integer missedCleavages) {
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

	/**
	 * See also {@link #INSTRUMENT_SERIES} and {@link #R}.
	 * <pre>
	 * sequest ion_series "0           1           1           0.0 1.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0"
	 *                    "(neutral-a) (neutral-b) (neutral-y) a   b   c   d   v   w   x   y   z"
	 * </pre>
	 */
	public Instrument mapInstrumentFromNative(MappingContext context) {
		Map<String, IonSeries> ionseries = context.getAbstractParamsInfo().getIons();

		HashSet<IonSeries> hasseries = new HashSet<IonSeries>();

		String ionSeries = getNativeParam(ION_SERIES);

		Matcher matcher = R.matcher(ionSeries);
		if (!matcher.matches()) {
			throw new MprcException("Can't understand sequest ion_series " + ionSeries);
		}
		for (int i = 0; i < INSTRUMENT_SERIES.length; ++i) {
			Double d = Double.parseDouble(matcher.group(i + 1));
			if (d > 0) {
				hasseries.add(ionseries.get(INSTRUMENT_SERIES[i]));
			}
		}

		if (hasseries.size() == 2
				&& hasseries.contains(ionseries.get("y"))
				&& hasseries.contains(ionseries.get("b"))) {
			// We cannot determine which instrument it is - two of them match these ions
			return null;
		}

		Instrument instrument = Instrument.findInstrumentMatchingSeries(hasseries, context.getAbstractParamsInfo().getInstrumentAllowedValues());
		if (instrument == null) {
			String seriesnames = Joiner.on(" ").join(hasseries);
			throw new MprcException("Can't find instrument matching sequest ion series "
					+ seriesnames);
		}
		return instrument;
	}

	public void mapInstrumentToNative(MappingContext context, Instrument it) {
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

	private static final double MOD_MASS_TOL = MMU_TO_DA;

	/**
	 * Add a given modification to modspecs, doing proper check and issuing warnings in case of trouble.
	 */
	private static void addModSpec(Unimod unimod, ModSet modspecs, double d, Character site, Terminus terminus, Boolean proteinOnly, MappingContext context) {
		String message = null;

		Set<ModSpecificity> specs = unimod.findMatchingModSpecificities(d - MOD_MASS_TOL, d + MOD_MASS_TOL, site, terminus, proteinOnly, null);
		if (specs.size() == 1) {
			modspecs.add(specs.iterator().next());
		} else {
			String title = d + "@" + site;
			if (specs.size() == 0) {
				message = "Can't find modification " + title;
			} else {
				context.reportWarning("" + specs.size() + " modifications have same mass (within " + MOD_MASS_TOL + " Da of " + title + ")");
			}
		}
		if (message != null) {
			throw new MprcException(message);
		}
	}

	/**
	 * Add a given modification to modspecs, doing proper check and issuing warnings in case of trouble.
	 * The modification is to occur at the terminus. We try position="Anywhere", "Any TERM" and "Protein TERM" in a row,
	 * stopping if we find a mod that works.
	 * TODO: HACK - remove reverse mapping altogether
	 */
	private static void addModSpecTerm(Unimod unimod, ModSet modspecs, double d, Terminus terminus, MappingContext context) {
		String title = d + "@" + terminus.name();

		if (tryAddMod(unimod, modspecs, d, '*', terminus, false, context, title)) {
			return;
		}
		if (tryAddMod(unimod, modspecs, d, '*', terminus, true, context, title)) {
			return;
		}

		throw new MprcException("Can't find modification " + title);
	}

	private static boolean tryAddMod(Unimod unimod, ModSet modspecs, double d, Character site, Terminus terminus, Boolean proteinOnly, MappingContext context, String title) {
		Set<ModSpecificity> specs = unimod.findMatchingModSpecificities(d - MOD_MASS_TOL,
				d + MOD_MASS_TOL, site, terminus, proteinOnly, null);
		if (specs.size() == 1) {
			modspecs.add(specs.iterator().next());
			return true;
		} else if (specs.size() > 1) {
			context.reportWarning("" + specs.size() + " modifications have same mass (within " + MOD_MASS_TOL + " Da of " + title + ")");
			return true;
		}
		return false;
	}

}
