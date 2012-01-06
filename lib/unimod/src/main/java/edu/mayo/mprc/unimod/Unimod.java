package edu.mayo.mprc.unimod;

import edu.mayo.mprc.MprcException;
import org.apache.log4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
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
	 * Parses unimod.xml in the Mascot format.
	 * It is the responsibility of the caller to close the stream.
	 *
	 * @param xmlStream a stream of xml
	 * @throws java.io.IOException      if there was a problem parsing the xmlStream
	 * @throws org.xml.sax.SAXException if we couldn't get SAXParser to work
	 */
	public void parseUnimodXML(InputStream xmlStream) throws IOException, SAXException {
		parseUsingHandler(xmlStream, new UnimodContentHandler(this));
	}

	/**
	 * Parses unimod.xml in the format as provided by Scaffold. (Unimod 1.0)
	 *
	 * @param xmlStream Scaffold's unimod.xml
	 */
	public void parseUnimod1XML(InputStream xmlStream) {
		try {
			parseUsingHandler(xmlStream, new Unimod1ContentHandler(this));
		} catch (Exception e) {
			throw new MprcException("Could not parse Scaffold's unimod.xml", e);
		}
	}

	private void parseUsingHandler(InputStream xmlStream, ContentHandler contentHandler) throws SAXException, IOException {
		XMLReader parser = getParser(/*preferedParser*/"org.apache.xerces.parsers.SAXParser");

		parser.setContentHandler(contentHandler);

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