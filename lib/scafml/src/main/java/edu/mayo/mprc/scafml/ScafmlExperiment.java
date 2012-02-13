package edu.mayo.mprc.scafml;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.files.FileHolder;
import edu.mayo.mprc.utilities.xml.XMLUtilities;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * definition of an experiment
 * experiment contains biological samples
 */
public final class ScafmlExperiment extends FileHolder {
	private static final long serialVersionUID = 4851459805058267855L;
	/**
	 * can have more than one biological sample per experiment
	 */
	private final LinkedHashMap<String, ScafmlBiologicalSample> biologicalSamples;
	private String name;
	private final Map<String, ScafmlFastaDatabase> scafmlFastaDatabases;
	private ScafmlExport export;
	private boolean connectToNCBI = true;
	private boolean annotateWithGOA = true;
	private boolean reportDecoyHits;

	public ScafmlExperiment(String name) {
		biologicalSamples = new LinkedHashMap<String, ScafmlBiologicalSample>(1);
		scafmlFastaDatabases = new HashMap<String, ScafmlFastaDatabase>(1);
		this.name = name;
	}

	public ScafmlFastaDatabase getFastaDatabase(String id) {
		return this.scafmlFastaDatabases.get(id);
	}

	public void addFastaDatabase(ScafmlFastaDatabase pFastaDatabase) {
		if (pFastaDatabase == null) {
			throw new MprcException("null object for Biological Sample");
		}
		if (pFastaDatabase.getId() == null) {
			throw new MprcException("no id for Biological Sample object");
		}
		this.scafmlFastaDatabases.put(pFastaDatabase.getId(), pFastaDatabase);
	}

	public ScafmlBiologicalSample getBiologicalSample(String id) {
		return this.biologicalSamples.get(id);
	}

	public void addBiologicalSample(ScafmlBiologicalSample pBiologicalSample) {
		if (pBiologicalSample == null) {
			throw new MprcException("null object for Biological Sample");
		}
		if (pBiologicalSample.getId() == null) {
			throw new MprcException("no id for Biological Sample object");
		}
		this.biologicalSamples.put(pBiologicalSample.getId(), pBiologicalSample);
	}

	public void setName(String sName) {
		this.name = sName;
	}

	public String getName() {
		return name;
	}


	public void setExport(ScafmlExport pExport) {
		this.export = pExport;
	}

	public ScafmlExport getExport() {
		return export;
	}

	public boolean isConnectToNCBI() {
		return connectToNCBI;
	}

	public void setConnectToNCBI(boolean connectToNCBI) {
		this.connectToNCBI = connectToNCBI;
	}

	public boolean isAnnotateWithGOA() {
		return annotateWithGOA;
	}

	public void setAnnotateWithGOA(boolean annotateWithGOA) {
		this.annotateWithGOA = annotateWithGOA;
	}

	public boolean isReportDecoyHits() {
		return reportDecoyHits;
	}

	public void setReportDecoyHits(boolean reportDecoyHits) {
		this.reportDecoyHits = reportDecoyHits;
	}

	public void appendToDocument(StringBuilder result, String indent, ScafmlScaffold scaffold) {
		result
				.append(indent)
				.append("<" + "Experiment")
				.append(XMLUtilities.wrapatt("name", this.getName()))
				.append(XMLUtilities.wrapatt("connectToNCBI", connectToNCBI ? "true" : "false"))
				.append(XMLUtilities.wrapatt("annotateWithGOA", annotateWithGOA ? "true" : "false"))
				.append(XMLUtilities.wrapatt("spectrumNameRegEx", "([^ /(]+\\.dta)"));

		if (scaffold.getVersionMajor() >= 3) {
			result
					.append(XMLUtilities.wrapatt("peakListGeneratorName", "extract_msn"))
					.append(XMLUtilities.wrapatt("peakListDeisotoped", "false"));
		}

		result.append(">\n");

		// now the fasta databases
		for (ScafmlFastaDatabase fd : this.scafmlFastaDatabases.values()) {
			fd.appendToDocument(result, indent + "\t", isReportDecoyHits());
		}

		// now the biological samples
		for (ScafmlBiologicalSample bios : biologicalSamples.values()) {
			bios.appendToDocument(result, indent + "\t");
		}

		// now the exports
		this.getExport().appendToDocument(result, indent + "\t");

		result.append(indent + "</" + "Experiment" + ">\n");
	}
}

