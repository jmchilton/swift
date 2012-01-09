package edu.mayo.mprc.xtandem;

import com.google.common.base.Joiner;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.swift.params2.*;
import edu.mayo.mprc.swift.params2.mapping.MappingContext;
import edu.mayo.mprc.swift.params2.mapping.Mappings;
import edu.mayo.mprc.unimod.ModSet;
import edu.mayo.mprc.unimod.ModSpecificity;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.ResourceUtilities;
import edu.mayo.mprc.utilities.xml.XMLUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XTandemMappings implements Mappings {
	public static final String DATABASE_TAXON = "database1";

	private static final String PEP_TOL_UNIT = "spectrum, parent monoisotopic mass error units";
	private static final String PEP_TOL_PLUS = "spectrum, parent monoisotopic mass error plus";
	private static final String PEP_TOL_MINUS = "spectrum, parent monoisotopic mass error minus";
	private static final String FRAG_TOL_UNIT = "spectrum, fragment monoisotopic mass error units";
	private static final String FRAG_TOL_VALUE = "spectrum, fragment monoisotopic mass error";
	private static final String VAR_MODS = "residue, potential modification mass";
	private static final String FIXED_MODS = "residue, modification mass";
	private static final String DATABASE = "protein, taxon";
	private static final String ENZYME = "protein, cleavage site";
	private static final String MISSED_CLEAVAGES = "scoring, maximum missed cleavage sites";

	private static final Pattern IONS_PATTERN = Pattern.compile("^scoring, (.*) ions");

	private Map<String, String> nativeParams = new HashMap<String, String>();
	private static final int ALLOW_NON_SPECIFIC_CLEAVAGES = 50;
	private static final String DALTONS = "Daltons";

	public XTandemMappings() {
	}

	@Override
	public Reader baseSettings() {
		return ResourceUtilities.getReader("classpath:edu/mayo/mprc/swift/params/base.tandem.xml.template", this.getClass());
	}

	public void read(Reader isr) {
	}

	public void write(Reader oldParams, Writer out) {
		Document bioml;
		try {
			bioml = XMLUtilities.loadDocument(oldParams);

			Element doc = bioml.getDocumentElement();
			NodeList notes = doc.getElementsByTagName("note");
			for (int i = 0; i < notes.getLength(); i++) {
				Node it = notes.item(i);
				if (it.hasAttributes() &&
						"input".equals(it.getAttributes().getNamedItem("type").getTextContent())) {

					// TODO if the nextSibling has no type attribute, use it as the comment.
					final String id = it.getAttributes().getNamedItem("label").getTextContent();
					if (nativeParams.keySet().contains(id)) {
						it.setTextContent(nativeParams.get(id));
					}
				}
			}
		} catch (Exception t) {
			throw new MprcException("Error reading X!Tandem parameter set", t);
		}

		// Prepare the output file
		Result result = new StreamResult(out);

		// Write the DOM document to the file
		try {
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			DOMSource ds = new DOMSource(bioml.getDocumentElement());
			xformer.transform(ds, result);
		} catch (TransformerException e) {
			throw new MprcException("XML transformer error when writing tandem parameter set", e);
		} finally {
			FileUtilities.closeQuietly(out);
		}
	}

	public String getNativeParam(String name) {
		return nativeParams.get(name);
	}

	public void setNativeParam(String name, String value) {
		nativeParams.put(name, value);
	}

	public void setPeptideTolerance(MappingContext context, Tolerance peptideTolerance) {
		double value = peptideTolerance.getValue();
		String unit = peptideTolerance.getUnit().getCode();
		if (peptideTolerance.getUnit().equals(MassUnit.Da)) {
			unit = DALTONS;
		}
		setNativeParam(PEP_TOL_PLUS, String.valueOf(value));
		setNativeParam(PEP_TOL_MINUS, String.valueOf(value));
		setNativeParam(PEP_TOL_UNIT, unit);
	}

	public void setFragmentTolerance(MappingContext context, Tolerance fragmentTolerance) {
		if (!MassUnit.Da.equals(fragmentTolerance.getUnit())) {
			setNativeParam(FRAG_TOL_VALUE, "1");
			setNativeParam(FRAG_TOL_UNIT, DALTONS);
			context.reportWarning("Tandem does not support '" + fragmentTolerance.toString() + "' fragment tolerances; using 1 Da instead.");
			return;
		}
		setNativeParam(FRAG_TOL_VALUE, String.valueOf(fragmentTolerance.getValue()));
		setNativeParam(FRAG_TOL_UNIT, DALTONS);
	}

	public void setVariableMods(MappingContext context, ModSet variableMods) {
		mapModsToNative(context, variableMods, VAR_MODS);
	}

	public void setFixedMods(MappingContext context, ModSet fixedMods) {
		mapModsToNative(context, fixedMods, FIXED_MODS);
	}

	private void mapModsToNative(MappingContext context, ModSet variableMods, String modVariable) {
		List<String> mods = new ArrayList<String>();

		for (ModSpecificity ms : variableMods.getModifications()) {
			String title = ms.toString();
			double mass = ms.getModification().getMassMono();

			// "Protein [NC]-term"
			final boolean proteinNcTerm = ms.isProteinOnly() != null && ms.isProteinOnly();
			// we can't support specific amino acids at N or C terminus
			final boolean specificAminoAcidAtNorCterminus = !ms.isPositionAnywhere() && ms.isSiteAminoAcid();

			if ((ms.getTerm() != null) && (specificAminoAcidAtNorCterminus || proteinNcTerm)) {

				context.reportWarning("Tandem does not support " + (VAR_MODS.equals(modVariable) ? "variable" : "fixed") + " modification with position '" +
						ms.getTerm() + "' and site '" + ms.getSite() + "', dropping " + title);
			} else {
				String site = String.valueOf(ms.getSite());
				if (ms.isPositionNTerminus()) {
					site = "[";
				} else if (ms.isPositionCTerminus()) {
					site = "]";
				}

				mods.add(mass + "@" + site);
			}
		}
		setNativeParam(modVariable, makeCanonicalModsString(mods));
	}

	private static String makeCanonicalModsString(List<String> mods) {
		String[] modsArray = new String[mods.size()];
		mods.toArray(modsArray);
		Arrays.sort(modsArray);
		return Joiner.on(",").join(modsArray);
	}

	public void setSequenceDatabase(MappingContext context, String shortDatabaseName) {
		setNativeParam(DATABASE, DATABASE_TAXON);
	}

	private static final Pattern TANDEM_MOD = Pattern.compile("\\s*([\\[\\{])([A-Z]*)[\\]\\}]\\|([\\[\\{])([A-Z]*)[\\]\\}]\\s*");

	public void setProtease(MappingContext context, Protease protease) {
		String cle = null;
		String rnminus1 = protease.getRnminus1();
		String rn = protease.getRn();

		if (rn.length() == 0 && rnminus1.length() == 0) {
			rnminus1 = "X";
			rn = "X";
			// handle the case where we set to 50 in allow non-specific cleavages
			setNativeParam(MISSED_CLEAVAGES, "50");
		} else {
			if (rnminus1.length() == 0) {
				rnminus1 = "X";
			}
			if (rn.length() == 0) {
				rn = "X";
			}
		}

		if (rnminus1.startsWith("!")) {
			cle = "{" + rnminus1.substring(1) + "}";
		} else {
			cle = "[" + rnminus1 + "]";
		}
		cle += "|";

		if (rn.startsWith("!")) {
			cle += "{" + rn.substring(1) + "}";
		} else {
			cle += "[" + rn + "]";
		}
		setNativeParam(ENZYME, cle);
	}

	public void setMissedCleavages(MappingContext context, Integer missedCleavages) {
		String value = null;

		try {
			if (missedCleavages != ALLOW_NON_SPECIFIC_CLEAVAGES) {
				//handle the case where we set to 50 in allow non-specific cleavages
				value = String.valueOf(missedCleavages);
			}
		} catch (NumberFormatException ignore) {
			// SWALLOWED
			context.reportWarning("Can't understand tandem missed cleavages: " + missedCleavages);
		}

		if (value != null) {
			setNativeParam(MISSED_CLEAVAGES, value);
		}
	}

	public void setInstrument(MappingContext context, Instrument instrument) {
		Map<String, IonSeries> hasSeries = new HashMap<String, IonSeries>();
		for (IonSeries is : instrument.getSeries()) {
			hasSeries.put(is.getName(), is);
		}
		for (String p : nativeParams.keySet()) {
			Matcher matcher = IONS_PATTERN.matcher(p);
			if (matcher.matches()) {
				String seriesname = matcher.group(1);
				if (hasSeries.containsKey(seriesname)) {
					setNativeParam(p, "yes");
					hasSeries.remove(seriesname);
				} else {
					setNativeParam(p, "no");
				}
			}
		}

		for (String seriesname : hasSeries.keySet()) {
			context.reportWarning("Tandem doesn't support ion series " + seriesname);
		}
	}
}
