package edu.mayo.mprc.scaffoldparser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.AbstractXmlDriver;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.InputStream;
import java.io.OutputStream;

public final class ScaffoldParser {
	private ScaffoldParser() {
	}

	public static Scaffold loadScaffoldXml(InputStream stream) {
		return loadScaffoldXml(stream, new XppDriver());
	}

	public static Scaffold loadScaffoldXml(InputStream stream, AbstractXmlDriver driver) {
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

		return (Scaffold) xs.fromXML(stream);
	}

	public static void saveScaffoldXml(Scaffold data, OutputStream stream) {
		saveScaffoldXml(data, stream, new XppDriver());
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
