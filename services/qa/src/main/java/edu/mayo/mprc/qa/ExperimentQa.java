package edu.mayo.mprc.qa;

import edu.mayo.mprc.daemon.files.FileHolder;

import java.io.File;
import java.util.List;

/**
 * Information about QA for a particular experiment.
 */
public class ExperimentQa extends FileHolder {
	private static final long serialVersionUID = 6226958874248391404L;
	private String experimentName;
	private File scaffoldSpectraFile;
	private List<MgfQaFiles> mgfQaFiles;
	private String scaffoldVersion;

	/**
	 * @param experimentName      Name of the Scaffold experiment.
	 * @param scaffoldSpectraFile Scaffold .tsv report with information per each spectrum.
	 * @param mgfQaFiles          A list of original .mgf files with a collection of QA information about that .mgf file.
	 *                            Order of the .mgf files matters - they should be reported in the same order as how the user entered them.
	 * @param scaffoldVersion     A string denoting the version of Scaffold (e.g. "2" versus "3"). In case the same file is searched by two Scaffolds,
	 *                            the version is used to separate the data files.
	 */
	public ExperimentQa(final String experimentName, final File scaffoldSpectraFile, final List<MgfQaFiles> mgfQaFiles, final String scaffoldVersion) {
		this.experimentName = experimentName;
		this.scaffoldSpectraFile = scaffoldSpectraFile;
		this.mgfQaFiles = mgfQaFiles;
		this.scaffoldVersion = scaffoldVersion;
	}

	public String getExperimentName() {
		return experimentName;
	}

	public File getScaffoldSpectraFile() {
		return scaffoldSpectraFile;
	}

	public List<MgfQaFiles> getMgfQaFiles() {
		return mgfQaFiles;
	}

	public String getScaffoldVersion() {
		return scaffoldVersion;
	}
}
