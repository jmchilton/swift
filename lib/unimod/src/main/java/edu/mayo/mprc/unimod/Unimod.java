package edu.mayo.mprc.unimod;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads testUniMod.xml
 */
public final class Unimod extends IndexedModSet {

	private static final Logger LOGGER = Logger.getLogger(Unimod.class);

	/**
	 * the unimod major version
	 */
	private String majorVersion = "2";

	/**
	 * the unimod minor version
	 */
	private String minorVersion = "0";

	/**
	 * null constructor
	 */
	public Unimod() {

	}

	/**
	 * It is the responsibility of the caller to close the stream.
	 *
	 * @param xmlStream a stream of xml
	 * @throws java.io.IOException      if there was a problem parsing the xmlStream
	 * @throws org.xml.sax.SAXException if we couldn't get SAXParser to work
	 */
	public void parseUnimodXML(InputStream xmlStream) throws IOException, SAXException {
		XMLReader parser = getParser(/*preferedParser*/"org.apache.xerces.parsers.SAXParser");

		parser.setContentHandler(new UnimodContentHandler(this));

		InputSource source = new InputSource(xmlStream);
		parser.parse(source);
		setName("Unimod " + getMajorVersion() + "." + getMinorVersion());
	}

	/**
	 * @param preferedParser the parser you want to find first
	 * @return a reader hopefully the prefered on
	 */
	private static XMLReader getParser(String preferedParser) throws SAXException {
		XMLReader toUse = null;
		try {
			toUse = XMLReaderFactory.createXMLReader(preferedParser);
		} catch (SAXException e) {

			toUse = XMLReaderFactory.createXMLReader();
		}
		return toUse;
	}

	public String getMajorVersion() {
		return majorVersion;
	}


	public void setMajorVersion(String majorVersion) {
		this.majorVersion = majorVersion;
	}

	public String getMinorVersion() {
		return minorVersion;
	}

	public void setMinorVersion(String minorVersion) {
		this.minorVersion = minorVersion;
	}

	/**
	 * this is a ContentHandler that does all of the work of parsing the unimode.xml file
	 */
	public String toString() {
		return "Unimod " + getMajorVersion() + "." + getMinorVersion();
	}

	private class UnimodContentHandler extends DefaultHandler {
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

		private Double getDoubleValue(Attributes attr, String attrName) {
			Double value = null;
			try {
				value = new Double(attr.getValue("", attrName));
			} catch (Exception ignore) {
				//SWALLOWED: just allow null to be returned
			}
			return value;
		}

		private Boolean getBooleanValue(Attributes attr, String attrName) {
			Boolean value = null;
			try {
				String strValue = attr.getValue("", attrName);
				value = (!strValue.equals("0"));
			} catch (Exception ignore) {
				//SWALLOWED: allow null return;
			}
			return value;
		}

		private Integer getIntegerValue(Attributes attr, String attrName) {
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
				majorVersion = attr.getValue("majorVersion");
				minorVersion = attr.getValue("minorVersion");
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

	@Override
	public boolean equals(Object t) {
		if (this == t) {
			return true;
		}
		if (!(t instanceof Unimod)) {
			return false;
		}
		if (!super.equals(t)) {
			return false;
		}

		Unimod unimod = (Unimod) t;

		if (getMajorVersion() != null ? !getMajorVersion().equals(unimod.getMajorVersion()) : unimod.getMajorVersion() != null) {
			return false;
		}
		if (getMinorVersion() != null ? !getMinorVersion().equals(unimod.getMinorVersion()) : unimod.getMinorVersion() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (getMajorVersion() != null ? getMajorVersion().hashCode() : 0);
		result = 31 * result + (getMinorVersion() != null ? getMinorVersion().hashCode() : 0);
		return result;
	}
}