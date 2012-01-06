package edu.mayo.mprc.unimod;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for unimod.xml file in the Scaffold format (older version of unimod.xml)
 *
 * @author Roman Zenka
 */
class Unimod1ContentHandler extends DefaultHandler {
	private static final Logger LOGGER = Logger.getLogger(Unimod1ContentHandler.class);
	private final Unimod into;

	private final Map<Integer, ModBuilder> modBuilders = new HashMap<Integer, ModBuilder>(300);
	private final Map<Integer, String> positions = new HashMap<Integer, String>(5);
	private final Map<Integer, String> classifications = new HashMap<Integer, String>(5);

	public Unimod1ContentHandler(final Unimod into) {
		this.into = into;
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attr) {
		if ("unimod".equals(localName)) {
			into.setMajorVersion(attr.getValue("majorVersion"));
			into.setMinorVersion(attr.getValue("minorVersion"));
		} else if ("modifications_row".equals(localName)) {
			/*
			Example

			<modifications_row

			username_of_poster="unimod"
			avge_mass="47.9982"
			mono_mass="47.984744"
			record_id="345"
			full_name="cysteine oxidation to cysteic acid"
			code_name="Cysteic_acid"
			date_time_modified="2005-10-03 00:21:40"
			date_time_posted="2005-09-10 22:39:38"
			composition="O(3)"
			group_of_poster="admin">
					  <misc_notes></misc_notes>
					 */

			final Integer modId = UnimodContentHandler.getIntegerValue(attr, "record_id");
			final ModBuilder modBuilder = getModBuilder(modId);
			modBuilder.setMassAverage(UnimodContentHandler.getDoubleValue(attr, "avge_mass"));
			modBuilder.setMassMono(UnimodContentHandler.getDoubleValue(attr, "mono_mass"));
			modBuilder.setFullName(attr.getValue("", "full_name"));
			modBuilder.setTitle(attr.getValue("", "code_name"));
			modBuilder.setComposition(attr.getValue("", "composition"));
			modBuilders.put(modId, modBuilder);
		} else if ("alt_names_row".equals(localName)) {
			// <alt_names_row alt_name="Pierce EZ-Link PEO-Iodoacetyl Biotin" record_id="321" mod_key="20"></alt_names_row>
			final Integer modId = UnimodContentHandler.getIntegerValue(attr, "mod_key");
			final ModBuilder modBuilder = getModBuilder(modId);
			modBuilder.getAltNames().add(attr.getValue("", "alt_name"));
		} else if ("positions_row".equals(localName)) {
			//  <positions_row position="-" record_id="1"></positions_row>
			//  <positions_row position="Anywhere" record_id="2"></positions_row>
			positions.put(
					UnimodContentHandler.getIntegerValue(attr, "record_id"),
					attr.getValue("position"));
		} else if ("classifications_row".equals(localName)) {
			//     <classifications_row record_id="2" classification="Post-translational"></classifications_row>
			classifications.put(
					UnimodContentHandler.getIntegerValue(attr, "record_id"),
					attr.getValue("classification"));
		} else if ("specificity_row".equals(localName)) {
			// <specificity_row position_key="3" nl_mono_mass="0.000000" classifications_key="13" record_id="2487" one_letter="N-term" mod_key="1" nl_composition="" hidden="0" nl_avge_mass="0.0000" spec_group="2">
			// <misc_notes></misc_notes>
			// </specificity_row>
			final ModBuilder modBuilder = getModBuilder(UnimodContentHandler.getIntegerValue(attr, "mod_key"));
			Boolean hidden = UnimodContentHandler.getBooleanValue(attr, "hidden");
			String oneLetter = attr.getValue("", "one_letter");
			Integer specificityGroup = UnimodContentHandler.getIntegerValue(attr, "spec_group");
			String classification = classifications.get(UnimodContentHandler.getIntegerValue(attr, "classifications_key"));
			String position = positions.get(UnimodContentHandler.getIntegerValue(attr, "position_key"));

			modBuilder.addSpecificityFromUnimod(oneLetter, position, hidden, classification, specificityGroup);
		}
	}

	private ModBuilder getModBuilder(int recordId) {
		final ModBuilder builder = modBuilders.get(recordId);
		if (builder == null) {
			final ModBuilder newBuilder = new ModBuilder();
			newBuilder.setRecordID(recordId);
			modBuilders.put(recordId, newBuilder);
			return newBuilder;
		}
		return builder;
	}

	@Override
	public void characters(char[] ch, int start, int length) {
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qualifiedName) {
		if ("unimod".equals(localName)) {
			dump();
		}
	}

	private void dump() {
		for (ModBuilder builder : modBuilders.values()) {
			into.add(builder.build());
		}
	}
}
