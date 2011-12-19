package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.AbstractXmlDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.InputStream;
import java.io.OutputStream;

public final class ScaffoldParser {
	static final XmlFriendlyReplacer KEEP_UNDERSCORES = new XmlFriendlyReplacer("-_", "_");

	private ScaffoldParser() {
	}

	public static Scaffold loadScaffoldXml(InputStream stream) {
		// Change the replacer to keep underscores.
		// We marshall the classes manually anyway, so this is not a big deal


		return loadScaffoldXml(stream, new XppDriver(KEEP_UNDERSCORES));
	}

	public static Scaffold loadScaffoldXml(InputStream stream, AbstractXmlDriver driver) {
		XStream xs = new XStream(driver);
//		{
//			@Override
//			protected MapperWrapper wrapMapper(MapperWrapper next) {
//				return new MapperWrapper(next) {
//					public boolean shouldSerializeMember(Class definedIn, String fieldName) {
//						return definedIn != Object.class ? super.shouldSerializeMember(definedIn, fieldName) : false;
//					}
//				};
//			}
//		};

		xs.setMode(XStream.ID_REFERENCES);

		xs.processAnnotations(new Class[]{
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
		});

		return (Scaffold) xs.fromXML(stream);
	}

	public static void saveScaffoldXml(Scaffold data, OutputStream stream) {
		saveScaffoldXml(data, stream, new XppDriver(KEEP_UNDERSCORES));
	}

	public static void saveScaffoldXml(Scaffold data, OutputStream stream, AbstractXmlDriver driver) {
		XStream xs = new XStream(driver);
		xs.setMode(XStream.ID_REFERENCES);
		xs.processAnnotations(new Class[]{
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
		});

		xs.toXML(data, stream);
	}
}
