package edu.mayo.mprc.unimod;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Entry point to unimod parsing. Based on the version, it picks one of the two actual implementations.
 *
 * @author Roman Zenka
 */
public final class UnimodHandler extends DefaultHandler {
	private final Unimod into;
	private ContentHandler actualHandler;

	public UnimodHandler(final Unimod into) {
		this.into = into;
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

	public static Double getDoubleValue(Attributes attr, String attrName) {
		Double value = null;
		try {
			value = new Double(attr.getValue("", attrName));
		} catch (Exception ignore) {
			//SWALLOWED: just allow null to be returned
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
	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attr) throws SAXException {
		if (actualHandler == null) {
			if ("unimod".equals(localName)) {
				into.setMajorVersion(attr.getValue("majorVersion"));
				into.setMinorVersion(attr.getValue("minorVersion"));
				if ("1".equals(into.getMajorVersion())) {
					actualHandler = new Unimod1Handler(into);
				} else if ("2".equals(into.getMajorVersion())) {
					actualHandler = new Unimod2Handler(into);
				}
			}
		} else {
			actualHandler.startElement(namespaceURI, localName, qualifiedName, attr);
		}
	}


	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (actualHandler != null) {
			actualHandler.characters(ch, start, length);
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
		if (actualHandler != null) {
			actualHandler.endElement(namespaceURI, localName, qualifiedName);
		}
	}

}
