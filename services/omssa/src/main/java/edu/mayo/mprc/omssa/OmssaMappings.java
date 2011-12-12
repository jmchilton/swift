package edu.mayo.mprc.omssa;

import com.google.common.base.Joiner;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.swift.params2.*;
import edu.mayo.mprc.swift.params2.mapping.MappingContext;
import edu.mayo.mprc.swift.params2.mapping.Mappings;
import edu.mayo.mprc.unimod.ModSet;
import edu.mayo.mprc.utilities.ResourceUtilities;
import edu.mayo.mprc.utilities.xml.XMLUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OmssaMappings implements Mappings {
	private static final String DATABASE = "db";
	private static final String PEP_TOL = "peptol";
	private static final String FRAG_TOL = "msmstol";
	private static final String MISSED_CLEAVAGES = "missedcleave";

	private final Map<String, Node> nativeParams = new HashMap<String, Node>();
	private Document nativeParamsDocument;
	private UnimodOmssaConverter converter = new UnimodOmssaConverter(new ModsUtilities());
	private static final String ENZYME = "enzyme";
	private static final double AVG_PEPTIDE_MASS = 1000d;
	private static final double MILLION = 1000000d; // For part-per-million conversions

	public OmssaMappings() {
	}

	@Override
	public Reader baseSettings() {
		return ResourceUtilities.getReader("classpath:edu/mayo/mprc/swift/params/base.omssa.params.xml", this.getClass());
	}

	public void read(Reader isr) {
		Document doc = XMLUtilities.loadDocument(isr);
		nativeParamsDocument = doc;
		NodeList childNodes = doc.getDocumentElement().getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node.getNodeName().startsWith("MSSearchSettings_")) {
				String id = node.getNodeName().replaceFirst("MSSearchSettings_", "");
				nativeParams.put(id, node);
			}
		}
	}

	public void write(Reader oldParams, Writer out) {
		Document bioml = nativeParamsDocument;
		// Write the DOM document to the file
		StreamSource tSource = new StreamSource(new StringReader(XMLUtilities.INDENT_TRANSFORM_STRING));
		try {
			Transformer xformer = TransformerFactory.newInstance().newTransformer(tSource);
			xformer.transform(new DOMSource(bioml.getDocumentElement()), new StreamResult(out));
		} catch (Exception t) {
			throw new MprcException("Cannot save omssa parameter file", t);
		}
	}

	public String getNativeParam(String name) {
		return nativeParams.get(name).getTextContent();
	}

	public void setNativeParam(String name, String value) {
		nativeParams.get(name).setTextContent(value);
	}

	public void mapPeptideToleranceToNative(MappingContext context, Tolerance peptideTolerance) {
		if (!MassUnit.Da.equals(peptideTolerance.getUnit()) && !MassUnit.Ppm.equals(peptideTolerance.getUnit())) {
			//the user is trying to use ppm or an unsupported unit
			setNativeParam(PEP_TOL, "1");
			context.reportWarning("OMSSA does not support '" + peptideTolerance.getUnit() + "' tolerances; using 1 Dalton instead.");
			return;
		}

		double value = convertToDalton(context, true, peptideTolerance);
		setNativeParam(PEP_TOL, String.valueOf(value));
	}

	private double convertToDalton(MappingContext context, boolean ppmAllowed, Tolerance tolerance) {
		double value = tolerance.getValue();
		if (ppmAllowed && tolerance.getUnit().equals(MassUnit.Ppm)) {
			//convert ppm to Da assuming a average peptide mass of 1000 Da
			double normMass = AVG_PEPTIDE_MASS;
			value = value * normMass / MILLION;
			context.reportWarning("Converted to " + value + " Da for OMSSA.");
		}
		return value;
	}

	public void mapFragmentToleranceToNative(MappingContext context, Tolerance fragmentTolerance) {
		if (!fragmentTolerance.getUnit().equals(MassUnit.Da)) {
			//the user is trying to use ppm or an unsupported unit
			setNativeParam(FRAG_TOL, "1");
			context.reportWarning("OMSSA does not support '" + fragmentTolerance.getUnit() + "' tolerances; using 1 Dalton instead.");
		}
		//we can have a nice straight-forward conversion to Da from mmu
		double value = convertToDalton(context, false, fragmentTolerance);
		setNativeParam(FRAG_TOL, String.valueOf(value));
	}

	private NodeList getMsModSpecs() {
		return ((Element) ((Element) nativeParams.get("usermods")).getElementsByTagName("MSModSpecSet").item(0)).getElementsByTagName("MSModSpec");
	}

	public void mapVariableModsToNative(MappingContext context, ModSet variableMods) {
		Document doc = nativeParamsDocument;
		try {
			converter.convertUnimodToOmssa(/*fixed*/ false, variableMods.getModifications(), doc);
		} catch (Exception t) {
			throw new MprcException(t);
		}
	}

	public void mapFixedModsToNative(MappingContext context, ModSet fixedMods) {
		//this converter will insert necessary into the document
		try {
			converter.convertUnimodToOmssa(/*fixed*/ true, fixedMods.getModifications(), nativeParamsDocument);
		} catch (Exception t) {
			context.reportError(t.getMessage(), t);
		}
	}

	public void mapSequenceDatabaseToNative(MappingContext context, String shortDatabaseName) {
		setNativeParam(DATABASE, "${DB:" + shortDatabaseName + "}");
	}

	public void mapEnzymeToNative(MappingContext context, Protease enzyme) {
		String omssaId = EnzymeLookup.mapEnzymeAbstractToOmssa(enzyme.getName());

		if (omssaId == null) {
			context.reportWarning("OMSSA cannot support the enzyme " + enzyme.getName() + " so please disable OMSSA if you want this enzyme.");
			return; //don't change the enzyme
		}

		//remove previously selected enzymes.
		final Element enzymeElement = (Element) nativeParams.get(ENZYME);
		NodeList previouslySelected = enzymeElement.getElementsByTagName("MSEnzymes");
		for (int i = previouslySelected.getLength() - 1; i >= 0; i--) {
			enzymeElement.removeChild(previouslySelected.item(i));
		}

		if (omssaId.contains("+")) {
			String[] ids = omssaId.split("\\+");
			for (String id : ids) {
				//all of the templates should have empty enzyme sections so just add them as children
				Element newEnzyme = enzymeElement.getOwnerDocument().createElement("MSEnzymes");
				newEnzyme.setTextContent(id);
				enzymeElement.appendChild(newEnzyme);
			}
		} else {
			Element newEnzyme = enzymeElement.getOwnerDocument().createElement("MSEnzymes");
			newEnzyme.setTextContent(omssaId);
			enzymeElement.appendChild(newEnzyme);
		}
	}

	public void mapMissedCleavagesToNative(MappingContext context, Integer missedCleavages) {
		String value = null;
		try {
			value = String.valueOf(missedCleavages);
			setNativeParam(MISSED_CLEAVAGES, value);
		} catch (Exception t) {
			throw new MprcException("Can't understand OMSSA missed cleavages " + missedCleavages + " using " + value, t);
		}
	}

	public void mapInstrumentToNative(MappingContext context, Instrument instrument) {
		Document doc = nativeParamsDocument;
		List<String> ionSeriesIds = new ArrayList<String>();

		List<String> unsupportedIons = new ArrayList<String>();
		for (IonSeries ionSeries : instrument.getSeries()) {
			String id = IonLookup.lookupEnum(ionSeries.getName());
			if (id == null) {
				unsupportedIons.add(ionSeries.getName());
			} else {
				ionSeriesIds.add(id);
			}
		}
		if (unsupportedIons.size() > 0) {
			context.reportWarning("OMSSA does not support ions: " + Joiner.on(", ").join(unsupportedIons));
		}
		Element elemIons = (Element) nativeParams.get("ionstosearch");
		// Empty this element
		while (elemIons.hasChildNodes()) {
			elemIons.removeChild(elemIons.getFirstChild());
		}

		for (String id : ionSeriesIds) {
			Element newIon = doc.createElement("MSIonType");
			newIon.setTextContent(id);
			elemIons.appendChild(newIon);
		}
	}
}

