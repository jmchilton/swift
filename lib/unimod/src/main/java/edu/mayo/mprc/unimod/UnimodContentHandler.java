package edu.mayo.mprc.unimod;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Roman Zenka
 */
class UnimodContentHandler extends DefaultHandler {
	private static final Logger LOGGER = Logger.getLogger(UnimodContentHandler.class);
	private final Unimod into;

	private ModBuilder currentMod;
	private SpecificityBuilder currentSpecificity;

	/**
	 * True if there is a specificity currently being worked on.
	 */
	private Double currentMassMono;
	private Double currentMassAverage;
	private String currentComposition;

	private StringBuilder tagScanner = new StringBuilder();

	public UnimodContentHandler(final Unimod into) {
		this.into = into;
	}

	public static Double getDoubleValue(Attributes attr, String attrName) {
		Double value = null;
		try {
			value = new Double(attr.getValue("", attrName));
		} catch (Exception ignore) {
			//SWALLOWED: just allow null to be returned
		}
		return value;
	}

	public static Boolean getBooleanValue(Attributes attr, String attrName) {
		Boolean value = null;
		try {
			String strValue = attr.getValue("", attrName);
			value = (!strValue.equals("0"));
		} catch (Exception ignore) {
			//SWALLOWED: allow null return;
		}
		return value;
	}

	public static Integer getIntegerValue(Attributes attr, String attrName) {
		Integer value = null;
		try {
			String strValue = attr.getValue("", attrName);
			value = Integer.valueOf(strValue);
		} catch (Exception ignore) {
			// SWALLOWED: allow null return;
		}
		return value;
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attr) {
		if (localName.equals("mod")) {
			currentMod = new ModBuilder();
			currentMod.setTitle(attr.getValue("", "title"));
			currentMod.setFullName(attr.getValue("", "full_name"));
			currentMod.setRecordID(getIntegerValue(attr, "record_id"));

		} else if (localName.equals("alt_name")) {
			tagScanner = new StringBuilder();
		} else if (localName.equals("specificity")) {
			// We are working on a new specificity
			currentSpecificity = currentMod.addSpecificityFromUnimod(
					attr.getValue("", "site"),
					attr.getValue("", "position"),
					getBooleanValue(attr, "hidden"),
					attr.getValue("", "classification"),
					getIntegerValue(attr, "spec_group"));
		} else if (localName.equals("delta")) {
			currentMassMono = getDoubleValue(attr, "mono_mass");
			currentMassAverage = getDoubleValue(attr, "avge_mass");
			currentComposition = attr.getValue("", "composition");
		} else if (localName.equals("unimod")) {
			into.setMajorVersion(attr.getValue("majorVersion"));
			into.setMinorVersion(attr.getValue("minorVersion"));
		} else if (!localName.equals("misc_notes")
				&& !localName.equals("elem")
				&& !localName.equals("element")
				&& !localName.equals("elements")
				&& !localName.equals("xref")
				&& !localName.equals("text")
				&& !localName.equals("source")
				&& !localName.equals("url")
				&& !localName.equals("brick")
				&& !localName.equals("aminoAcids")
				&& !localName.equals("Ignore")
				&& !localName.equals("NeutralLoss")
				&& !localName.equals("modifications")
				&& !localName.equals("aa")
				&& !localName.equals("amino_acids")
				&& !localName.equals("mod_bricks")) {
			LOGGER.info("Unused unimod element: " + qualifiedName);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if (this.tagScanner != null) {
			for (int i = 0; i < length; i++) {
				this.tagScanner.append(ch[start + i]);
			}

		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qualifiedName) {
		if (localName.equals("mod")) {
			if (currentMod != null) {
				into.add(currentMod.build());
				currentMod = null;
			}
		} else if (localName.equals("alt_name")) {
			if (tagScanner != null && currentMod != null) {
				currentMod.getAltNames().add(tagScanner.toString());
				tagScanner = null;
			}
		} else if (localName.equals("specificity")) {
			if (currentMod != null && currentSpecificity != null) {
				currentSpecificity = null;
			}
		} else if (localName.equals("misc_notes")) {
			if (currentSpecificity != null) {
				String addition = "";
				if (tagScanner != null) {
					addition = tagScanner.toString();
					if (addition != null && !addition.equals("") && !addition.equals("null")) {
						currentSpecificity.addComment(addition.trim());
					}
				}
			}
		} else if (localName.equals("delta")) {
			if (currentMod != null && currentMassMono != null) {
				currentMod.setMassMono(currentMassMono);
				currentMod.setMassAverage(currentMassAverage);
				currentMod.setComposition(currentComposition);
				currentMassMono = null;
			}
		} else if (!localName.equals("elem") &&
				!localName.equals("element") &&
				!localName.equals("elements") &&
				!localName.equals("xref") &&
				!localName.equals("text") &&
				!localName.equals("source") &&
				!localName.equals("url") &&
				!localName.equals("brick") &&
				!localName.equals("aminoAcids") &&
				!localName.equals("Ignore") &&
				!localName.equals("NeutralLoss") &&
				!localName.equals("modifications") &&
				!localName.equals("aa") &&
				!localName.equals("amino_acids") &&
				!localName.equals("mod_bricks") &&
				!localName.equals("unimod")) {
			LOGGER.debug("Unused unimod element: " + qualifiedName);
		}
		tagScanner = new StringBuilder();
	}
}
