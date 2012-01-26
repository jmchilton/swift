package edu.mayo.mprc.unimod;

import com.google.common.base.Joiner;
import edu.mayo.mprc.MprcException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

/**
 * Reads testUniMod.xml
 */
public final class Unimod extends IndexedModSet {
	private static final int EXPECTED_BYTES_PER_DUMP_LINE = 50;
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
	 */
	public void parseUnimodXML(InputStream xmlStream) {

		XMLReader parser = null;
		try {
			parser = getParser(/*preferedParser*/"org.apache.xerces.parsers.SAXParser");
		} catch (SAXException e) {
			throw new MprcException("Could not obtain XML parser to parse unimod.xml", e);
		}

		try {
			parser.setContentHandler(new UnimodHandler(this));
			InputSource source = new InputSource(xmlStream);
			parser.parse(source);
		} catch (IOException e) {
			throw new MprcException("Could not read unimod.xml stream", e);
		} catch (SAXException e) {
			throw new MprcException("Could not parse unimod.xml stream", e);
		}
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

	/**
	 * @return A string containing all information from the unimod set for testing purposes.
	 */
	public String debugDump() {
		StringBuilder builder = new StringBuilder(EXPECTED_BYTES_PER_DUMP_LINE * size());

		final Set<ModSpecificity> specificitySet = getAllSpecificities(true);
		final ModSpecificity[] specificities = specificitySet.toArray(new ModSpecificity[specificitySet.size()]);
		Arrays.sort(specificities);

		builder.append("Modification Record ID\tModification Title\tModification Full Name\tModification Mono Mass\tModification Average Mass\tModification Alt Names\t" +
				"Modification Composition\tSpecificity Site\tSpecificity Terminus\tSpecificity Protein Only\tSpecificity Classification\tSpecificity Hidden\tSpecificity Group\tSpecificity Comments\n");
		for (ModSpecificity specificity : specificities) {
			final Mod mod = specificity.getModification();
			builder
					.append(mod.getRecordID())
					.append('\t')
					.append(mod.getTitle())
					.append('\t')
					.append(mod.getFullName())
					.append('\t')
					.append(mod.getMassMono())
					.append('\t')
					.append(mod.getMassAverage())
					.append('\t');

			Joiner.on(',').appendTo(builder, mod.getAltNames());

			builder
					.append('\t')
					.append(mod.getComposition())
					.append('\t')
					.append(specificity.getSite())
					.append('\t')
					.append(specificity.getTerm())
					.append('\t')
					.append(specificity.isProteinOnly())
					.append('\t')
					.append(specificity.getClassification())
					.append('\t')
					.append(specificity.getHidden())
					.append('\t')
					.append(specificity.getSpecificityGroup())
					.append('\t')
					.append(specificity.getComments())
					.append('\n');
		}
		return builder.toString();
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