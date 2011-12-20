package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.AbstractXmlDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import java.io.InputStream;
import java.io.OutputStream;

public final class ScaffoldParser {
	static final XmlFriendlyReplacer KEEP_UNDERSCORES = new XmlFriendlyReplacer("-_", "_");
	private static final Class[] SCAFFOLD_CLASSES = new Class[]{
			BiologicalSample.class,
			DisplayThresholds.class,
			Experiment.class,
			MascotThresholds.class,
			Modification.class,
			PeptideAnalysisIdentification.class,
			PeptideGroupIdentification.class,
			PreferredProteinAnnotation.class,
			ProteinAnalysisIdentification.class,
			ProteinGroup.class,
			Scaffold.class,
			SequestThresholds.class,
			SpectrumAnalysisIdentification.class,
			TandemMassSpectrometrySample.class,
			TandemThresholds.class,
	};

	private ScaffoldParser() {
	}

	/**
	 * Load Scaffold .xml export from input stream.
	 *
	 * @param stream Stream to load the .xml from.
	 * @return The root object {@link Scaffold} representing the entire Scaffold .xml file.
	 */
	public static Scaffold loadScaffoldXml(InputStream stream) {
		// Change the replacer to keep underscores.
		// We marshall the classes manually anyway, so this is not a big deal
		return loadScaffoldXml(stream, new XppDriver(KEEP_UNDERSCORES));
	}

	private static Scaffold loadScaffoldXml(InputStream stream, AbstractXmlDriver driver) {
		XStream xs = new XStream(driver) {
			@Override
			protected MapperWrapper wrapMapper(MapperWrapper next) {
				return new MapperWrapper(next) {
					public boolean shouldSerializeMember(Class definedIn, String fieldName) {
						return (!Object.class.equals(definedIn) || realClass(fieldName) != null) && super.shouldSerializeMember(definedIn, fieldName);
					}
				};
			}
		};

		xs.setMode(XStream.ID_REFERENCES);

		xs.processAnnotations(SCAFFOLD_CLASSES);

		return (Scaffold) xs.fromXML(stream);
	}

	/**
	 * Save given Scaffold .xml file representation to a give stream.
	 *
	 * @param scaffold Scaffold object to save
	 * @param stream   Stream to save into as .xml
	 */
	public static void saveScaffoldXml(Scaffold scaffold, OutputStream stream) {
		saveScaffoldXml(scaffold, stream, new XppDriver(KEEP_UNDERSCORES));
	}

	private static void saveScaffoldXml(Scaffold data, OutputStream stream, AbstractXmlDriver driver) {
		XStream xs = new XStream(driver);
		xs.setMode(XStream.ID_REFERENCES);
		xs.processAnnotations(SCAFFOLD_CLASSES);

		xs.toXML(data, stream);
	}
}
